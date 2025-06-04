package com.germanfica.wsfe.util;

import org.bouncycastle.cms.CMSSignedData;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CmsFormatInspector {

    private CmsFormatInspector() { }

    public enum Format {
        CMS_SIGNED,
        CERTIFICATE,
        UNKNOWN
    }

    public static Format analyze(byte[] data) {
        if (isCmsSigned(data)) return Format.CMS_SIGNED;
        if (isCertificate(data)) return Format.CERTIFICATE;
        return Format.UNKNOWN;
    }

    public static Format analyze(String base64) {
        return analyze(CryptoUtils.decodeBase64(base64));
    }

    public static boolean isCmsSigned(byte[] data) {
        try {
            new CMSSignedData(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCmsSigned(String base64) {
        return isCmsSigned(CryptoUtils.decodeBase64(base64));
    }

    public static boolean isCertificate(byte[] data) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(data));
            return cert != null;
        } catch (CertificateException | java.security.NoSuchProviderException e) {
            return false;
        }
    }

    public static boolean isCertificate(String base64) {
        return isCertificate(CryptoUtils.decodeBase64(base64));
    }
}
