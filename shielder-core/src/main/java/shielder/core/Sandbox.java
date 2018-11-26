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

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A container for constraining arbitrary tasks within defined CPU and RAM limitations.
 * <p>
 * When tasks are submitted through {@link #startTask(Callable)} and its variants, an event loop (executed by
 * {@link #getExecutor()}) monitors their activity for detecting when limitations are exceeded defined by their
 * respective {@link SandboxEnvironment}. When a limitation is exceeded, the thread the task is executing on is
 * {@link Thread#interrupt() interrupted}. If a certain {@link SandboxEnvironment#getInterruptTimeout()} duration}
 * of time has passed since the interrupt, and the task has failed to complete, then the thread will be terminally
 * interrupted in an implementation-specific manner (see <b>Implementation Note</b>).
 * <p>
 * Only one instance of this class should exist within the lifetime of an application. This class is thread-safe.
 *
 * @implNote Tasks that fail to complete after an interrupt will be {@link Thread#stop() stopped}. This may have
 * devastating consequences on the state of the JVM, thus, it is imperative to prevent state that may be corrupted.
 * See the deprecation note of {@link Thread#stop()} for details.
 */
public final class Sandbox {

    /**
     * Returns a newly constructed {@code Sandbox}. The {@link #getDefaultEnvironment() default environment}
     * will have all its values initialized with default values (as defined by {@link SandboxEnvironment})
     * and a possibly newly initialized {@link #getExecutor() executor}.
     *
     * @return A newly constructed {@code Sandbox}.
     */
    public static Sandbox create() {
        return new Sandbox();
    }

    private final AtomicReference<ScheduledExecutorService> executor;
    private final AtomicReference<SandboxEnvironment> environment;
    private final TaskMonitor monitor;

    /** Constructs a new {@code Sandbox}. */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    private Sandbox() {
        executor = new AtomicReference<>(Executors.newSingleThreadScheduledExecutor());
        environment = new AtomicReference<>(SandboxEnvironment.builder().build());
        monitor = new TaskMonitor(this);
    }

    /**
     * Gets the default {@link SandboxEnvironment}.
     *
     * @return The default {@link SandboxEnvironment}.
     */
    public SandboxEnvironment getDefaultEnvironment() {
        return environment.get();
    }

    /**
     * Sets the default {@link SandboxEnvironment}.
     *
     * @param environment The default {@link SandboxEnvironment} to set. Must be non-null.
     */
    public void setDefaultEnvironment(final SandboxEnvironment environment) {
        this.environment.set(Objects.requireNonNull(environment));
    }

    /**
     * Gets the {@link ScheduledExecutorService executor}.
     * <p>
     * Shared usage of this executor should be approached with caution. Depending on its implementation, long-running
     * tasks (not submitted by/to this {@code Sandbox} instance) may prevent the event loop from functioning which may
     * allow tasks submitted to this {@code Sandbox} instance to significantly bypass their limitations.
     *
     * @return The {@link ScheduledExecutorService executor}.
     */
    public ScheduledExecutorService getExecutor() {
        return executor.get();
    }

    /**
     * Sets the {@link ScheduledExecutorService executor}.
     * <p>
     * The previous executor does not need to be explicitly shutdown and is discouraged. The previous executor will be
     * garbage collected when eligible, even when there are still executing tasks under this {@code Sandbox} instance.
     *
     * @param executor The {@link ScheduledExecutorService executor} to set. Must be non-null.
     */
    public void setExecutor(final ScheduledExecutorService executor) {
        this.executor.set(Objects.requireNonNull(executor));
    }

    /**
     * Executes a task within the CPU and RAM limitations defined by {@link #getDefaultEnvironment()}.
     * <p>
     * The task will be executed on the thread supplied by {@link SandboxEnvironment#getExecutor()}.
     *
     * @param task The {@link SandboxEnvironment environment} that the task will be executed under. Must be non-null.
     *
     * @return A {@link CompletableFuture} that will complete (normally or exceptionally) when the task completes while
     * respecting the limitations defined by the {@link SandboxEnvironment}, otherwise, completes exceptionally with a
     * subclass of {@link SandboxEnvironment}.
     *
     * @see #startTask(SandboxEnvironment, Callable)
     */
    public <T> CompletableFuture<T> startTask(final Callable<? extends T> task) {
        return monitor.startTask(environment.get(), Objects.requireNonNull(task));
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
    public <T> CompletableFuture<T> startTask(final SandboxEnvironment environment, final Callable<? extends T> task) {
        return monitor.startTask(environment, Objects.requireNonNull(task));
    }

    @Override
    public String toString() {
        return "Sandbox{" +
            "executor=" + executor +
            ", environment=" + environment +
            ", monitor=" + monitor +
            '}';
    }
}
