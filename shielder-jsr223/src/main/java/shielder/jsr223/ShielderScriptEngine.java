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

public interface ShielderScriptEngine extends ScriptEngine {

    String SANDBOX_ENVIRONMENT = "shielder.jsr223.sandbox_environment";

    @Override
    ShielderScriptEngineFactory getFactory();

    ScriptEngine getDelegate();
}
