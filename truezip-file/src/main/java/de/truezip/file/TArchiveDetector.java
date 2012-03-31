/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.file;

import de.truezip.kernel.fs.FsAbstractCompositeDriver;
import de.truezip.kernel.fs.FsAbstractDriverProvider;
import de.truezip.kernel.fs.FsDriver;
import de.truezip.kernel.fs.FsDriverProvider;
import de.truezip.kernel.addr.FsScheme;
import de.truezip.kernel.sl.FsDriverLocator;
import static de.truezip.kernel.util.Maps.initialCapacity;
import de.truezip.kernel.util.SuffixSet;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

/**
 * Detects a <em>prospective</em> archive file and declares its file system
 * type by mapping its file name suffix to an archive driver.
 * Note that this class does <em>not</em> access any file system!
  * <p>
 * The map of detectable archive file name suffixes and corresponding archive
 * drivers is configured by the constructors of this class.
 * There are two types of constructors available:
 * <ol>
 * <li>Constructors which filter the driver map of a given file system driver
 *     provider by a given list of file name suffixes.
 *     For example, the driver map of the provider
 *     {@link FsDriverLocator#SINGLETON} could be filtered by the file name
 *     suffix list {@code "tar|zip"} in order to recognize only TAR and ZIP
 *     files.
 * <li>Constructors which decorate a given file system driver provider with a
 *     given map of file system schemes to file system drivers.
 *     This can get used to specify custom archive file name suffixes or
 *     archive drivers.
 *     For example, the file name suffix list {@code "foo|bar"} could be used
 *     to detect a custom variant of the JAR file format (you need to provide
 *     a custom archive driver then, too).
 * </ol>
 * <p>
 * Where a constructor expects a list of file name suffixes as a parameter,
 * it must obeye the syntax constraints for {@link SuffixSet}s.
 * As an example, the parameter {@code "zip|jar"} would cause
 * the archive detector to recognize ZIP and JAR files in a path.
 * The same would be true for {@code "||.ZiP||.JaR||ZIP||JAR||"},
 * but this notation is discouraged because it's not in canonical form.
 * 
 * @author Christian Schlichtherle
 */
@Immutable
public final class TArchiveDetector extends FsAbstractCompositeDriver {

    /**
     * This instance never recognizes any archive files in a path.
     * This can get used as the end of a chain of
     * {@code TArchiveDetector} instances or if archive files
     * shall be treated like regular files rather than (virtual) directories.
     */
    public static final TArchiveDetector NULL = new TArchiveDetector("");

    /**
     * This instance recognizes all archive types for which an archive driver
     * can be found by the file system driver service locator singleton
     * {@link FsDriverLocator#SINGLETON}.
     */
    public static final TArchiveDetector ALL = new TArchiveDetector(null);

    private final Map<FsScheme, FsDriver> drivers;

    /**
     * The canonical string respresentation of the set of suffixes recognized
     * by this archive detector.
     * This set is used to filter the registered archive file suffixes in
     * {@link #drivers}.
     */
    private final String suffixes;

    /**
     * Equivalent to
     * {@link #TArchiveDetector(FsDriverProvider, String)
     * TArchiveDetector(FsDriverLocator.SINGLETON, suffixes)}.
     */
    public TArchiveDetector(@CheckForNull String suffixes) {
        this(FsDriverLocator.SINGLETON, suffixes);
    }

