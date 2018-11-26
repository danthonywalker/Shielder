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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a task submitted through a {@link Sandbox}.
 * <p>
 * This class provides a convenient internal container for context of a task submitted through a {@link Sandbox}. When
 * this class is initialized, and {@link SandboxEnvironment} values are present, respective monitoring objects are
 * initialized and will consequently begin monitoring activities.
 */
final class Task {

    private final Thread thread;
    private final CompletableFuture<?> promise;
    private final SandboxEnvironment environment;
    private final CpuThreadMonitor cpuMonitor;
    private final RamThreadMonitor ramMonitor;

    /**
     * Constructs a new {@code Task}.
     *
     * @param thread The thread that this task is {@link SandboxEnvironment#getExecutor() executing} on.
     * @param promise The {@link CompletableFuture} where its completion status reflects that of this task's.
     * @param environment The {@link SandboxEnvironment} that defines the limitations and context of this task.
     */
    Task(final Thread thread, final CompletableFuture<?> promise, final SandboxEnvironment environment) {

        this.thread = Objects.requireNonNull(thread);
        this.promise = Objects.requireNonNull(promise);
        this.environment = Objects.requireNonNull(environment);
        cpuMonitor = environment.getCpuLimit().isPresent() ? new CpuThreadMonitor(thread.getId()) : null;
        ramMonitor = environment.getRamLimit().isPresent() ? new RamThreadMonitor(thread.getId()) : null;
    }

    /**
     * Gets the thread that this task is {@link SandboxEnvironment#getExecutor() executing} on.
     *
     * @return The thread that this task is {@link SandboxEnvironment#getExecutor() executing} on.
     */
    Thread getThread() {
        return thread;
    }

    /**
     * Gets the {@link CompletableFuture} where its completion status reflects that of this task's.
     *
     * @return The {@link CompletableFuture} where its completion status reflects that of this task's.
     */
    CompletableFuture<?> getPromise() {
        return promise;
    }

    /**
     * Gets the {@link SandboxEnvironment} that defines the limitations and context of this task.
     *
     * @return The {@link SandboxEnvironment} that defines the limitations and context of this task.
     */
    SandboxEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Gets the {@link CpuThreadMonitor} for the {@link #getThread() thread} of this task, if present.
     *
     * @return The {@link CpuThreadMonitor} for the {@link #getThread() thread} of this task, if present.
     */
    Optional<CpuThreadMonitor> getCpuMonitor() {
        return Optional.ofNullable(cpuMonitor);
    }

    /**
     * Gets the {@link RamThreadMonitor} for the {@link #getThread() thread} of this task, if present.
     *
     * @return The {@link RamThreadMonitor} for the {@link #getThread() thread} of this task, if present.
     */
    Optional<RamThreadMonitor> getRamMonitor() {
        return Optional.ofNullable(ramMonitor);
    }

    @Override
    public String toString() {
        return "Task{" +
            "thread=" + thread +
            ", promise=" + promise +
            ", environment=" + environment +
            ", cpuMonitor=" + cpuMonitor +
            ", ramMonitor=" + ramMonitor +
            '}';
    }
}
