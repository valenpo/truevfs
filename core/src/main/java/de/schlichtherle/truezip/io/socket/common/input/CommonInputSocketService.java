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

package de.schlichtherle.truezip.io.socket.common.input;

import de.schlichtherle.truezip.io.socket.common.entry.CommonEntryContainer;
import de.schlichtherle.truezip.io.socket.common.output.CommonOutputSocketService;
import de.schlichtherle.truezip.io.socket.common.entry.CommonEntry;
import java.io.Closeable;

/**
 * A common entry container which provides common entry input sockets.
 * <p>
 * Implementations do <em>not</em> need to be thread-safe:
 * Multithreading needs to be addressed by client classes.
 *
 * @param   <CE> The type of the common entries.
 * @see     CommonOutputSocketService
 * @author  Christian Schlichtherle
 * @version $Id$
 */
// TODO: Move Closeable to ArchiveInput!
public interface CommonInputSocketService<CE extends CommonEntry>
extends Closeable,
        CommonEntryContainer<CE>,
        CommonInputSocketProvider<CE> {
}
