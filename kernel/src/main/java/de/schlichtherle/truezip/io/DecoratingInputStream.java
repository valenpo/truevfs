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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract decorator for an input stream.
 * This is a clean room implementation of its cousin {@link FilterInputStream}
 * in the JSE, but optimized for performance and <em>without</em>
 * multithreading support.
 *
 * @see     DecoratingOutputStream
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public abstract class DecoratingInputStream extends InputStream {

    /** The nullable decorated input stream. */
    @Nullable
    protected InputStream delegate;

    /**
     * Constructs a new filter input stream.
     *
     * @param in the input stream to wrap in this decorator.
     */
    protected DecoratingInputStream(final InputStream in) {
        this.delegate = in;
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public final int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
}
