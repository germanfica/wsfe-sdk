package com.germanfica.wsfe.util;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;

import java.io.IOException;
import java.util.Map;

/**
 * Utility helpers for dealing with X.500 principals and ARCA‑specific DN quirks.
 * <p>
 * ✦ {@link #decodeRdnValue(Object)} – Converts an {@code RDN} value (DER‑encoded byte[] or String)
 *   into a human‑readable {@link String}.
 * <br>
 * ✦ {@link #normalizeType(String)} – Maps numeric OIDs (e.g. {@code 2.5.4.3}) to their familiar
 *   short names (e.g. {@code CN}) so you never have to remember the numbers again.
 */
public class X500Utils {
    private X500Utils() { /* no‑instances */ }

    // ---------------------------------------------------------------------
    // OID - Friendly‑name mapping (RFC 4519 & common practice)
    // ---------------------------------------------------------------------
    private static final Map<String, String> OID_MAP = Map.ofEntries(
        Map.entry("2.5.4.3", "CN"),              // commonName
        Map.entry("2.5.4.4", "SN"),              // surname
        Map.entry("2.5.4.5", "serialNumber"),
        Map.entry("2.5.4.6", "C"),               // countryName
        Map.entry("2.5.4.7", "L"),               // localityName
        Map.entry("2.5.4.8", "ST"),              // stateOrProvinceName
        Map.entry("2.5.4.9", "STREET"),          // streetAddress
        Map.entry("2.5.4.10", "O"),              // organizationName
        Map.entry("2.5.4.11", "OU"),             // organizationalUnitName
        Map.entry("2.5.4.12", "title"),
        Map.entry("2.5.4.13", "description"),
        Map.entry("2.5.4.15", "businessCategory"),
        Map.entry("2.5.4.16", "postalAddress"),
        Map.entry("2.5.4.17", "postalCode"),
        Map.entry("2.5.4.18", "postOfficeBox"),
        Map.entry("2.5.4.20", "telephoneNumber"),
        Map.entry("2.5.4.42", "givenName"),
        Map.entry("2.5.4.43", "initials"),
        Map.entry("2.5.4.46", "dnQualifier"),
        Map.entry("2.5.4.65", "pseudonym")
    );

    /**
     * Map an RDN type to a friendly short name. If the type is already a friendly
     * name (e.g. {@code CN}) or an unknown OID, the original value is returned.
     *
     * @param type value returned by {@link Rdn#getType()}
     * @return canonical short name (e.g. {@code CN}, {@code O}, {@code serialNumber})
     */
    public static String normalizeType(String type) {
        return OID_MAP.getOrDefault(type, type);
    }

    /**
     * Decode an RDN value that might be either {@code byte[]} (DER‑encoded ASN.1)
     * or a plain {@code String}.
     *
     * @param rawValue the value returned by {@link Rdn#getValue()}
     * @return human‑readable string representation
     */
    public static String decodeRdnValue(Object rawValue) {
        if (rawValue instanceof byte[]) {
            byte[] bytes = (byte[]) rawValue;
            try {
                ASN1Primitive asn1 = ASN1Primitive.fromByteArray(bytes);
                if (asn1 instanceof ASN1String) {
                    return ((ASN1String) asn1).getString();
                }
                throw new IllegalStateException("RDN value is not an ASN1String: " + asn1.getClass());
            } catch (IOException e) {
                throw new RuntimeException("Unable to decode ASN.1 RDN value", e);
            }
        }
        return rawValue.toString();
    }
}