    /**
     * Constructs a new {@code TArchiveDetector} by filtering the given driver
     * provider for all canonicalized suffixes in the {@code suffixes} list.
     *
     * @param  provider the file system driver provider to filter.
     * @param  suffixes A list of file name suffixes which shall identify
     *         prospective archive files.
     *         If this is {@code null}, no filtering is applied and all drivers
     *         known by the given provider are available for use with this
     *         archive detector.
     * @throws IllegalArgumentException If any of the suffixes in the list
     *         names a suffix for which no file system driver is known by the
     *         provider.
     * @see    SuffixSet Syntax constraints for suffix lists.
     */
    public TArchiveDetector(final FsDriverProvider provider,
                            final @CheckForNull String suffixes) {
        final Map<FsScheme, FsDriver> inDrivers = provider.get();
        final SuffixSet inSuffixes;
        final Map<FsScheme, FsDriver> outDrivers;
        if (null != suffixes) {
            inSuffixes = new SuffixSet(suffixes);
            outDrivers = new HashMap<FsScheme, FsDriver>(
                    initialCapacity(inDrivers.size()));
        } else {
            inSuffixes = null;
            outDrivers = inDrivers;
        }
        final SuffixSet outSuffixes = new SuffixSet();
        for (final Map.Entry<FsScheme, FsDriver> entry : inDrivers.entrySet()) {
            final FsDriver driver = entry.getValue();
            assert null != driver;
            /*if (null == driver)
                continue;*/
            final FsScheme scheme = entry.getKey();
            final boolean federated = driver.isFederated();
            if (null != inSuffixes) {
                final boolean accepted = inSuffixes.contains(scheme.toString());
                if (!federated || accepted)
                    outDrivers.put(scheme, driver);
                if (federated && accepted)
                    outSuffixes.add(scheme.toString());
            } else {
                if (federated)
                    outSuffixes.add(scheme.toString());
            }
        }
        if (null != inSuffixes) {
            inSuffixes.removeAll(outSuffixes);
            if (!inSuffixes.isEmpty())
                throw new IllegalArgumentException(
                        "\"" + inSuffixes + "\" (no archive driver installed for these suffixes)");
        }
        this.drivers = Collections.unmodifiableMap(outDrivers);
        this.suffixes = outSuffixes.toString();
    }

    /**
     * Equivalent to
     * {@link #TArchiveDetector(FsDriverProvider, String, FsDriver)
     * TArchiveDetector(TArchiveDetector.NULL, suffixes, driver)}.
     */
    public TArchiveDetector(String suffixes, @CheckForNull FsDriver driver) {
        this(NULL, suffixes, driver);
    }

    /**
     * Constructs a new {@code TArchiveDetector} by
     * decorating the configuration of {@code delegate} with
     * mappings for all canonicalized suffixes in {@code suffixes} to
     * {@code driver}.
     * 
     * @param  delegate the file system driver provider to decorate.
     * @param  suffixes A list of file name suffixes which shall identify
     *         prospective archive files.
     *         This must not be {@code null} and must not be empty.
     * @param  driver the file system driver to map for the suffix list.
     *         {@code null} may be used to <i>shadow</i> a mapping for an equal
     *         file system scheme in {@code delegate} by removing it from the
     *         resulting map for this detector.
     * @throws NullPointerException if a required configuration element is
     *         {@code null}.
     * @throws IllegalArgumentException if any other parameter precondition
     *         does not hold.
     * @see    SuffixSet Syntax contraints for suffix lists.
     */
    public TArchiveDetector(FsDriverProvider delegate,
                            String suffixes,
                            @CheckForNull FsDriver driver) {
        this(delegate, new Object[][] {{ suffixes, driver }});
    }

    /**
     * Creates a new {@code TArchiveDetector} by
     * decorating the configuration of {@code delegate} with
     * mappings for all entries in {@code config}.
     * 
     * @param  delegate the file system driver provider to decorate.
     * @param  config an array of key-value pair arrays.
     *         The first element of each inner array must either be a
     *         {@link FsScheme file system scheme}, an object {@code o} which
     *         can get converted to a set of file name suffixes by calling
     *         {@link SuffixSet#SuffixSet(String) new SuffixSet(o.toString())}
     *         or a {@link Collection collection} of these.
     *         The second element of each inner array must either be a
     *         {@link FsDriver file system driver object}, a
     *         {@link Class file system driver class}, a
     *         {@link String fully qualified name of a file system driver class},
     *         or {@code null}.
     *         {@code null} may be used to <i>shadow</i> a mapping for an equal
     *         file system scheme in {@code delegate} by removing it from the
     *         resulting map for this detector.
     * @throws NullPointerException if a required configuration element is
     *         {@code null}.
     * @throws IllegalArgumentException if any other parameter precondition
     *         does not hold.
     * @see    SuffixSet Syntax contraints for suffix lists.
     */
    public TArchiveDetector(FsDriverProvider delegate, Object[][] config) {
        this(delegate, FsAbstractDriverProvider.newMap(config));
    }

