/*
 * Copyright (C) 2006-2010 Schlichtherle IT Services
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

package de.schlichtherle.io;

/**
 * Like its super class, but indicates the existance of open output streams.
 *
 * @see <a href="package-summary.html#streams">Using Archive Entry Streams</a>
 * @see File#umount
 * @see File#update
 * @author Christian Schlichtherle
 * @version $Id$
 */
public class ArchiveOutputBusyException extends ArchiveBusyException {

    private final int numStreams;

    // TODO: Make this package private!
    public ArchiveOutputBusyException(
            ArchiveException priorException, String cPath, int numStreams) {
        super(priorException, cPath);
        this.numStreams = numStreams;
    }

    /**
     * Returns the number of open entry output streams, whereby an open stream
     * is a stream which's <code>close()</code> method hasn't been called.
     */
    public int getNumStreams() {
        return numStreams;
    }
}
