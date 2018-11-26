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
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Defines the limitations and context for a task's execution in a {@link Sandbox}.
 * <p>
 * Each task submitted to a {@code Sandbox} is associated with some instance of this class. The values of this class are
 * immutable. Details on the behavior the values of this class manipulate are documented on their respective getters.
 */
public final class SandboxEnvironment {

    /** The constant default value of {@link #getRamLimit()}. */
    public static final long DEFAULT_RAM_LIMIT = 0L;

    /** The constant default value of {@link #getCpuLimit()}. */
    public static final Duration DEFAULT_CPU_LIMIT = Duration.ZERO;

    /** The constant default value of {@link #getScanInterval()}. */
    public static final Duration DEFAULT_SCAN_INTERVAL = Duration.ofSeconds(1L);

    /** The constant default value of {@link #getInterruptTimeout()}. */
    public static final Duration DEFAULT_INTERRUPT_TIMEOUT = Duration.ofSeconds(1L);

    /** The constant default value of {@link #getExecutor()}. */
    public static final Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool();

    /**
     * Returns a newly constructed {@code Builder}. All the values are initialized with default values as defined by the
     * constants residing in this class. As a consequence, {@code builder().build()} will result in an instance of this
     * class with default values as defined by the constants residing in this class.
     *
     * @return A newly constructed {@code Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private final long ramLimit;
    private final Duration cpuLimit;
    private final Duration scanInterval;
    private final Duration interruptTimeout;
    private final Executor executor;

    /**
     * Constructs a new {@code SandboxEnvironment}.
     *
     * @param ramLimit The value for {@link #getRamLimit()}.
     * @param cpuLimit The value for {@link #getCpuLimit()}.
     * @param scanInterval The value for {@link #getScanInterval()}.
     * @param interruptTimeout The value for {@link #getInterruptTimeout()}.
     * @param executor The value for {@link #getExecutor()}.
     */
    private SandboxEnvironment(final long ramLimit,
                               final Duration cpuLimit,
                               final Duration scanInterval,
                               final Duration interruptTimeout,
                               final Executor executor) {

        this.ramLimit = ramLimit;
        this.cpuLimit = Objects.requireNonNull(cpuLimit);
        this.scanInterval = Objects.requireNonNull(scanInterval);
        this.interruptTimeout = Objects.requireNonNull(interruptTimeout);
        this.executor = Objects.requireNonNull(executor);
    }

    /**
     * Gets the RAM limit applied for this {@code SandboxEnvironment}, if present.
     * <p>
     * If present, the returned value represents RAM usage, in bytes. If present, tasks that utilize more
     * RAM than the value returned by this method will complete with a {@link RamExceededException}.
     *
     * @return The RAM limit applied for this {@code SandboxEnvironment}, if present.
     */
    public OptionalLong getRamLimit() {
        return (ramLimit <= 0L) ? OptionalLong.empty() : OptionalLong.of(ramLimit);
    }

    /**
     * Gets the CPU limit applied for this {@code SandboxEnvironment}, if present.
     * <p>
     * If present, the returned value represents <a href="https://en.wikipedia.org/wiki/CPU_time">CPU Time</a>.
     * If present, tasks that utilize more <i>CPU Time</i> than the value returned by this method will complete
     * with a {@link CpuExceededException}.
     *
     * @return The CPU limit applied for this {@code SandboxEnvironment}, if present.
     */
    public Optional<Duration> getCpuLimit() {
        return Optional.of(cpuLimit).filter(duration -> duration.compareTo(DEFAULT_CPU_LIMIT) > 0);
    }

    /**
     * Gets the interval/frequency a task should be scanned for limitation violations.
     * <p>
     * When a task is submitted through the {@link Sandbox} it will periodically be scanned for violations of
     * limitations defined by this instance of {@code SandboxEnvironment}. Using {@link Sandbox#getExecutor()}, a
     * scan will be scheduled every {@code scanInterval} until the task's completion (normally or exceptionally).
     *
     * @return The interval/frequency a task should be scanned for limitations violations.
     */
    public Duration getScanInterval() {
        return scanInterval;
    }

    /**
     * Gets the amount of time allocated between a thread's interruption and a task's termination.
     * <p>
     * When a task has exceeded its limitations, as defined by this instance of {@code SandboxEnvironment}, it will
     * be immediately {@link Thread#interrupt() interrupted}. If after an {@code interruptTimeout} amount of time has
     * passed, and the task has yet to complete, then it will be terminally interrupted in an implementation-specific
     * manner. This termination task is scheduled by {@link Sandbox#getExecutor()}.
     * <p>
     * It should be noted that the task will always be completed exceptionally with a subclass of
     * {@link SandboxException} if a limitation is exceeded. However, if the task completes after
     * an interrupt, but before the timeout, then that exception may be ignored.
     *
     * @return The amount of time allocated between a thread's interruption and a task's termination.
     */
    public Duration getInterruptTimeout() {
        return interruptTimeout;
    }

