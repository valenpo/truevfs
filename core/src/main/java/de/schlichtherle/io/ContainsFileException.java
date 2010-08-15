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

import java.io.FileNotFoundException;

/**
 * Thrown if two paths are referring to the same file or contain each other.
 * This exception is typically thrown from {@link File#cp}.
 *
 * @author Christian Schlichtherle
 * @version $Revision$
 * @since TrueZIP 6.4
 */
public class ContainsFileException extends FileNotFoundException {

    private final java.io.File ancestor, descendant;

    /**
     * Creates a new instance of <code>ContainsFileException</code>.
     */
    public ContainsFileException(
            final java.io.File ancestor,
            final java.io.File descendant) {
        super("Paths refer to the same file or contain each other!");
        this.ancestor = ancestor;
        this.descendant = descendant;
    }

    public java.io.File getAncestor() {
        return ancestor;
    }

    public java.io.File getDescendant() {
        return descendant;
    }
}
