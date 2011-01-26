/*
 * Copyright (C) 2011 Schlichtherle IT Services
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
package de.schlichtherle.truezip.io;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract decorator for an output stream.
 * This is a clean room implementation of its cousin {@link FilterOutputStream}
 * in the JSE, but optimized for performance and <em>without</em>
 * multithreading support.
 *
 * @see     DecoratingInputStream
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public abstract class DecoratingOutputStream extends OutputStream {

    /** The nullable decorated output stream. */
    @Nullable
    protected OutputStream delegate;

    /**
     * Constructs a new synchronized output stream.
     * This object will synchronize on itself.
     *
     * @param out the output stream to wrap in this decorator.
     */
    protected DecoratingOutputStream(final OutputStream out) {
        this.delegate = out;
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public final void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            delegate.close();
        }
    }
}
