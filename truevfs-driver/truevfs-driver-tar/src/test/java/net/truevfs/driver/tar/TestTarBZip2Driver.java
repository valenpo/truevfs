/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.truevfs.driver.tar;

import net.truevfs.kernel.spec.TestConfig;
import net.truevfs.kernel.spec.cio.IoPool;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * Extends its super class to configure it for minimizing heap usage.
 * 
 * @author Christian Schlichtherle
 */
public final class TestTarBZip2Driver extends TarBZip2Driver {

    @Override
    public IoPool<?> getIoPool() {
        return TestConfig.get().getIoPoolProvider().ioPool();
    }

    @Override
    public int getLevel() {
        return BZip2CompressorOutputStream.MIN_BLOCKSIZE;
    }
}
