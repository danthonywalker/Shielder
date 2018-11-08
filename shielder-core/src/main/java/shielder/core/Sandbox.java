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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public final class Sandbox {

    public static Sandbox create() {
        return new Sandbox();
    }

    private final AtomicReference<SandboxEnvironment> environment;
    private final ThreadMonitor monitor;

    private Sandbox() {
        environment = new AtomicReference<>(SandboxEnvironment.builder().build());
        monitor = new ThreadMonitor(Executors.newSingleThreadScheduledExecutor());
    }

    public SandboxEnvironment getDefaultEnvironment() {
        return environment.get();
    }

    public void setDefaultEnvironment(final SandboxEnvironment environment) {
        this.environment.set(Objects.requireNonNull(environment));
    }

    public ScheduledExecutorService getExecutor() {
        return monitor.getExecutor().get();
    }

    public void setExecutor(final ScheduledExecutorService executor) {
        monitor.getExecutor().set(Objects.requireNonNull(executor));
    }

    public Duration getInterruptTimeout() {
        return monitor.getTimeout().get();
    }

    public void setInterruptTimeout(final Duration interruptTimeout) {
        monitor.getTimeout().set(Objects.requireNonNull(interruptTimeout));
    }

    public <T> CompletableFuture<T> startTask(final Callable<? extends T> task) {
        return monitor.startTask(environment.get(), Objects.requireNonNull(task));
    }

    public <T> CompletableFuture<T> startTask(final SandboxEnvironment environment, final Callable<? extends T> task) {
        return monitor.startTask(environment, Objects.requireNonNull(task));
    }

    @Override
    public String toString() {
        return "Sandbox{" +
            "environment=" + environment +
            ", monitor=" + monitor +
            '}';
    }
}
