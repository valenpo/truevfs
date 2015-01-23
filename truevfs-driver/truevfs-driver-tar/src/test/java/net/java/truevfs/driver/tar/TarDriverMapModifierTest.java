/*
 * Copyright (C) 2005-2015 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.truevfs.driver.tar;

import net.java.truevfs.driver.tar.TarDriverMapModifier;
import net.java.truevfs.kernel.spec.spi.FsDriverMapModifier;
import net.java.truevfs.kernel.spec.spi.FsDriverMapModifierTestSuite;

/**
 * @author Christian Schlichtherle
 */
public class TarDriverMapModifierTest
extends FsDriverMapModifierTestSuite {

    @Override
    protected String getExtensions() {
        return "tar";
    }

    @Override
    protected FsDriverMapModifier newModifier() {
        return new TarDriverMapModifier();
    }
}
