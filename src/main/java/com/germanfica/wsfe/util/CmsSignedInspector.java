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
    public record CmsTimestamps(
        ArcaDateTime signingTime,
        ArcaDateTime generationTime,
        ArcaDateTime expirationTime,
        ArcaDateTime certNotBefore,
        ArcaDateTime certNotAfter) { }

    public CmsTimestamps inspect(String signedCmsBase64) throws Exception {
        CMSSignedData cms = new CMSSignedData(CryptoUtils.decodeBase64(signedCmsBase64));

        // 1) signingTime (atributo CMS)
        SignerInformation si = cms.getSignerInfos().getSigners().iterator().next();
        ArcaDateTime signingTime = Optional.ofNullable(
                si.getSignedAttributes().get(CMSAttributes.signingTime))
            .map(attr -> {
                var asn1Obj = attr.getAttrValues().getObjectAt(0).toASN1Primitive();
                Time time = Time.getInstance(asn1Obj);
                return ArcaDateTime.of(time.getDate().toInstant());
            })
            .orElse(null);

        // 2) Certificado
        X509Certificate cert = CryptoUtils.extractCertificate(signedCmsBase64);

        // 3) Valores del LTR
        String rawXml = getRawXml(cms);

        System.out.println(rawXml);

        LoginTicketRequestData ticket = (LoginTicketRequestData) LoginTicketParser.parse(rawXml);

        System.out.println(ticket.source());
        System.out.println(ticket.destination());
        System.out.println(ticket.uniqueId());
        System.out.println(ticket.generationTime());
        System.out.println(ticket.expirationTime());
        System.out.println(ticket.service());

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
     * Returns the raw XML payload embedded in the given {@link CMSSignedData}.
     *
     * <p>This simply casts the CMS signed content to a byte[] and builds a UTF-8 string.</p>
     *
     * @param cms a {@link CMSSignedData} instance containing the signed content.
     * @return the XML as a UTF-8 string.
     */
    private String getRawXml(final CMSSignedData cms) {
        var contentBytes = (byte[]) cms.getSignedContent().getContent();
        return new String(contentBytes, StandardCharsets.UTF_8);
    }
}
