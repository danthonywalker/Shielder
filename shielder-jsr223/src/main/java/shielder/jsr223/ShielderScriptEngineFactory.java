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

import java.util.List;
import java.util.Objects;
import javax.script.ScriptEngineFactory;
import shielder.core.Sandbox;

public final class ShielderScriptEngineFactory implements ScriptEngineFactory {

    private final ScriptEngineFactory delegate;
    private final Sandbox sandbox;
    private final ClassLoader loader;

    ShielderScriptEngineFactory(final ScriptEngineFactory delegate, final Sandbox sandbox, final ClassLoader loader) {

        this.delegate = Objects.requireNonNull(delegate);
        this.sandbox = Objects.requireNonNull(sandbox);
        this.loader = loader;
    }

    @Override
    public String getEngineName() {
        return delegate.getEngineName();
    }

    @Override
    public String getEngineVersion() {
        return delegate.getEngineVersion();
    }

    @Override
    public List<String> getExtensions() {
        return delegate.getExtensions();
    }

    @Override
    public List<String> getMimeTypes() {
        return delegate.getMimeTypes();
    }

    @Override
    public List<String> getNames() {
        return delegate.getNames();
    }

    @Override
    public String getLanguageName() {
        return delegate.getLanguageName();
    }

    @Override
    public String getLanguageVersion() {
        return delegate.getLanguageVersion();
    }

    @Override
    public Object getParameter(final String key) {
        return delegate.getParameter(key);
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String m, final String... args) {
        return delegate.getMethodCallSyntax(obj, m, args);
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return delegate.getOutputStatement(toDisplay);
    }

    @Override
    public String getProgram(final String... statements) {
        return delegate.getProgram(statements);
    }

    @Override
    public ShielderScriptEngine getScriptEngine() {
        return ShielderScriptEngineProxy.of(delegate.getScriptEngine(), this, loader);
    }

    public ScriptEngineFactory getDelegate() {
        return delegate;
    }

    Sandbox getSandbox() {
        return sandbox;
    }

    @Override
    public String toString() {
        return "ShielderScriptEngineFactory{" +
            "delegate=" + delegate +
            ", sandbox=" + sandbox +
            ", loader=" + loader +
            '}';
    }
}
