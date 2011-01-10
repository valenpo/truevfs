/*
 * Copyright 2011 Schlichtherle IT Services
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
package de.schlichtherle.truezip.fs;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;

/**
 * A federating file system driver is a composite of a file system driver and
 * a file system driver provider which uses its map of file system drivers to
 * lookup the appropriate driver for the scheme of a given mount point.
 *
 * @author  Christian Schlichtherle
 * @version $Id$
 */
public class FsFederatingDriver implements FsDriver, FsDriverProvider {

    public static final FsFederatingDriver ALL
            = new FsFederatingDriver(FsClassPathDriverProvider.INSTANCE);

    private final Map<FsScheme, ? extends FsDriver> drivers;

    /** Will query the given provider for drivers. */
    public FsFederatingDriver(final @NonNull FsDriverProvider provider) {
        this.drivers = provider.getDrivers(); // immutable map!
        if (null == drivers)
            throw new NullPointerException("broken interface contract!");
    }

    /** Copy constructor. */
    protected FsFederatingDriver(final @NonNull FsFederatingDriver original) {
        this.drivers = original.drivers; // immutable map!
        assert null != drivers;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation in the class {@link FsFederatingDriver} queries the
     * {@link FsDriverProvider} provided to its constructor for the appropriate
     * {@link FsDriver} for the {@link FsScheme} of the given mount point.
     *
     * @throws NullPointerException if no appropriate driver is found for the
     *         scheme of the given mount point.
     */
    @Override
    public FsController<?>
    newController(FsMountPoint mountPoint, FsController<?> parent) {
        assert null == mountPoint.getParent()
                ? null == parent
                : mountPoint.getParent().equals(parent.getModel().getMountPoint());
        return drivers.get(mountPoint.getScheme()).newController(mountPoint, parent);
    }

    @Override
    public Map<FsScheme, ? extends FsDriver> getDrivers() {
        return drivers;
    }
}