    /**
     * Constructs a new {@code TArchiveDetector} by decorating the given driver
     * provider with mappings for all entries in {@code config}.
     * 
     * @param  provider the file system driver provider to decorate.
     * @param  config a map of file system schemes to file system drivers.
     *         {@code null} may be used to <i>shadow</i> a mapping for an equal
     *         file system scheme in {@code provider} by removing it from the
     *         resulting map for this detector.
     * @throws NullPointerException if a required configuration element is
     *         {@code null}.
     * @throws ClassCastException if a configuration element is of the wrong
     *         type.
     * @throws IllegalArgumentException if any other parameter precondition
     *         does not hold.
     * @see    SuffixSet Syntax contraints for suffix lists.
     */
    public TArchiveDetector(final FsDriverProvider provider,
                            final Map<FsScheme, FsDriver> config) {
        final Map<FsScheme, FsDriver> inDrivers = provider.get();
        final Map<FsScheme, FsDriver> 
                outDrivers = new HashMap<FsScheme, FsDriver>(
                        initialCapacity(inDrivers.size()));
        final SuffixSet outSuffixes = new SuffixSet();
        for (final Map.Entry<FsScheme, FsDriver> entry : inDrivers.entrySet()) {
            final FsDriver driver = entry.getValue();
            assert null != driver;
            /*if (null == driver)
                continue;*/
            final FsScheme scheme = entry.getKey();
            outDrivers.put(scheme, driver);
            if (driver.isFederated())
                outSuffixes.add(scheme.toString());
        }
        for (final Map.Entry<FsScheme, FsDriver> entry : config.entrySet()) {
            final FsScheme scheme = entry.getKey();
            final FsDriver driver = entry.getValue();
            if (null != driver) {
                outDrivers.put(scheme, driver);
                outSuffixes.add(scheme.toString());
            } else {
                outDrivers.remove(scheme);
                outSuffixes.remove(scheme.toString());
            }
        }
        this.drivers = Collections.unmodifiableMap(outDrivers);
        this.suffixes = outSuffixes.toString();
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Map<FsScheme, FsDriver> get() {
        return drivers;
    }

    /**
     * Detects whether the given {@code path} name identifies a prospective
     * archive file by matching its file name suffixes against the set of file
     * system schemes in the archive driver map.
     * If a match is found, the file name suffix gets converted to a file
     * system scheme and returned.
     * Otherwise, {@code null} is returned.
     *
     * @param  path the path name.
     * @return A file system scheme to declare the file system type of the
     *         prospective archive file or {@code null} if no archive file name
     *         suffix has been detected.
     */
    public @CheckForNull FsScheme getScheme(String path) {
        // An archive file name suffix may contain a dot (e.g. "tar.gz"), so we
        // can't just look for the last dot in the file name and look up the
        // remainder in the key set of the archive driver map.
        // Likewise, a file name may contain additional dots, so we can't just
        // look for the first dot in it and look up the remainder ...
        path = path.replace('/', File.separatorChar);
        int i = path.lastIndexOf(File.separatorChar) + 1;
        path = path.substring(i);//.toLowerCase(Locale.ENGLISH);
        final int l = path.length();
        FsScheme scheme;
        for (i = 0; 0 < (i = path.indexOf('.', i) + 1) && i < l ;) {
            try {
                scheme = new FsScheme(path.substring(i));
            } catch (URISyntaxException noSchemeNoArchiveBadLuck) {
                continue; // TODO: http://java.net/jira/browse/TRUEZIP-132
            }
            final FsDriver driver = drivers.get(scheme);
            if (null != driver && driver.isFederated())
                return scheme;
        }
        return null;
    }

    /**
     * Returns the <i>canonical suffix list</i> for all federated file system
     * types recognized by this {@code TArchiveDetector}.
     *
     * @return Either {@code ""} to indicate an empty set or
     *         a string of the form {@code "suffix[|suffix]*"},
     *         where {@code suffix} is a combination of lower case
     *         letters which does <em>not</em> start with a dot.
     *         The string never contains empty or duplicated suffixes and the
     *         suffixes are sorted in natural order.
     * @see    #TArchiveDetector(String)
     * @see    SuffixSet Syntax constraints for suffix lists.
     */
    @Override
    public String toString() {
        return suffixes;
    }
}
