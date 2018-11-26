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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import shielder.core.Sandbox;

public final class ShielderScriptEngineManager extends ScriptEngineManager {

    private final Sandbox sandbox;
    private final ClassLoader loader;

    public ShielderScriptEngineManager() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ShielderScriptEngineManager(final ClassLoader loader) {

        super(loader);
        this.loader = loader;
        sandbox = Sandbox.create();
    }

    @Override
    public ShielderScriptEngine getEngineByExtension(final String extension) {
        return wrapEngine(super.getEngineByExtension(extension));
    }

    @Override
    public ShielderScriptEngine getEngineByMimeType(final String mimeType) {
        return wrapEngine(super.getEngineByMimeType(mimeType));
    }

    @Override
    public ShielderScriptEngine getEngineByName(final String shortName) {
        return wrapEngine(super.getEngineByName(shortName));
    }

    public ShielderScriptEngineFactory wrapFactory(final ScriptEngineFactory factory) {
        if (factory instanceof ShielderScriptEngineFactory) {
            return (ShielderScriptEngineFactory) factory;
        }

        return new ShielderScriptEngineFactory(factory, sandbox, loader);
    }

    public ShielderScriptEngine wrapEngine(final ScriptEngine engine) {
        if (engine instanceof ShielderScriptEngine) {
            return (ShielderScriptEngine) engine;
        }

        return ShielderScriptEngineProxy.of(engine, wrapFactory(engine.getFactory()), loader);
    }

    public Sandbox getSandbox() {
        return sandbox;
    }

    @Override
    public String toString() {
        return "ShielderScriptEngineManager{" +
            "sandbox=" + sandbox +
            ", loader=" + loader +
            '}';
    }
}
