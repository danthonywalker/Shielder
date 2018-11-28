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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import shielder.core.Sandbox;
import shielder.core.SandboxEnvironment;

@FunctionalInterface
interface SandboxExecutor {

    private static ScriptException wrapThrowable(final Throwable throwable) {
        return (throwable instanceof ScriptException) ? (ScriptException) throwable
            : ((throwable instanceof Exception) ? new ScriptException((Exception) throwable)
            : wrapThrowable(new Exception(throwable)));
    }

    Sandbox getSandbox();

    default <T> T sandboxSync(final ScriptContext context, final Callable<? extends T> task) throws ScriptException {

        try {
            return sandboxAsync(context, task).join();
        } catch (final CompletionException exception) {
            throw wrapThrowable(exception.getCause());
        } catch (final CancellationException exception) {
            throw wrapThrowable(exception);
        }
    }

    default <T> CompletableFuture<T> sandboxAsync(final ScriptContext context, final Callable<? extends T> task) {

        final var attribute = (SandboxEnvironment) context.getAttribute(ShielderScriptEngine.SANDBOX_ENVIRONMENT);
        final var environment = (attribute == null) ? getSandbox().getDefaultEnvironment() : attribute;
        final var promise = new CompletableFuture<T>();

        return getSandbox().startTask(environment, task)
            .handle((result, exception) -> (exception != null)
                ? promise.completeExceptionally(wrapThrowable(exception))
                : promise.complete(result))
            .thenCompose(ignored -> promise);
    }
}
