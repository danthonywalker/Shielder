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

import java.util.Objects;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

public final class ShielderCompiledScript extends CompiledScript {

    private final CompiledScript delegate;
    private final ShielderScriptEngine engine;
    private final SandboxExecutor executor;

    ShielderCompiledScript(final CompiledScript delegate,
                           final ShielderScriptEngine engine,
                           final SandboxExecutor executor) {

        this.delegate = Objects.requireNonNull(delegate);
        this.engine = Objects.requireNonNull(engine);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public Object eval() throws ScriptException {
        return executor.sandboxSync(engine.getContext(), delegate::eval);
    }

    @Override
    public Object eval(final Bindings bindings) throws ScriptException {
        return executor.sandboxSync(engine.getContext(), () -> delegate.eval(bindings));
    }

    @Override
    public Object eval(final ScriptContext context) throws ScriptException {
        return executor.sandboxSync(context, () -> delegate.eval(context));
    }

    @Override
    public ShielderScriptEngine getEngine() {
        return engine;
    }

    public CompiledScript getDelegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return "ShielderCompiledScript{" +
            "delegate=" + delegate +
            ", engine=" + engine +
            ", executor=" + executor +
            '}';
    }
}
