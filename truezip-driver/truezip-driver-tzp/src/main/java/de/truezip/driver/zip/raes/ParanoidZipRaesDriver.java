/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.driver.zip.raes;

import de.schlichtherle.truezip.fs.FsModel;
import de.truezip.driver.zip.ZipDriverEntry;
import de.truezip.driver.zip.ZipInputService;
import de.truezip.driver.zip.ZipOutputService;
import de.truezip.kernel.key.KeyManagerProvider;
import de.schlichtherle.truezip.cio.IOPoolProvider;
import de.schlichtherle.truezip.cio.OutputService;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.concurrent.Immutable;

/**
 * A paranoid archive driver for RAES encrypted ZIP files.
 * This driver <em>always</em> checks the cipher text of input archive files
 * using the RAES Message Authentication Code (MAC), which makes it slower than
 * the {@link SafeZipRaesDriver} for archive files larger than 512 KB and
 * may pause the client application on the first access to the archive file
 * for a while if the file is large.
 * Note that the CRC-32 value of the plain text ZIP file is never checked
 * because this is made redundant by the MAC verification.
 * <p>
 * In addition, this driver limits the number of concurrent entry output
 * streams to one, so that writing unencrypted temporary files is inhibited.
 * <p>
 * Subclasses must be thread-safe and should be immutable!
 * 
 * @see    SafeZipRaesDriver
 * @author Christian Schlichtherle
 */
@Immutable
public class ParanoidZipRaesDriver extends ZipRaesDriver {

    public ParanoidZipRaesDriver(   IOPoolProvider ioPoolProvider,
                                    KeyManagerProvider keyManagerProvider) {
        super(ioPoolProvider, keyManagerProvider);
    }

    @Override
    public final long getAuthenticationTrigger() {
        return Long.MAX_VALUE;
    }

    /**
     * This implementation returns a new {@link ZipOutputService}.
     * This restricts the number of concurrent output entry streams to one in
     * order to inhibit writing unencrypted temporary files for buffering the
     * written entries.
     */
    @Override
    protected OutputService<ZipDriverEntry> newOutputService(
            FsModel model,
            OutputStream out,
            ZipInputService source)
    throws IOException {
        return new ZipOutputService(this, model, out, source);
    }
}
