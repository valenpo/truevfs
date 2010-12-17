/*
 * Copyright (C) 2010 Schlichtherle IT Services
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
package de.schlichtherle.truezip.io.archive.driver.registry;

import de.schlichtherle.truezip.io.archive.driver.ArchiveDriver;
import de.schlichtherle.truezip.io.SuffixSet;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.schlichtherle.truezip.util.ClassLoaders.loadClass;

/**
 * The head of a chain of registries for archive file suffixes and archive
 * drivers.
 * Each element in the chain is an instance of this class which
 * maps single archive file suffixes (not lists) [{@code String}] to the
 * fully qualified class name, class object or instance of an archive driver
 * [{@code String}, {@code Class} or {@link ArchiveDriver}].
 * The {@link #getArchiveDriver} method can then be used to lookup an archive
 * driver for a given (file name) suffix in the chain.
 * <p>
 * This class is serializable in order to meet the requirements of some client
 * classes. However, it's not really recommended to serialize this class
 * because all associated archive drivers will be serialized too,
 * which is pretty inefficient.
 * 
 * @author Christian Schlichtherle
 * @version $Id$
 */
public class ArchiveDriverRegistry implements Serializable {

    private static final long serialVersionUID = 3445783613096128268L;

    private static final String CLASS_NAME
            = ArchiveDriverRegistry.class.getName();
    private static final ResourceBundle RESOURCES
            = ResourceBundle.getBundle(CLASS_NAME);
    private static final Logger LOGGER
            = Logger.getLogger(CLASS_NAME, CLASS_NAME);

    static final String KWD_DRIVER = "DRIVER";      // NOI18N
    static final String KWD_DEFAULT = "DEFAULT";    // NOI18N

    final Map<String, Object> drivers = new HashMap<String, Object>();

    /**
     * The parent archive driver registry is used to lookup archive drivers
     * when no archive driver is configured locally.
     * May be {@code null}.
     */
    private final ArchiveDriverRegistry parent;

    /** Creates an empty {@code ArchiveDriverRegistry}. */
    ArchiveDriverRegistry() {
        this.parent = null;
    }

    /**
     * Creates a new {@link ArchiveDriverRegistry} by decorating the
     * configuration of {@code parent} with mappings for all entries in
     * {@code config}.
     * 
     * @param  parent the {@code ArchiveDriverRegistry} which's
     *         configuration is to be virtually inherited.
     * @param  config a map of suffix lists and archive drivers.
     *         Each key in this map must be a non-{@code null}, non-empty
     *         archive file suffix list, obeying the usual syntax.
     *         Each value must either be an archive driver instance, an archive
     *         driver class, a string with the fully qualified name name of
     *         an archive driver class, or {@code null}.
     *         A {@code null} archive driver may be used to shadow a
     *         mapping for the same archive driver in {@code delegate},
     *         effectively removing it.
     * @throws NullPointerException if any parameter or configuration element
     *         other than an archive driver is {@code null}.
     * @throws IllegalArgumentException if any other parameter precondition
     *         does not hold or an illegal keyword is found in the
     *         configuration.
     * @see    SuffixSet Syntax Definition for Suffix Lists
     */
    public ArchiveDriverRegistry(
            final ArchiveDriverRegistry parent,
            final Map<String, ?> config) {
        if (parent == null)
            throw new NullPointerException(getString("null", "delegate")); // NOI18N
        this.parent = parent;
        registerArchiveDrivers(config, true);
    }

    /**
     * Processes the given {@code config} and adds the configured archive
     * driver mappings.
     * 
     * @param  config a map of suffix lists and archive driver IDs.
     *         Each key in this map must be a non-null, non-empty suffix list,
     *         obeying the usual syntax.
     *         Each value must either be an archive driver instance, an archive
     *         driver class, or a string with the fully qualified name name of
     *         an archive driver class.
     * @param  eager iff {@code true}, archive drivers are immediately
     *         instantiated and the keyword {@code DEFAULT} is not allowed.
     * @throws NullPointerException if any parameter or config element
     *         is {@code null}.
     * @throws IllegalArgumentException if any other parameter precondition
     *         does not hold or the keyword {@code DRIVER} is found.
     */
    final void registerArchiveDrivers(	final Map<String, ?> config,
    									final boolean eager) {
        for (final Map.Entry<String, ?> entry : config.entrySet()) {
            final String key = entry.getKey();
            if (KWD_DRIVER.equals(key))
                throw new IllegalArgumentException(
                        getString("keyword", KWD_DRIVER)); // NOI18N
            final Object value = entry.getValue();
            if (KWD_DEFAULT.equals(key)) {
                if (eager)
                    throw new IllegalArgumentException(
                            getString("keyword", KWD_DEFAULT)); // NOI18N
                final SuffixSet set = (SuffixSet) drivers.get(key);
                if (set != null)
                    set.addAll((String) value);
                else
                    drivers.put(key, new SuffixSet((String) value));
            } else {
                registerArchiveDriver(key, value, eager);
            }
        }
    }

