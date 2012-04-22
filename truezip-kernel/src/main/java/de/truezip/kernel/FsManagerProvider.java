/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.kernel;

/**
 * A provider for the singleton file system manager.
 *
 * @author Christian Schlichtherle
 */
public interface FsManagerProvider {

    /**
     * Returns the singleton file system manager.
     * <p>
     * This is an immutable property - multiple calls must return the same
     * object.
     *
     * @return The file system manager.
     */
    FsManager getManager();
}