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

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

/**
 * Monitors the RAM usage of a targeted thread.
 * <p>
 * Monitoring does not occur until after this class's instantiation and each instantiation has an
 * initial usage of {@code 0}. This means two referentially unequal {@code RamThreadMonitor} instances,
 * even with equal {@code threadId} fields, may return varying values from {@link #getMonitoredRam()}.
 */
final class RamThreadMonitor {

    private final long threadId;
    private final ThreadMXBean monitor;
    private final long initialRam;

    /**
     * Constructs a new {@code RamThreadMonitor}.
     *
     * @param threadId The {@link Thread#getId() ID} of the targeted thread.
     */
    RamThreadMonitor(final long threadId) {
        final var possibleMonitor = ManagementFactory.getThreadMXBean();
        if (!(possibleMonitor instanceof ThreadMXBean)) { // Different class type
            throw new UnsupportedOperationException("The Java Virtual Machine " +
                "does not support thread memory allocation measurement.");
        }

        monitor = (ThreadMXBean) possibleMonitor;
        if (!monitor.isThreadAllocatedMemoryEnabled()) {
            monitor.setThreadAllocatedMemoryEnabled(true);
        }

        initialRam = monitor.getThreadAllocatedBytes(threadId);
        this.threadId = threadId;
    }

    /**
     * Gets the amount of RAM usage, in bytes, that this instance
     * has monitored for the targeted thread since its instantiation.
     *
     * @return The amount of RAM usage, in bytes, that this instance
     * has monitored for the targeted thread since its instantiation.
     */
    long getMonitoredRam() {
        return monitor.getThreadAllocatedBytes(threadId) - initialRam;
    }

    @Override
    public String toString() {
        return "RamThreadMonitor{" +
            "threadId=" + threadId +
            ", monitor=" + monitor +
            ", initialRam=" + initialRam +
            '}';
    }
}