    /**
     * Registers the given archive {@code id} for the given
     * list of {@code suffixes}.
     * 
     * @param  eager Whether the archive driver shall get instantiated now or
     *         later.
     * @throws NullPointerException If {@code id} is {@code null}.
     * @throws ClassCastException If {@code eager} is {@code false}
     *         and {@code driver} isn't a string.
     * @throws IllegalArchiveDriverException If {@code eager} is
     *         {@code true} and {@code driver} can't get instantiated
     *         for some reason.
     *         The cause is wrapped in the exception.
     */
    private void registerArchiveDriver(
            final String list,
            Object driver,
            final boolean eager) {
        if (list == null)
            throw new NullPointerException(getString("noSuffixes")); // NOI18N
        final SuffixSet set = new SuffixSet(list);
        if (set.isEmpty()) {
            if (eager)
                throw new IllegalArgumentException(getString("noSuffixes")); // NOI18N
            else
                LOGGER.log(Level.WARNING, "noSuffixes"); // NOI18N
        } else {
            driver = eager ? newArchiveDriver(driver) : (String) driver; // force cast
            for (String suffix : set)
                drivers.put(suffix, driver);
        }
    }

    /**
     * Returns the archive driver for the given canonical {@code suffix}
     * or {@code null} if no archive driver is found in the registry.
     * <p>
     * This instance is the head element of the registry chain.
     * If this head element does not hold an archive driver for the given
     * suffix, then the next element in the registry chain (i.e. its parent)
     * is searched.
     * This repeats recursively until either an archive driver is found or
     * the end of the chain is reached.
     * <p>
     * An archive driver is looked up in the registry as follows:
     * <ol>
     * <li>If the registry holds a string, it's supposed to be the fully
     *     qualified class name of an {@code ArchiveDriver}
     *     implementation. The class will be loaded and stored in the registry.
     * <li>If the registry then holds a class instance, it's instantiated
     *     with its no-arguments constructor, cast to the
     *     {@code ArchiveDriver} type and stored in the registry.
     * <li>If the registry then holds an instance of an
     *     {@code ArchiveDriver} implementation, it's returned.
     * <li>Otherwise, {@code null} is returned.
     * </ol>
     * This method is thread safe.
     *
     * @throws RuntimeException A subclass is thrown if loading or
     *         instantiating an archive driver class fails.
     */
    public final synchronized ArchiveDriver<?> getArchiveDriver(
            final String suffix) {
        // Lookup driver locally.
        Object driver = drivers.get(suffix);
        if (!(driver instanceof ArchiveDriver<?>)) {
            if (null == driver) {
                if (drivers.containsKey(suffix) || null == parent)
                    return null;
                // Lookup the driver in the delegate.
                driver = parent.getArchiveDriver(suffix); // may be null!
            } else {
                // We have found an entry in the drivers map, but it isn't
                // an ArchiveDriver, so we probably need to load its class
                // first and instantiate it.
                driver = newArchiveDriver(driver);
                LOGGER.log(Level.FINE, "installed", // NOI18N
                        new Object[] { suffix, driver });
            }
            // Cache the driver in the local registry.
            drivers.put(suffix, driver); // may drivers null!
        }
        return (ArchiveDriver<?>) driver;
    }

    /**
     * Creates a new archive driver from the given blueprint.
     *
     * @param driver A string with the fully qualified class name of an archive
     *        driver implementation, a class instance for an archive driver
     *        implementation, an archive driver instance or {@code null}.
     * @return An archive driver instance or {@code null} iff
     *         {@code driver} is {@code null}.
     * @throws IllegalArgumentException If an archive driver cannot get
     *         returned. Its cause provides more detail.
     */
    @SuppressWarnings("unchecked")
    private static ArchiveDriver<?> newArchiveDriver(Object driver) {
        try {
            if (driver instanceof String)
                driver = loadClass((String) driver, ArchiveDriverRegistry.class);
            if (driver instanceof Class<?>)
                driver = ((Class<? extends ArchiveDriver<?>>) driver).newInstance();
            return (ArchiveDriver<?>) driver; // may throw ClassCastException
        } catch (Exception ex) {
            throw new IllegalArgumentException(getString("cannotCreate"), ex); // NOI18N
        }
    }

    /**
     * Returns a new set of all suffixes which map to a valid archive driver in
     * this registry.
     * This includes the drivers found in the entire registry chain, not just
     * this registry object.
     */
    public final SuffixSet getSuffixes() {
        return decorate(null != parent ? parent.getSuffixes() : new SuffixSet());
    }

    /**
     * Decorates the given suffix set by adding all suffixes which map to a
     * valid archive driver and removing all suffixes which map to
     * {@code null} in the local registry.
     *
     * @param set A non-null set of canonical archive file suffixes.
     * @return {@code set}, decorated according to the mappings in the
     *         local registry.
     */
    public final SuffixSet decorate(final SuffixSet set) {
        for (final String suffix : drivers.keySet()) {
            if (null != drivers.get(suffix))
                set.addAll(suffix);
            else
                set.removeAll(suffix);
        }
        return set;
    }

    private static String getString(String key) {
        return RESOURCES.getString(key);
    }

    private static String getString(String key, String arg) {
        return MessageFormat.format(RESOURCES.getString(key),
                                    (Object[]) new String[] { arg });
    }
}
