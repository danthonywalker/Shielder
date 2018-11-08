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

import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import shielder.core.Sandbox;

final class ShielderScriptEngineProxy implements SandboxExecutor, InvocationHandler,
    ShielderCompilable, ShielderInvocable, ShielderScriptEngine {

    static ShielderScriptEngine of(final ScriptEngine delegate,
                                   final ShielderScriptEngineFactory factory,
                                   final ClassLoader loader) {

        final var proxy = new ShielderScriptEngineProxy(delegate, factory);
        final var compilable = delegate instanceof Compilable;
        final var invocable = delegate instanceof Invocable;

        final var interfaces = (compilable && invocable)
            ? new Class[]{ShielderScriptEngine.class, ShielderCompilable.class, ShielderInvocable.class}
            : (compilable ? new Class[]{ShielderScriptEngine.class, ShielderCompilable.class}
            : (invocable ? new Class[]{ShielderScriptEngine.class, ShielderInvocable.class}
            : new Class[]{ShielderScriptEngine.class}));

        return (ShielderScriptEngine) Proxy.newProxyInstance(loader, interfaces, proxy);
    }

    private final ScriptEngine delegate;
    private final ShielderScriptEngineFactory factory;

    private ShielderScriptEngineProxy(final ScriptEngine delegate, final ShielderScriptEngineFactory factory) {

        this.delegate = Objects.requireNonNull(delegate);
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try { // This class implements all expected method objects. Rethrows originally thrown exceptions
            return method.invoke(this, args);
        } catch (final InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    @Override
    public Object invokeMethod(final Object thiz, final String name, final Object... args) throws ScriptException {
        return sandboxSync(getContext(), () -> ((Invocable) delegate).invokeMethod(thiz, name, args));
    }

    @Override
    public Object invokeFunction(final String name, final Object... args) throws ScriptException {
        return sandboxSync(getContext(), () -> ((Invocable) delegate).invokeFunction(name, args));
    }

    @Override
    public <T> T getInterface(final Class<T> clasz) {
        return ((Invocable) delegate).getInterface(clasz);
    }

    @Override
    public <T> T getInterface(final Object thiz, final Class<T> clasz) {
        return ((Invocable) delegate).getInterface(thiz, clasz);
    }

    @Override
    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        return sandboxSync(context, () -> delegate.eval(script, context));
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        return sandboxSync(context, () -> delegate.eval(reader, context));
    }

    @Override
    public Object eval(final String script) throws ScriptException {
        return sandboxSync(getContext(), () -> delegate.eval(script));
    }

    @Override
    public Object eval(final Reader reader) throws ScriptException {
        return sandboxSync(getContext(), () -> delegate.eval(reader));
    }

    @Override
    public Object eval(final String script, final Bindings n) throws ScriptException {
        return sandboxSync(getContext(), () -> delegate.eval(script, n));
    }

    @Override
    public Object eval(final Reader reader, final Bindings n) throws ScriptException {
        return sandboxSync(getContext(), () -> delegate.eval(reader, n));
    }

    @Override
    public void put(final String key, final Object value) {
        delegate.put(key, value);
    }

    @Override
    public Object get(final String key) {
        return delegate.get(key);
    }

    @Override
    public Bindings getBindings(final int scope) {
        return delegate.getBindings(scope);
    }

    @Override
    public void setBindings(final Bindings bindings, final int scope) {
        delegate.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return delegate.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return delegate.getContext();
    }

    @Override
    public void setContext(final ScriptContext context) {
        delegate.setContext(context);
    }

    @Override
    public Sandbox getSandbox() {
        return factory.getSandbox();
    }

    @Override
    public ShielderCompiledScript compile(final Reader script) throws ScriptException {
        final var compiledScript = sandboxSync(getContext(), () -> ((Compilable) delegate).compile(script));
        return new ShielderCompiledScript(compiledScript, this, this);
    }

    @Override
    public ShielderCompiledScript compile(final String script) throws ScriptException {
        final var compiledScript = sandboxSync(getContext(), () -> ((Compilable) delegate).compile(script));
        return new ShielderCompiledScript(compiledScript, this, this);
    }

    @Override
    public ShielderScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public ScriptEngine getDelegate() {
        return delegate;
    }

    @Override
    public String toString() {
        return "ShielderScriptEngineProxy{" +
            "delegate=" + delegate +
            ", factory=" + factory +
            '}';
    }
}
