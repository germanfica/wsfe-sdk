package com.germanfica.wsfe.util;

import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.model.LoginTicketRequestData;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class CmsSignedInspector {

    private CmsSignedInspector() { }

    public record CmsTimestamps(
        ArcaDateTime signingTime,
        ArcaDateTime generationTime,
        ArcaDateTime expirationTime,
        ArcaDateTime certNotBefore,
        ArcaDateTime certNotAfter) { }

    /**
     * Inspects a Base64-encoded CMS structure and extracts relevant timestamps.
     *
     * @param signedCmsBase64 the CMS content encoded in Base64.
     * @return a CmsTimestamps object containing signing time, generation time,
     *         expiration time, and certificate validity period.
     * @throws Exception if parsing or data extraction fails.
     */
    public static CmsTimestamps inspect(String signedCmsBase64) throws Exception {
        CMSSignedData cms = new CMSSignedData(CryptoUtils.decodeBase64(signedCmsBase64));

        // 1) Extract signingTime attribute from CMS
        SignerInformation signerInfo = cms.getSignerInfos().getSigners().iterator().next();
        ArcaDateTime signingTime = Optional.ofNullable(
                signerInfo.getSignedAttributes().get(CMSAttributes.signingTime))
            .map(attr -> {
                var asn1Obj = attr.getAttrValues().getObjectAt(0).toASN1Primitive();
                Time time = Time.getInstance(asn1Obj);
                return ArcaDateTime.of(time.getDate().toInstant());
            })
            .orElse(null);

        // 2) Extract X509 certificate from CMS
        X509Certificate cert = CryptoUtils.extractCertificate(signedCmsBase64);

        // 3) Extract raw XML and parse Login Ticket Request data
        String rawXml = getRawXml(cms);
        LoginTicketRequestData ticket = (LoginTicketRequestData) LoginTicketParser.parse(rawXml);

        ArcaDateTime generationTime = ArcaDateTime.parse(ticket.generationTime());
        ArcaDateTime expirationTime = ArcaDateTime.parse(ticket.expirationTime());

        return new CmsTimestamps(
            signingTime == null ? null : ArcaDateTime.of(signingTime.toOffsetDateTime()),
            ArcaDateTime.of(generationTime.toOffsetDateTime()),
            ArcaDateTime.of(expirationTime.toOffsetDateTime()),
            ArcaDateTime.of(cert.getNotBefore().toInstant()),
            ArcaDateTime.of(cert.getNotAfter().toInstant())
        );
    }

    /**
     * Retrieves the raw XML payload embedded in the given CMSSignedData.
     *
     * @param cms a CMSSignedData instance containing the signed content.
     * @return the XML payload as a UTF-8 string.
     */
    private static String getRawXml(final CMSSignedData cms) {
        byte[] contentBytes = (byte[]) cms.getSignedContent().getContent();
        return new String(contentBytes, StandardCharsets.UTF_8);
    }
}
