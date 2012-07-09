/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.truevfs.driver.tar;

import net.truevfs.kernel.spec.FsArchiveDriverTestSuite;

/**
 * @author Christian Schlichtherle
 */
public final class TarBZip2DriverTest
extends FsArchiveDriverTestSuite<TarDriverEntry, TarBZip2Driver> {

    @Override
    protected TarBZip2Driver newArchiveDriver() {
        return new TestTarBZip2Driver();
    }

    @Override
    protected String getUnencodableName() {
        return null;
    }
}
