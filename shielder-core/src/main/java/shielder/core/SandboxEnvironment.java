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
import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SandboxEnvironment {

    public static final long DEFAULT_CPU_LIMIT = Long.MIN_VALUE;
    public static final long DEFAULT_RAM_LIMIT = Long.MIN_VALUE;
    public static final Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool();

    public static Builder builder() {
        return new Builder();
    }

    private final long cpuLimit;
    private final long ramLimit;
    private final Executor executor;

    private SandboxEnvironment(final long cpuLimit, final long ramLimit, final Executor executor) {

        this.cpuLimit = cpuLimit;
        this.ramLimit = ramLimit;
        this.executor = Objects.requireNonNull(executor);
    }

    public OptionalLong getCpuLimit() {
        return (cpuLimit < 0L) ? OptionalLong.empty() : OptionalLong.of(cpuLimit);
    }

    public OptionalLong getRamLimit() {
        return (ramLimit < 0L) ? OptionalLong.empty() : OptionalLong.of(ramLimit);
    }

    public Executor getExecutor() {
        return executor;
    }

    @Override
    public String toString() {
        return "SandboxEnvironment{" +
            "cpuLimit=" + cpuLimit +
            ", ramLimit=" + ramLimit +
            ", executor=" + executor +
            '}';
    }

    public static final class Builder {

        private long cpuLimit;
        private long ramLimit;
        private Executor executor;

        private Builder() {
            cpuLimit = DEFAULT_CPU_LIMIT;
            ramLimit = DEFAULT_RAM_LIMIT;
            executor = DEFAULT_EXECUTOR;
        }

        public Builder setCpuLimit(final long cpuLimit) {
            this.cpuLimit = cpuLimit;
            return this;
        }

        public Builder setRamLimit(final long ramLimit) {
            this.ramLimit = ramLimit;
            return this;
        }

        public Builder setExecutor(final Executor executor) {
            this.executor = Objects.requireNonNull(executor);
            return this;
        }

        public SandboxEnvironment build() {
            return new SandboxEnvironment(cpuLimit, ramLimit, executor);
        }

        @Override
        public String toString() {
            return "Builder{" +
                "cpuLimit=" + cpuLimit +
                ", ramLimit=" + ramLimit +
                ", executor=" + executor +
                '}';
        }
    }
}
