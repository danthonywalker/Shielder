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

final class RamThreadMonitor {

    private final long threadId;
    private final ThreadMXBean monitor;
    private final long initialRam;

    /**
     * Creates a new {@code RamThreadMonitor} that watches the CPU use of the provided thread.
     *
     * @param threadId The ID of the thread to look for.
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
     *
     *
     * @return
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
