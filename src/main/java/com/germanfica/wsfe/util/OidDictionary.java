package com.germanfica.wsfe.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping common ASN.1 OIDs to human-readable names.
 */
public final class OidDictionary {

    private static final Map<String, String> OID_MAP = new HashMap<>();

    static {
        // PKCS#9 standard attributes
        OID_MAP.put("1.2.840.113549.1.9.1", "emailAddress");
        OID_MAP.put("1.2.840.113549.1.9.2", "unstructuredName");
        OID_MAP.put("1.2.840.113549.1.9.3", "contentType");
        OID_MAP.put("1.2.840.113549.1.9.4", "messageDigest");
        OID_MAP.put("1.2.840.113549.1.9.5", "signingTime");
        OID_MAP.put("1.2.840.113549.1.9.6", "counterSignature");
        OID_MAP.put("1.2.840.113549.1.9.7", "challengePassword");
        OID_MAP.put("1.2.840.113549.1.9.8", "unstructuredAddress");
        OID_MAP.put("1.2.840.113549.1.9.14", "extensionRequest");
        OID_MAP.put("1.2.840.113549.1.9.15", "SMIMECapabilities");

        // RFC 5035 / 5751 (S/MIME + CMS Signing Certificate attributes)
        OID_MAP.put("1.2.840.113549.1.9.16.2.12", "signingCertificate");
        OID_MAP.put("1.2.840.113549.1.9.52", "signingCertificateV2");
        OID_MAP.put("1.2.840.113549.1.9.16.2.47", "signingCertificateV2 (alt)");

        // X.509 certificate extensions
        OID_MAP.put("2.5.29.14", "subjectKeyIdentifier");
        OID_MAP.put("2.5.29.15", "keyUsage");
        OID_MAP.put("2.5.29.16", "privateKeyUsagePeriod");
        OID_MAP.put("2.5.29.17", "subjectAltName");
        OID_MAP.put("2.5.29.18", "issuerAltName");
        OID_MAP.put("2.5.29.19", "basicConstraints");
        OID_MAP.put("2.5.29.31", "crlDistributionPoints");
        OID_MAP.put("2.5.29.32", "certificatePolicies");
        OID_MAP.put("2.5.29.35", "authorityKeyIdentifier");
        OID_MAP.put("2.5.29.37", "extendedKeyUsage");

        // Algoritmos (RSA, SHA, ECDSA, etc.)
        OID_MAP.put("1.2.840.113549.1.1.1", "rsaEncryption");
        OID_MAP.put("1.2.840.113549.1.1.5", "sha1WithRSAEncryption");
        OID_MAP.put("1.2.840.113549.1.1.11", "sha256WithRSAEncryption");
        OID_MAP.put("1.2.840.113549.1.1.12", "sha384WithRSAEncryption");
        OID_MAP.put("1.2.840.113549.1.1.13", "sha512WithRSAEncryption");
        OID_MAP.put("2.16.840.1.101.3.4.2.1", "sha256");
        OID_MAP.put("2.16.840.1.101.3.4.2.2", "sha384");
        OID_MAP.put("2.16.840.1.101.3.4.2.3", "sha512");
        OID_MAP.put("1.2.840.10045.4.3.2", "ecdsaWithSHA256");
        OID_MAP.put("1.2.840.10045.4.3.3", "ecdsaWithSHA384");
        OID_MAP.put("1.2.840.10045.4.3.4", "ecdsaWithSHA512");

        // Netscape legacy
        OID_MAP.put("2.16.840.1.113730.1.1", "NetscapeCertType");
        OID_MAP.put("2.16.840.1.113730.1.13", "NetscapeComment");
    }

    private OidDictionary() { /* util */ }

    /**
     * Returns a human-readable name for the given OID, or the OID itself if not recognized.
     */
    public static String prettyName(String oid) {
        return OID_MAP.getOrDefault(oid, oid);
    }
}
