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

final class TaskContext {

    private final Thread thread;
    private final CompletableFuture<?> promise;
    private final SandboxEnvironment environment;
    private final CpuThreadMonitor cpuMonitor;
    private final RamThreadMonitor ramMonitor;

    TaskContext(final Thread thread, final CompletableFuture<?> promise, final SandboxEnvironment environment) {

        this.thread = Objects.requireNonNull(thread);
        this.promise = Objects.requireNonNull(promise);
        this.environment = Objects.requireNonNull(environment);
        cpuMonitor = environment.getCpuLimit().isPresent() ? new CpuThreadMonitor(thread.getId()) : null;
        ramMonitor = environment.getRamLimit().isPresent() ? new RamThreadMonitor(thread.getId()) : null;
    }

    Thread getThread() {
        return thread;
    }

    CompletableFuture<?> getPromise() {
        return promise;
    }

    SandboxEnvironment getEnvironment() {
        return environment;
    }

    Optional<CpuThreadMonitor> getCpuMonitor() {
        return Optional.ofNullable(cpuMonitor);
    }

    Optional<RamThreadMonitor> getRamMonitor() {
        return Optional.ofNullable(ramMonitor);
    }

    @Override
    public String toString() {
        return "TaskContext{" +
            "thread=" + thread +
            ", promise=" + promise +
            ", environment=" + environment +
            ", cpuMonitor=" + cpuMonitor +
            ", ramMonitor=" + ramMonitor +
            '}';
    }
}
