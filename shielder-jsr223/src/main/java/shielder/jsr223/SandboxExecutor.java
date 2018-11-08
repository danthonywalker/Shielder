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
package shielder.jsr223;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import shielder.core.Sandbox;
import shielder.core.SandboxEnvironment;

@FunctionalInterface
interface SandboxExecutor {

    Sandbox getSandbox();

    private SandboxEnvironment getEnvironment(final ScriptContext context) {
        final var environment = (SandboxEnvironment) context.getAttribute(ShielderScriptEngine.SANDBOX_ENVIRONMENT);
        return (environment == null) ? getSandbox().getDefaultEnvironment() : environment;
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    default <T> T sandboxSync(final ScriptContext context, final Callable<? extends T> task) throws ScriptException {

        try { // Avoid sandboxAsync as unnecessary wrapping of exceptions occur
            return getSandbox().startTask(getEnvironment(context), task).get();
        } catch (final Exception exception) {
            throw new ScriptException(exception);
        }
    }

    default <T> CompletableFuture<T> sandboxAsync(final ScriptContext context, final Callable<? extends T> task) {

        final var promise = new CompletableFuture<T>();
        return getSandbox().startTask(getEnvironment(context), task)
            .handle((result, exception) -> (exception != null)
                // ScriptException cannot be thrown directly in a lambda so promise is used
                ? promise.completeExceptionally(new ScriptException((Exception) exception))
                : promise.complete(result))
            .thenCompose(ignored -> promise);
    }
}
