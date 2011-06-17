/*
 * Copyright (C) 2007-2011 Schlichtherle IT Services
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
package de.schlichtherle.truezip.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import net.jcip.annotations.NotThreadSafe;

import static java.nio.charset.CoderResult.*;

/**
 * Encodes and decodes illegal characters in URI components according to
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>
 * and its updates in
 * <a href="http://www.ietf.org/rfc/rfc2732.txt">RFC&nbsp;2732</a>
 * for IPv6 addresses.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">
 *      RFC&nbsp;2396: Uniform Resource Identifiers (URI): Generic Syntax</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2732.txt">
 *      RFC&nbsp;2732: Format for Literal IPv6 Addresses in URL's</a>
 * @see UriBuilder
 * @see UriDecoder
 * @author Christian Schlichtherle
 * @version $Id$
 */
@DefaultAnnotation(NonNull.class)
@NotThreadSafe
public final class UriEncoder {

    /** The default character set. */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final String
            ALPHANUM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String MARK_CHARS = "-_.!~*'()";
    private static final String
            DEFAULT_LEGAL_CHARS = ALPHANUM_CHARS + MARK_CHARS + ",;$&+=@";

    private final @CheckForNull CharsetEncoder encoder;
    private final boolean quotePercent;
    private @CheckForNull StringBuilder stringBuilder;

    /**
     * Constructs a new URI codec which uses the UTF-8 character set to encode
     * non-US-ASCII characters.
     * Equivalent to {@link #UriEncoder(Charset, boolean) UriEncoder(UTF8, false)}.
     */
    public UriEncoder() {
        this(UTF8, false);
    }

    /**
     * Constructs a new URI codec which uses the given character set to encode
     * non-US-ASCII characters.
     * Equivalent to {@link #UriEncoder(Charset, boolean) UriEncoder(charset, false)}.
     */
    public UriEncoder(final @CheckForNull Charset charset) {
        this(charset, false);
    }

    /**
     * Constructs a new URI codec which uses the given character set to encode
     * non-US-ASCII characters.
     * <p>
     * 
     * @param charset the character set to use for encoding non-US-ASCII
     *        characters.
     *        If this is {@code null}, then this codec preserves non-US-ASCII
     *        characters.
     *        Note that using any other character set than UTF-8 should void
     *        interoperability with most applications!
     * @param raw If {@code true}, then the {@code '%'} character doesn't get
     *        quoted.
     */
    public UriEncoder(final @CheckForNull Charset charset, final boolean raw) {
        this.encoder = null == charset ? null : charset.newEncoder();
        this.quotePercent = !raw;
    }

    private static void quote(final char dc, final StringBuilder eS) {
        quote(UTF8.encode(CharBuffer.wrap(Character.toString(dc))), eS);
    }

    private static void quote(final ByteBuffer eB, final StringBuilder eS) {
        while (eB.hasRemaining()) {
            final byte eb = eB.get();
            eS.append('%');
            eS.append(HEX[(eb >> 4) & 0xf]);
            eS.append(HEX[ eb       & 0xf]);
        }
    }