    /**
     * Gets the {@link Executor} that a task will be executed on after submission.
     * <p>
     * When a task is submitted to a {@link Sandbox}, it will be executed on a thread supplied by
     * {@link Executor#execute(Runnable)}. An implementation that uses the current thread should
     * <i>not</i> be allowed as this will cause submissions to block.
     *
     * @return The {@link Executor} that a task will be executed on after submission.
     */
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public String toString() {
        return "SandboxEnvironment{" +
            "ramLimit=" + ramLimit +
            ", cpuLimit=" + cpuLimit +
            ", scanInterval=" + scanInterval +
            ", interruptTimeout=" + interruptTimeout +
            ", executor=" + executor +
            '}';
    }

    /**
     * A builder pattern for {@link SandboxEnvironment}. All setters mutate the current instance of this
     * class, thus, sharing an instance of this class is not recommended. This class is not thread-safe.
     */
    public static final class Builder {

        private long ramLimit;
        private Duration cpuLimit;
        private Duration scanInterval;
        private Duration interruptTimeout;
        private Executor executor;

        /** Constructs a new {@code Builder}. */
        private Builder() {
            ramLimit = DEFAULT_RAM_LIMIT;
            cpuLimit = DEFAULT_CPU_LIMIT;
            scanInterval = DEFAULT_SCAN_INTERVAL;
            interruptTimeout = DEFAULT_INTERRUPT_TIMEOUT;
            executor = DEFAULT_EXECUTOR;
        }

        /**
         * Sets the RAM limit for this builder. A negative value {@code ramLimit < 0}
         * will result in an absent value for {@link #getRamLimit()}.
         *
         * @param ramLimit The RAM Limit for this builder.
         * @return This instance.
         */
        public Builder setRamLimit(final long ramLimit) {
            this.ramLimit = ramLimit;
            return this;
        }

        /**
         * Sets the CPU limit for this builder. A null value will result a
         * {@link #DEFAULT_CPU_LIMIT default value} for {@link #getCpuLimit()}.
         *
         * @param cpuLimit The CPU limit for this builder.
         * @return This instance.
         */
        public Builder setCpuLimit(final Duration cpuLimit) {
            this.cpuLimit = (cpuLimit == null) ? DEFAULT_CPU_LIMIT : cpuLimit;
            return this;
        }

        /**
         * Sets the scan interval for this builder. A null value will result a
         * {@link #DEFAULT_SCAN_INTERVAL default value} for {@link #getScanInterval()}.
         *
         * @param scanInterval The scan interval for this builder.
         * @return This instance.
         */
        public Builder setScanInterval(final Duration scanInterval) {
            this.scanInterval = (scanInterval == null) ? DEFAULT_SCAN_INTERVAL : scanInterval;
            return this;
        }

        /**
         * Sets the interrupt timeout for this builder. A null value will result a
         * {@link #DEFAULT_INTERRUPT_TIMEOUT default value} for {@link #getInterruptTimeout()}.
         *
         * @param interruptTimeout The interrupt timeout for this builder.
         * @return This instance.
         */
        public Builder setInterruptTimeout(final Duration interruptTimeout) {
            this.interruptTimeout = (interruptTimeout == null) ? DEFAULT_INTERRUPT_TIMEOUT : interruptTimeout;
            return this;
        }

        /**
         * Sets the executor for this builder. A null value will result a
         * {@link #DEFAULT_EXECUTOR default value} for {@link #getExecutor()}.
         *
         * @param executor The executor for this builder.
         * @return This instance.
         */
        public Builder setExecutor(final Executor executor) {
            this.executor = (executor == null) ? DEFAULT_EXECUTOR : executor;
            return this;
        }

        /**
         * Returns a newly constructed {@link SandboxEnvironment} with its values
         * equal to the values of prior invocations for this instance's setters.
         *
         * @return A newly constructed {@link SandboxEnvironment}.
         */
        public SandboxEnvironment build() {
            return new SandboxEnvironment(ramLimit, cpuLimit, scanInterval, interruptTimeout, executor);
        }

        @Override
        public String toString() {
            return "Builder{" +
                "ramLimit=" + ramLimit +
                ", cpuLimit=" + cpuLimit +
                ", scanInterval=" + scanInterval +
                ", interruptTimeout=" + interruptTimeout +
                ", executor=" + executor +
                '}';
        }
    }
}
