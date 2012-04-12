/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.driver.tar;

import static de.truezip.kernel.FsAccessOption.CACHE;
import static de.truezip.kernel.FsAccessOption.COMPRESS;
import de.truezip.kernel.*;
import static de.truezip.kernel.cio.Entry.Access.WRITE;
import static de.truezip.kernel.cio.Entry.Size.DATA;
import de.truezip.kernel.cio.Entry.Type;
import de.truezip.kernel.cio.*;
import de.truezip.kernel.sl.IOPoolLocator;
import de.truezip.kernel.util.BitField;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.annotation.CheckForNull;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

/**
 * An archive driver for Tape Archive files (TAR).
 * By default, TAR files use the US-ASCII character set for the encoding of
 * entry names.
 * This configuration pretty much constraints the applicability of this file
 * format to North American countries.
 * <p>
 * Subclasses must be thread-safe and should be immutable!
 * 
 * @author Christian Schlichtherle
 */
@Immutable
public class TarDriver extends FsArchiveDriver<TarDriverEntry> {

    /**
     * The character set for entry names and comments, which is the default
     * character set.
     */
    public static final Charset TAR_CHARSET = Charset.defaultCharset();

    /**
     * {@inheritDoc}
     * 
     * @return {@link #TAR_CHARSET}.
     */
    @Override
    public Charset getCharset() {
        return TAR_CHARSET;
    }

    @Override
    public IOPool<?> getIOPool() {
        return IOPoolLocator.SINGLETON.getIOPool();
    }

    /**
     * {@inheritDoc}
     *
     * @return The implementation in the class {@link TarDriver} returns
     *         {@code true} because when reading a TAR file sequentially,
     *         each TAR entry should &quot;override&quot; any previously read
     *         TAR entry with an equal name.
     */
    @Override
    public boolean getRedundantContentSupport() {
        return true;
    }

    @Override
    protected InputService<TarDriverEntry> newInputService(
            final FsModel model,
            final InputSocket<?> input)
    throws IOException {
        return new TarInputService(model, input, this);
    }

    @Override
    protected OutputService<TarDriverEntry> newOutputService(
            final FsModel model,
            final OutputSocket<?> output,
            final @CheckForNull @WillNotClose InputService<TarDriverEntry> input)
    throws IOException {
        return new MultiplexingOutputService<>(
                getIOPool(), new TarOutputService(model, output, this));
    }

    /**
     * Clears {@link FsAccessOption#CACHE} in {@code options} before
     * forwarding the call to {@code controller}.
     */
    @Override
    protected InputSocket<?> getInputSocket(
            FsController<?> controller,
            FsEntryName name,
            BitField<FsAccessOption> options) {
        return controller.getInputSocket(name, options.clear(CACHE));
    }

    /**
     * Sets {@link FsAccessOption#COMPRESS} in {@code options} before
     * forwarding the call to {@code controller}.
     */
    @Override
    protected OutputSocket<?> getOutputSocket(
            FsController<?> controller,
            FsEntryName name,
            BitField<FsAccessOption> options) {
        return controller.getOutputSocket(name, options.set(COMPRESS), null);
    }

    @Override
    public TarDriverEntry newEntry(
            String name,
            final Type type,
            final BitField<FsAccessOption> mknod,
            final @CheckForNull Entry template) {
        name = normalize(name, type);
        final TarDriverEntry entry;
        if (template instanceof TarArchiveEntry) {
            entry = newEntry(name, (TarArchiveEntry) template);
        } else {
            entry = newEntry(name);
            if (null != template) {
                entry.setModTime(template.getTime(WRITE));
                entry.setSize(template.getSize(DATA));
            }
        }
        return entry;
    }

    public TarDriverEntry newEntry(String name) {
        return new TarDriverEntry(name);
    }

    public TarDriverEntry newEntry(String name, TarArchiveEntry template) {
        return new TarDriverEntry(name, template);
    }
}