    /**
     * Encodes all characters in the string {@code dS} which are illegal within
     * the URI component {@code comp}.
     * <p>
     * Note that calling this method on an already encoded string escapes any
     * escape sequences again, that is, each occurence of the character
     * {@code '%'} is substituted with the string {@code "%25"} again.
     * 
     * @param  dS the decoded string to encode.
     * @param  comp the URI component to encode.
     * @return The encoded string.
     * @throws IllegalArgumentException on any encoding error with a
     *         {@link URISyntaxException} as its
     *         {@link IllegalArgumentException#getCause() cause}.
     *         This exception should never occur if the character set of this
     *         codec is UTF-8.
     */
    public String encode(String dS, Encoding comp) {
        try {
            StringBuilder eS = encode(dS, comp, null);
            return null != eS ? eS.toString() : dS;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Encodes all characters in the string {@code dS} which are illegal within
     * the URI component {@code comp} to the string builder {@code eS}.
     * <p>
     * Note that calling this method on an already encoded string escapes
     * any escape sequences again, that is, each occurence of the character
     * {@code '%'} is substituted with the string {@code "%25"} again.
     * 
     * @param  dS the decoded string to encode.
     * @param  comp the URI component to encode.
     * @param  eS the string builder to which all encoded characters shall get
     *         appended.
     * @return If {@code dS} contains only legal characters for the URI
     *         component {@code comp}, then {@code null} gets returned.
     *         Otherwise, if {@code eS} is not {@code null}, then it gets
     *         returned with all encoded characters appended to it.
     *         Otherwise, a temporary string builder gets returned which solely
     *         contains all encoded characters.
     *         This temporary string builder may get cleared and reused upon
     *         the next call to <em>any</em> method of this object.
     * @throws URISyntaxException on any encoding error.
     *         This exception should never occur if the character set of this
     *         codec is UTF-8.
     *         If it occurs however, {@code eS} is left in an undefined state.
     */
    public @CheckForNull StringBuilder encode(
            final String dS,
            final Encoding comp,
            @CheckForNull StringBuilder eS)         // encoded String
    throws URISyntaxException {
        final String[] escapes = comp.escapes;
        final CharBuffer dC = CharBuffer.wrap(dS);  // decoded characters
        ByteBuffer eB = null;                       // encoded bytes
        final CharsetEncoder enc = encoder;
        while (dC.hasRemaining()) {
            dC.mark();
            final char dc = dC.get();               // decoded character
            if (dc < 0x80) {
                final String es = escapes[dc];      // escape sequence
                if (null != es && (quotePercent || '%' != dc)) {
                    if (null == eB) {
                        if (null == eS) {
                            if (null == (eS = stringBuilder))
                                eS = stringBuilder = new StringBuilder();
                            else
                                eS.setLength(0);
                            eS.append(dS, 0, dC.position() - 1); // prefix until current character
                        }
                        eB = ByteBuffer.allocate(3);
                    }
                    eS.append(es);
                }  else if (eS != null) {
                    eS.append(dc);
                }
            } else if (null != enc) {
                if (eB == null) {
                    if (eS == null) {
                        if (null == (eS = stringBuilder))
                            eS = stringBuilder = new StringBuilder();
                        else
                            eS.setLength(0);
                        eS.append(dS, 0, dC.position() - 1); // prefix until current character
                    }
                    eB = ByteBuffer.allocate(3);
                }
                final int p = dC.position();
                dC.reset();
                dC.limit(p);
                { // Encode dC -> eB.
                    CoderResult cr;
                    if (UNDERFLOW != (cr = enc.reset().encode(dC, eB, true))
                            || UNDERFLOW != (cr = enc.flush(eB))) {
                        assert OVERFLOW != cr;
                        throw new QuotedInputUriSyntaxException(dS, cr.toString());
                    }
                }
                eB.flip();
                quote(eB, eS);
                eB.clear();
                dC.limit(dC.capacity());
            } else if (null != eS) {
                eS.append(dc);
            }
        }
        return null == eB ? null : eS;
    }

    /**
     * Defines the escape sequences for illegal characters in various URI
     * components.
     */
    public enum Encoding {
        /**
         * Encoding which can be safely used for any URI component, except the
         * URI scheme component which does not allow escape sequences.
         * This encoding may produce redundant escape sequences, however.
         */
        ANY(DEFAULT_LEGAL_CHARS),
        
        /** Encoding for exclusive use with the URI authority component. */
        AUTHORITY(DEFAULT_LEGAL_CHARS + ":[]"),

        /**
         * Encoding for exclusive use with the URI path component
         * where the path may contain arbitrary characters.
         * This encoding may produce redundant escape sequences for absolute
         * paths, however.
         * 
         * @see #ABSOLUTE_PATH
         */
        PATH(DEFAULT_LEGAL_CHARS + "/"),

        /**
         * Encoding for exclusive use with the URI path component
         * where the path starts with the separator character {@code '/'}.
         * 
         * @see #PATH
         */
        ABSOLUTE_PATH(DEFAULT_LEGAL_CHARS + ":/"),

        /** Encoding for exclusive use with the URI query component. */
        QUERY(DEFAULT_LEGAL_CHARS + ":/?"),

        /** Encoding for exclusive use with the URI fragment component. */
        FRAGMENT(DEFAULT_LEGAL_CHARS + ":/?");

        private final String[] escapes = new String[0x80];

        private Encoding(final String legal) {
            // Populate table of getEscapeSequence sequences.
            final StringBuilder sb = new StringBuilder();
            for (char c = 0; c < 0x80; c++) {
                if (legal.indexOf(c) >= 0)
                    continue;
                sb.setLength(0);
                quote(c, sb);
                escapes[c] = sb.toString();
            }
        }
    }
}
