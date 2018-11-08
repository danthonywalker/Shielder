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

public class SandboxException extends Exception {

    private static final long serialVersionUID = 1884532992528005776L;

    public SandboxException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SandboxException(final Throwable cause) {
        super(cause);
    }

    public SandboxException(final String message) {
        super(message);
    }

    public SandboxException() {
    }
}
