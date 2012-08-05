/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.extension.jmx;

import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.NotThreadSafe;
import net.java.truecommons.io.DecoratingOutputStream;

/**
 * @author Christian Schlichtherle
 */
@NotThreadSafe
final class JmxOutputStream extends DecoratingOutputStream {
    private final JmxIoStatistics stats;

    JmxOutputStream(@WillCloseWhenClosed OutputStream out, JmxIoStatistics stats) {
        super(out);
        assert null != stats;
        this.stats = stats;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        stats.addBytesWritten(1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        stats.addBytesWritten(len);
    }    
}
