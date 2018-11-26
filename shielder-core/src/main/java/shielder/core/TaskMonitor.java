/*
 * This file is part of Shielder.
 *
 * Shielder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shielder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Shielder.  If not, see <https://www.gnu.org/licenses/>.
 */
package shielder.core;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An auxiliary class for {@link Sandbox} implementation.
 * <p>
 * This class performs all the behaviors defined by {@link Sandbox#startTask(SandboxEnvironment, Callable)} (using
 * a method using the same definition). This class is purely for separation of concerns; there is no real concrete
 * reason the functionality of this class cannot be integrated into {@code Sandbox} other than code organization.
 */
final class TaskMonitor {

    private final Sandbox sandbox;

    /**
     * Constructs a new {@code TaskMonitor}.
     *
     * @param sandbox The {@link Sandbox} for which this class provides an implementation.
     */
    TaskMonitor(final Sandbox sandbox) {
        this.sandbox = Objects.requireNonNull(sandbox);
    }

    /**
     * Executes a task within the CPU and RAM limitations defined by the supplied {@link SandboxEnvironment}.
     * <p>
     * The task will be executed on the thread supplied by {@link SandboxEnvironment#getExecutor()}.
     *
     * @param environment The {@link SandboxEnvironment} that the task will be executed under. Must be non-null.
     * @param task The task to sandbox within the limitations defined by {@link SandboxEnvironment}. Must be non-null.

     * @return A {@link CompletableFuture} that will complete (normally or exceptionally) when the task completes while
     * respecting the limitations defined by the {@link SandboxEnvironment}, otherwise, completes exceptionally with a
     * subclass of {@link SandboxEnvironment}.
     */
    <T> CompletableFuture<T> startTask(final SandboxEnvironment environment, final Callable<? extends T> task) {

        final var threadReceiver = new CompletableFuture<Thread>();
        final var promise = new CompletableFuture<T>();

        environment.getExecutor().execute(() -> {
            threadReceiver.complete(Thread.currentThread());
            try { // All code is monitored beyond this point
                promise.complete(task.call());
            } catch (final Throwable exception) {
                promise.completeExceptionally(exception);
            }
        });

        // To prevent tasks from inflating CPU/RAM monitoring numbers, create a Task here
        return threadReceiver.thenApply(thread -> new Task(thread, promise, environment))
            .thenAccept(this::scan)
            .thenCompose(ignored -> promise);
    }

    /**
     * Scans the supplied {@code Task}, checking if its execution has violated a limitation defined by its
     * {@link Task#getEnvironment() SandboxEnvironment}. This method will be invoked until its completion
     * (normally or exceptionally) every {@link SandboxEnvironment#getScanInterval() scan interval}.
     *
     * @param task The {@code Task} to scan.
     */
    @SuppressWarnings({"deprecation", "CallToThreadStopSuspendOrResumeManager"})
    private void scan(final Task task) {
        final var exception = task.getCpuMonitor()
            .map(CpuThreadMonitor::getMonitoredCpu)
            .map(Duration::ofNanos)
            .filter(monitored -> task.getEnvironment().getCpuLimit().map(monitored::compareTo).orElse(0) > 0)
            .map(ignored -> new CpuExceededException("Task (" + task + ") has exceeded its CPU limit."))
            .map(SandboxException.class::cast)
            .or(() -> task.getRamMonitor()
                .map(RamThreadMonitor::getMonitoredRam)
                .filter(monitored -> monitored > task.getEnvironment().getRamLimit().orElse(Long.MAX_VALUE))
                .map(ignored -> new RamExceededException("Task (" + task + ") has exceeded its RAM limit.")))
            .orElse(null);

        final var running = !task.getPromise().isDone();
        if (running && (exception != null)) {
            task.getThread().interrupt();
            sandbox.getExecutor().schedule(() -> {
                if (!task.getPromise().isDone()) {
                    task.getThread().stop();
                }

                task.getPromise().completeExceptionally(exception);
            }, task.getEnvironment().getInterruptTimeout().toNanos(), TimeUnit.NANOSECONDS);

        } else if (running) {
            final var scanInterval = task.getEnvironment().getScanInterval().toNanos();
            sandbox.getExecutor().schedule(() -> scan(task), scanInterval, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public String toString() {
        return "TaskMonitor{" +
            "sandbox=" + sandbox +
            '}';
    }
}
