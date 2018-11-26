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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Monitors the <a href="https://en.wikipedia.org/wiki/CPU_time">CPU Time</a> of a targeted thread.
 * <p>
 * Monitoring does not occur until after this class's instantiation and each instantiation has an
 * initial usage of {@code 0}. This means two referentially unequal {@code CpuThreadMonitor} instances,
 * even with equal {@code threadId} fields, may return varying values from {@link #getMonitoredCpu()}.
 */
final class CpuThreadMonitor {

    private final long threadId;
    private final ThreadMXBean monitor;
    private final long initialCpu;

    /**
     * Constructs a new {@code CpuThreadMonitor}.
     *
     * @param threadId The {@link Thread#getId() ID} of the targeted thread.
     */
    CpuThreadMonitor(final long threadId) {
        monitor = ManagementFactory.getThreadMXBean();
        if (!monitor.isThreadCpuTimeEnabled()) {
            monitor.setThreadCpuTimeEnabled(true);
        }

        initialCpu = monitor.getThreadCpuTime(threadId);
        this.threadId = threadId;
    }

    /**
     * Gets the amount of CPU time, in nanoseconds, that this instance
     * has monitored for the targeted thread since its instantiation.
     *
     * @return The amount of CPU time, in nanoseconds, that this instance
     * has monitored for the targeted thread since its instantiation.
     */
    long getMonitoredCpu() {
        return monitor.getThreadCpuTime(threadId) - initialCpu;
    }

    @Override
    public String toString() {
        return "CpuThreadMonitor{" +
            "threadId=" + threadId +
            ", monitor=" + monitor +
            ", initialCpu=" + initialCpu +
            '}';
    }
}
