package com.germanfica.wsfe.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping common ASN.1 OIDs to human-readable names.
 */
public final class OidDictionary {

    private static final Map<String, String> OID_MAP = new HashMap<>();

    static {
        // PKCS#9
        OID_MAP.put("1.2.840.113549.1.9.3", "contentType");
        OID_MAP.put("1.2.840.113549.1.9.4", "messageDigest");
        OID_MAP.put("1.2.840.113549.1.9.5", "signingTime");
        OID_MAP.put("1.2.840.113549.1.9.15", "SMIME Capabilities");

        // RFC 5035 / Signing Certificate
        OID_MAP.put("1.2.840.113549.1.9.16.2.12", "signingCertificate");
        OID_MAP.put("1.2.840.113549.1.9.16.2.47", "signingCertificateV2");

        // X.509
        OID_MAP.put("2.5.29.14", "subjectKeyIdentifier");
        OID_MAP.put("2.5.29.15", "keyUsage");
        OID_MAP.put("2.5.29.17", "subjectAltName");
        OID_MAP.put("2.5.29.19", "basicConstraints");
        OID_MAP.put("2.5.29.35", "authorityKeyIdentifier");

        // Otros útiles en AFIP/ARCA (por si aparecen)
        OID_MAP.put("2.16.840.1.113730.1.1", "Netscape Cert Type");
    }

    private OidDictionary() { /* util */ }

    /**
     * Returns a human-readable name for the given OID, or the OID itself if not recognized.
     */
    public static String prettyName(String oid) {
        return OID_MAP.getOrDefault(oid, oid);
    }
}
