/*
 * Copyright (C) 2004-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.schlichtherle.truezip.fs.archive;

import de.schlichtherle.truezip.entry.Entry.Type;
import static de.schlichtherle.truezip.entry.Entry.Type.DIRECTORY;
import static de.schlichtherle.truezip.entry.EntryName.SEPARATOR;
import static de.schlichtherle.truezip.entry.EntryName.SEPARATOR_CHAR;
import static de.schlichtherle.truezip.io.Paths.cutTrailingSeparators;
import java.io.CharConversionException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import javax.annotation.concurrent.Immutable;

/**
 * An abstract base class of an archive driver which provides convenient
 * methods for dealing with the character set supported by a particular
 * archive type.
 * This class is intended to be used to implement archive types with a defined
 * character set, e.g. the ZIP file format with the IBM437 character set or
 * the TAR file format with the US-ASCII character set.
 * <p>
 * Subclasses must be thread-safe and should be immutable!
 * 
 * @param  <E> the type of the archive entries.
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class FsCharsetArchiveDriver<E extends FsArchiveEntry>
extends FsArchiveDriver<E> {

    private final Charset charset;

    /**
     * Constructs a new character set archive driver.
     *
     * @param charset the character set to use for encoding entry names and
     *        probably other meta data when writing an archive file.
     *        Depending on the archive file format, this may get used for
     *        reading an archive file, too.
     */
    protected FsCharsetArchiveDriver(final Charset charset) {
        if (null == (this.charset = charset))
            throw new NullPointerException();
    }

    /**
     * Returns the character set provided to the constructor.
     * Subsequent calls must return the same object.
     *
     * @return The character set to use for encoding entry names and
     *         probably other meta data when writing an archive file.
     *         Depending on the archive file format, this may get used for
     *         reading an archive file, too.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Fixes the given entry {@code name} so that it forms a valid entry name
     * for ZIP or TAR files by ensuring that the returned entry name ends with
     * the separator character {@code '/'} if and only if {@code type} is
     * {@code DIRECTORY}.
     *
     * @param  name an entry name.
     * @param  type an entry type.
     * @return The fixed entry name.
     */
    // TODO: Consider renaming this method to normalize().
    protected static String toZipOrTarEntryName(String name, Type type) {
        return DIRECTORY == type
                ? name.endsWith(SEPARATOR) ? name : name + SEPARATOR_CHAR
                : cutTrailingSeparators(name, SEPARATOR_CHAR);
    }

    /**
     * Ensures that the given entry name can be encoded by this driver's
     * character set.
     * Should be called by sub classes in their implementation of the method
     * {@link FsArchiveDriver#newEntry}.
     * 
     * @param  name an entry name.
     * @see    #getCharset
     * @throws CharConversionException If the path name contains characters
     *         which cannot get encoded.
     */
    // TODO: Consider naming this checkEncodable because of throws declaration.
    protected final void assertEncodable(String name)
    throws CharConversionException {
        if (!encoder.canEncode(name))
            throw new CharConversionException(name +
                    " (cannot encode all characters to " + getCharset() + ")");
    }

    private final ThreadLocalCharsetEncoder
            encoder = new ThreadLocalCharsetEncoder();

    private final class ThreadLocalCharsetEncoder
    extends ThreadLocal<CharsetEncoder> {
        @Override
        protected CharsetEncoder initialValue() {
            return getCharset().newEncoder();
        }

        boolean canEncode(CharSequence cs) {
            return get().canEncode(cs);
        }
    }

    /**
     * Returns a string representation of this object for debugging and logging
     * purposes.
     */
    @Override
    public String toString() {
        return String.format("%s[charset=%s, federated=%b, priority=%d]",
                getClass().getName(),
                getCharset(),
                isFederated(),
                getPriority());
    }
}
