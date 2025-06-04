package com.germanfica.wsfe.util;

import com.germanfica.wsfe.model.LoginTicketRequestData;
import com.germanfica.wsfe.time.ArcaDateTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * Utility class for inspecting ARCA/AFIP CMS‐signed messages.
 * <p>
 */
public final class CmsSignedExtractor {

    private CmsSignedExtractor() { /* util */ }

    /* =============================================================
     * Public API – high level
     * ============================================================= */

    public static ArcaDateTime extractSigningTime(CMSSignedData cms) {
        SignerInformation si = cms.getSignerInfos().getSigners().iterator().next();
        if (si.getSignedAttributes() == null) return null;

        Attribute attr = si.getSignedAttributes().get(CMSAttributes.signingTime);
        if (attr == null) return null;

        Time t = Time.getInstance(attr.getAttrValues().getObjectAt(0));
        return ArcaDateTime.of(t.getDate().toInstant());
    }

    public static LoginTicketRequestData extractLoginTicket(CMSSignedData cms) {
        String xml = getRawXml(cms);
        return (LoginTicketRequestData) LoginTicketParser.parse(xml);
    }

    public static X509Certificate extractCertificate(CMSSignedData cms) {
        try {
            return CryptoUtils.extractCertificate(cms.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract certificate from CMS", e);
        }
    }

    /**
     * Certificate <strong>valid‑from</strong> timestamp.
     */
    public static ArcaDateTime extractCertificateValidFrom(CMSSignedData cms) {
        X509Certificate cert = extractCertificate(cms);
        return ArcaDateTime.of(cert.getNotBefore().toInstant());
    }

    /**
     * Certificate <strong>valid‑to</strong> timestamp.
     */
    public static ArcaDateTime extractCertificateValidTo(CMSSignedData cms) {
        X509Certificate cert = extractCertificate(cms);
        return ArcaDateTime.of(cert.getNotAfter().toInstant());
    }

    /**
     * Login‑ticket <strong>generationTime</strong> value.
     */
    public static ArcaDateTime extractTicketGenerationTime(CMSSignedData cms) {
        LoginTicketRequestData ticket = extractLoginTicket(cms);
        return ArcaDateTime.parse(ticket.generationTime());
    }

    /**
     * Login‑ticket <strong>expirationTime</strong> value.
     */
    public static ArcaDateTime extractTicketExpirationTime(CMSSignedData cms) {
        LoginTicketRequestData ticket = extractLoginTicket(cms);
        return ArcaDateTime.parse(ticket.expirationTime());
    }

    /**
     * Raw XML payload as UTF‑8 text.
     */
    private static String getRawXml(CMSSignedData cms) {
        byte[] content = (byte[]) cms.getSignedContent().getContent();
        return new String(content, StandardCharsets.UTF_8);
    }
}
