/*
 * Copyright 2010 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schlichtherle.truezip.util;

/**
 * Abstract implementation of exception builder.
 * Subclasses must implement {@link #update(Throwable, Throwable)} and may
 * override {@link #post(Throwable)}.
 *
 * @author Christian Schlichtherle
 * @version $Id$
 * @param <C> The type of the cause exception.
 * @param <T> The type of the assembled exception.
 */
public abstract class AbstractExceptionBuilder<C extends Throwable, T extends Throwable>
implements ExceptionBuilder<C, T> {

    private T assembly;

    /**
     * This method is called to update the given {@code previous} result of
     * the assembly with the given {@code cause}.
     * 
     * @param cause A(nother) non-{@code null} cause exception to add to the
     *        assembly.
     * @param previous The previous result of the assembly or {@code null} if
     *        this is the first call since the creation of this instance or the
     *        last assembly has been checked out.
     * @return The assembled exception. {@code null} is not permitted.
     */
    protected abstract T update(T previous, C cause);

    /**
     * This method is called to post-process the given result of the assembly
     * after it has been checked out.
     * <p>
     * The implementation in the class {@link AbstractExceptionBuilder} simply
     * returns the parameter.
     *
     * @param assembly The checked out result of the exception assembly
     *        - may be {@code null}.
     * @return The post-processed checked out result of the exception assembly
     *         - may be {@code null}.
     */
    protected T post(T assembly) {
        return assembly;
    }

    private T checkout() {
        T t = assembly;
        assembly = null;
        return t;
    }

    /**
     * {@inheritDoc}
     *
     * @see #update(Throwable, Throwable)
     * @see #post(Throwable)
     */
    public final T fail(C cause) {
        if (cause == null)
            throw new NullPointerException();
        assembly = update(assembly, cause);
        return post(checkout());
    }

    /**
     * {@inheritDoc}
     *
     * @see #update(Throwable, Throwable)
     */
    public final void warn(C cause) {
        if (cause == null)
            throw new NullPointerException();
        assembly = update(assembly, cause);
    }

    /**
     * {@inheritDoc}
     *
     * @see #post(Throwable)
     */
    public final void check() throws T {
        T t = post(checkout());
        if (t != null)
            throw t;
    }
}
