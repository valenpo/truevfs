/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.truevfs.driver.jar;

import net.truevfs.driver.zip.core.AbstractZipDriverEntry;
import net.truevfs.kernel.spec.FsArchiveDriverTestSuite;
import net.truevfs.kernel.spec.cio.IoBuffer;
import net.truevfs.kernel.spec.cio.IoPool;

/**
 * @author Christian Schlichtherle
 */
public final class CheckedJarDriverTest
extends FsArchiveDriverTestSuite<AbstractZipDriverEntry, CheckedJarDriver> {

    @Override
    protected CheckedJarDriver newArchiveDriver() {
        return new CheckedJarDriver() {
            @Override
            public IoPool<? extends IoBuffer<?>> getIoPool() {
                return getTestConfig().getIoPoolProvider().ioPool();
            }
        };
    }

    @Override
    protected String getUnencodableName() {
        return null;
    }
}
