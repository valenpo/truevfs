/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.key;

import java.util.ServiceConfigurationError;

/**
 * A provider for key managers for secret key classes.
 *
 * @author Christian Schlichtherle
 */
public interface KeyManagerProvider {

    /**
     * Returns the key manager for the given secret key class.
     * <p>
     * This is an immutable indexed property - multiple calls must return the
     * same object for the same secret key class.
     *
     * @param  <K> the type of the secret keys.
     * @param  type the class for the secret key type.
     * @return the key manager for the given secret key class.
     * @throws ServiceConfigurationError if no appropriate key manager is
     *         available.
     */
    <K> KeyManager<K> getKeyManager(Class<K> type);
}
