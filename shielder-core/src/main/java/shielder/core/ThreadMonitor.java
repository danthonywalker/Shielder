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
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

final class ThreadMonitor implements Runnable {

    private final AtomicReference<ScheduledExecutorService> executor;
    private final AtomicReference<Duration> timeout;
    private final Collection<TaskContext> tasks;

    ThreadMonitor(final ScheduledExecutorService initialExecutor) {
        executor = new AtomicReference<>(Objects.requireNonNull(initialExecutor));
        timeout = new AtomicReference<>(Duration.ofSeconds(1L));
        tasks = ConcurrentHashMap.newKeySet();
    }

    <T> CompletableFuture<T> startTask(final SandboxEnvironment environment, final Callable<? extends T> task) {

        final var threadReceiver = new CompletableFuture<Thread>();
        final var promise = new CompletableFuture<T>();

        environment.getExecutor().execute(() -> {
            threadReceiver.complete(Thread.currentThread());
            try { // All code is monitored beyond this point
                promise.complete(task.call());
            } catch (final Exception exception) {
                promise.completeExceptionally(exception);
            }
        });

        // To prevent the task from inflating its own CPU/RAM monitoring numbers create the TaskContext here
        return threadReceiver.thenAccept(thread -> tasks.add(new TaskContext(thread, promise, environment)))
            .thenAccept(ignored -> executor.get().execute(this))
            .thenCompose(ignored -> promise);
    }

    AtomicReference<ScheduledExecutorService> getExecutor() {
        return executor;
    }

    AtomicReference<Duration> getTimeout() {
        return timeout;
    }

    @Override
    @SuppressWarnings({"deprecation", "CallToThreadStopSuspendOrResumeManager"})
    public void run() {
        tasks.removeIf(task -> {
            final var exception = task.getCpuMonitor()
                .map(CpuThreadMonitor::getMonitoredCpu)
                .filter(monitored -> monitored > task.getEnvironment().getCpuLimit().orElse(Long.MAX_VALUE))
                .map(ignored -> new CpuExceededException("Task (" + task + ") has exceeded its CPU limit."))
                .map(SandboxException.class::cast)
                .or(() -> task.getRamMonitor()
                    .map(RamThreadMonitor::getMonitoredRam)
                    .filter(monitored -> monitored > task.getEnvironment().getRamLimit().orElse(Long.MAX_VALUE))
                    .map(ignored -> new RamExceededException("Task (" + task + ") has exceeded its RAM limit.")))
                .orElse(null);

            final var finished = task.getPromise().isDone();
            if (!finished && (exception != null)) {
                task.getThread().interrupt();
                executor.get().schedule(() -> {
                    if (!task.getPromise().isDone()) {
                        task.getThread().stop(); // Fail graceful interrupt
                        task.getPromise().completeExceptionally(exception);
                    }

                }, timeout.get().toNanos(), TimeUnit.NANOSECONDS);
                return true;
            }

            return finished;
        });

        if (!tasks.isEmpty()) {
            // Keep checking on the tasks
            executor.get().execute(this);
        }
    }

    @Override
    public String toString() {
        return "ThreadMonitor{" +
            "executor=" + executor +
            ", timeout=" + timeout +
            ", tasks=" + tasks +
            '}';
    }
}
