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

/** Signals that a task has exceeded the CPU limitation assigned for its sandbox. */
public class CpuExceededException extends SandboxException {

    private static final long serialVersionUID = -2935558107670974423L;

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with cause is not
     * automatically incorporated in this exception's detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CpuExceededException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the
     * class and detail message of {@code cause}). This constructor is useful for
     * exceptions that are little more than wrappers for other throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CpuExceededException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link Throwable#initCause(Throwable)}.
     *
     * @param message The detail message. The detail message is saved for later
     *                retrieval by the {@link Throwable#getMessage()} method.
     */
    public CpuExceededException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not initialized,
     * and may be subsequently be initialized by a call to {@link Throwable#initCause(Throwable)}.
     */
    public CpuExceededException() {
    }
}
