/*
 * Copyright (C) 2009-2010 Schlichtherle IT Services
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

package de.schlichtherle.truezip.io.archive.driver.zip;

import de.schlichtherle.truezip.io.archive.driver.ArchiveEntry;
import de.schlichtherle.truezip.io.zip.DateTimeConverter;

import static de.schlichtherle.truezip.io.socket.common.entry.CommonEntry.Type.DIRECTORY;
import static de.schlichtherle.truezip.io.socket.common.entry.CommonEntry.Type.FILE;

/**
 * An adapter class to make the {@link ZipEntry} class implement the
 * {@link ArchiveEntry} interface.
 *
 * @see ZipDriver
 * @author Christian Schlichtherle
 * @version $Id$
 */
public class ZipEntry
extends de.schlichtherle.truezip.io.zip.ZipEntry
implements ArchiveEntry {

    static {
        assert de.schlichtherle.truezip.io.zip.ZipEntry.UNKNOWN
                == ArchiveEntry.UNKNOWN;
    }

    ZipEntry(String entryName) {
        super(entryName);
    }

    ZipEntry(ZipEntry blueprint) {
        super(blueprint);
    }

    @Override
    protected void setName(String name) {
        super.setName(name);
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
    public long getTime(Access type) {
        if (Access.WRITE != type)
            return ArchiveEntry.UNKNOWN;
        long time = super.getTime();
        return 0 <= time ? time : ArchiveEntry.UNKNOWN;
    }

    @Override
    public void setTime(Access type, long value) {
        if (Access.WRITE == type)
            super.setTime(value);
    }
}
