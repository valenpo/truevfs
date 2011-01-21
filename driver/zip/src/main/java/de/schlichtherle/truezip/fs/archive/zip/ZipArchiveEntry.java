/*
 * Copyright (C) 2009-2011 Schlichtherle IT Services
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
package de.schlichtherle.truezip.fs.archive.zip;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import de.schlichtherle.truezip.fs.archive.FsArchiveEntry;
import de.schlichtherle.truezip.zip.DateTimeConverter;
import de.schlichtherle.truezip.zip.ZipEntry;
import edu.umd.cs.findbugs.annotations.NonNull;

import static de.schlichtherle.truezip.entry.Entry.Access.WRITE;
import static de.schlichtherle.truezip.entry.Entry.Size.DATA;
import static de.schlichtherle.truezip.entry.Entry.Type.DIRECTORY;
import static de.schlichtherle.truezip.entry.Entry.Type.FILE;

/**
 * An adapter class to make the {@link ZipEntry} class implement the
 * {@link FsArchiveEntry} interface.
 *
 * @see ZipDriver
 * @author Christian Schlichtherle
 * @version $Id$
 */
@DefaultAnnotation(NonNull.class)
public class ZipArchiveEntry extends ZipEntry implements FsArchiveEntry {
    static {
        assert ZipEntry.UNKNOWN == FsArchiveEntry.UNKNOWN;
    }

    ZipArchiveEntry(String name) {
        super(name);
    }

    ZipArchiveEntry(String name, ZipEntry template) {
        super(name, template);
    }

    @Override
    public Type getType() {
        return isDirectory() ? DIRECTORY : FILE;
    }

    @Override
    protected DateTimeConverter getDateTimeConverter() {
        return DateTimeConverter.ZIP;
    }

    @Override
    public long getSize(final Size type) {
        switch (type) {
            case DATA:
                return getSize();
            case STORAGE:
                return getCompressedSize();
            default:
                return FsArchiveEntry.UNKNOWN;
        }
    }

    @Override
    public boolean setSize(final Size type, final long size) {
        if (DATA != type)
            return false;
        setSize(size);
        return true;
    }

    @Override
    public long getTime(Access type) {
        if (WRITE != type)
            return FsArchiveEntry.UNKNOWN;
        long time = getTime();
        return 0 <= time ? time : FsArchiveEntry.UNKNOWN;
    }

    @Override
    public boolean setTime(Access type, long time) {
        if (WRITE != type)
            return false;
        setTime(time);
        return true;
    }
}
