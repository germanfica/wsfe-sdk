package com.germanfica.wsfe.util;

import com.germanfica.wsfe.model.LoginTicketRequestData;
import com.germanfica.wsfe.time.ArcaDateTime;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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

    /** Raw XML payload (public accessor) */
    public static String extractRawXml(CMSSignedData cms) {
        return getRawXml(cms);
    }

    /** Extract extra fields from the LoginTicket (uniqueId, source, destination, service) */
    public static String extractTicketDetails(CMSSignedData cms) {
        LoginTicketRequestData ticket = extractLoginTicket(cms);
        return String.format(
            "uniqueId=%s, source=%s, destination=%s, service=%s",
            ticket.uniqueId(),
            ticket.source(),
            ticket.destination(),
            ticket.service()
        );
    }

    /** List all certificates embedded in the CMS */
    public static List<X509Certificate> extractAllCertificates(CMSSignedData cms) {
        List<X509Certificate> result = new ArrayList<>();
        try {
            var certs = cms.getCertificates().getMatches(null);
            JcaX509CertificateConverter conv = new JcaX509CertificateConverter();
            for (Object c : certs) {
                X509CertificateHolder holder = (X509CertificateHolder) c;
                result.add(conv.getCertificate(holder));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to list certificates", e);
        }
        return result;
    }

    /** List all signers and algorithms */
    public static List<String> extractSignerInfo(CMSSignedData cms) {
        List<String> info = new ArrayList<>();
        SignerInformationStore signers = cms.getSignerInfos();
        for (SignerInformation s : signers.getSigners()) {
            info.add("SignerID=" + s.getSID()
                + ", DigestAlg=" + s.getDigestAlgOID()
                + ", EncryptionAlg=" + s.getEncryptionAlgOID());
        }
        return info;
    }

    /** List signed attributes */
    public static List<String> extractSignedAttributes(CMSSignedData cms) {
        List<String> attrs = new ArrayList<>();
        SignerInformation signer = cms.getSignerInfos().getSigners().iterator().next();

        if (signer.getSignedAttributes() != null) {
            ASN1EncodableVector v = signer.getSignedAttributes().toASN1EncodableVector();
            for (int i = 0; i < v.size(); i++) {
                ASN1Encodable enc = v.get(i);

                if (enc instanceof Attribute) {
                    Attribute attr = (Attribute) enc;
                    String oid = attr.getAttrType().getId();
                    String val = attr.getAttrValues().toString();
                    attrs.add("OID=" + oid + ", values=" + val);
                } else {
                    // fallback si no es Attribute
                    attrs.add(enc.toString());
                }
            }
        }
        return attrs;
    }

    /** Verify signature against first certificate */
    public static boolean verifySignature(CMSSignedData cms) {
        try {
            SignerInformation signer = cms.getSignerInfos().getSigners().iterator().next();
            X509Certificate cert = extractCertificate(cms);
            return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    /* =============================================================
     * Internal helpers
     * ============================================================= */

    /**
     * Raw XML payload as UTF‑8 text.
     */
    private static String getRawXml(CMSSignedData cms) {
        byte[] content = (byte[]) cms.getSignedContent().getContent();
        return new String(content, StandardCharsets.UTF_8);
    }
}
