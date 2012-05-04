/*
 * Copyright (C) 2005-2012 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package de.truezip.driver.zip;

import de.truezip.kernel.FsDriver;
import de.truezip.kernel.FsScheme;
import de.truezip.kernel.spi.FsDriverService;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable container of a map of drivers for the ZIP file format.
 * The map provided by this service consists of the following entries:
 * <p>
<table border=1 cellpadding=5 summary="">
<thead>
<tr>
<th>URI Schemes / Archive File Extensions</th>
<th>File System Driver Class</th>
</tr>
</thead>
<tbody>
<tr>
<td>{@code ear}, {@code jar}, {@code war}</td>
<td>{@link de.truezip.driver.zip.JarDriver}</td>
</tr>
<tr>
<td>{@code exe}</td>
<td>{@link de.truezip.driver.zip.ReadOnlySfxDriver}</td>
</tr>
<tr>
<td>{@code odt}, {@code ott}, {@code odg}, {@code otg}, {@code odp}, {@code otp}, {@code ods}, {@code ots}, {@code odc}, {@code otc}, {@code odi}, {@code oti}, {@code odf}, {@code otf}, {@code odm}, {@code oth}, {@code odb}</td>
<td>{@link de.truezip.driver.zip.OdfDriver}</td>
</tr>
<tr>
<td>{@code zip}</td>
<td>{@link de.truezip.driver.zip.ZipDriver}</td>
</tr>
</tbody>
</table>
 * <p>
 * Note that the regular expression is actually decomposed into separate
 * {@link FsScheme} objects which getDrivers mapped individually.
 *
 * @see     <a href="http://docs.oasis-open.org/office/v1.2/OpenDocument-v1.2-part1.pdf">Open Document Format for Office Applications (OpenDocument) Version 1.2; Part 1: OpenDocument Schema; Appendix C: MIME Types and File Name Extensions (Non Normative)</a>
 * @author  Christian Schlichtherle
 */
@Immutable
public final class ZipDriverService extends FsDriverService {

    private final Map<FsScheme, FsDriver>
            drivers = newMap(new Object[][] {
                { "zip", new ZipDriver() },
                { "ear|jar|war", new JarDriver() },
                { "odt|ott|odg|otg|odp|otp|ods|ots|odc|otc|odi|oti|odf|otf|odm|oth|odb", new OdfDriver() },
                { "exe", new ReadOnlySfxDriver() },
            });

    @Override
    public Map<FsScheme, FsDriver> getDrivers() {
        return drivers;
    }

    /** @return -100 */
    @Override
    public int getPriority() {
        return -100;
    }
}
