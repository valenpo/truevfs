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
package de.schlichtherle.truezip.io.socket.input;

import de.schlichtherle.truezip.io.rof.ReadOnlyFile;
import de.schlichtherle.truezip.io.socket.InputSocket;
import de.schlichtherle.truezip.io.socket.entry.CommonEntry;
import de.schlichtherle.truezip.io.socket.output.CommonOutputSocket;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates input streams for reading bytes from its <i>local target</i>
 * common entry.
 * <p>
 * Implementations do <em>not</em> need to be thread-safe:
 * Multithreading needs to be addressed by client classes.
 *
 * @param   <CE> the type of the {@link #getTarget() local target} common entry.
 * @see     CommonOutputSocket
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public abstract class CommonInputSocket<CE extends CommonEntry>
extends InputSocket<CE, CommonEntry, CommonInputSocket<CE>> {

    /**
     * Returns the non-{@code null} local common entry target.
     *
     * @return The non-{@code null} local common entry target.
     */
    @Override
    public abstract CE getTarget();

    /**
     * {@inheritDoc}
     *
     * @throws CommonInputBusyException if the local target is currently busy
     *         on input.
     *         This exception is guaranteed to be recoverable, meaning it
     *         should be possible to write the common entry again as soon as
     *         the local target is not busy anymore.
     * @throws FileNotFoundException if the local target is not accessible
     *         for some reason.
     * @throws IOException on any other exceptional condition.
     */
    @Override
    public abstract InputStream newInputStream() throws IOException;

    /**
     * {@inheritDoc}
     *
     * @throws CommonInputBusyException if the local target is currently busy
     *         on input.
     *         This exception is guaranteed to be recoverable, meaning it
     *         should be possible to write the common entry again as soon as
     *         the local target is not busy anymore.
     * @throws FileNotFoundException if the local target is not accessible
     *         for some reason.
     * @throws IOException on any other exceptional condition.
     */
    @Override
    public abstract ReadOnlyFile newReadOnlyFile() throws IOException;
}
