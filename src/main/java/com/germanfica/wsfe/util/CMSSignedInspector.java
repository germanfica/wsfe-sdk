package com.germanfica.wsfe.util;

import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.wsaa.ticket.LoginTicketData;
import com.germanfica.wsfe.wsaa.ticket.LoginTicketRequestData;
import com.germanfica.wsfe.wsaa.ticket.LoginTicketResponseData;
import org.bouncycastle.asn1.cms.CMSAttributes;

import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

public class CMSSignedInspector {
    public record CmsTimestamps(
        OffsetDateTime signingTime,
        OffsetDateTime generationTime,
        OffsetDateTime expirationTime,
        OffsetDateTime certNotBefore,
        OffsetDateTime certNotAfter) { }

    public CmsTimestamps inspect(String signedCmsBase64) throws Exception {
        CMSSignedData cms = new CMSSignedData(CryptoUtils.decodeBase64(signedCmsBase64));

        // 1) signingTime (atributo CMS)
        SignerInformation si = cms.getSignerInfos().getSigners().iterator().next();
        ArcaDateTime signingTime = Optional.ofNullable(
                si.getSignedAttributes().get(CMSAttributes.signingTime))
            .map(attr -> {
                var asn1Obj = attr.getAttrValues().getObjectAt(0).toASN1Primitive();
                Time time = Time.getInstance(asn1Obj);
                return ArcaDateTime.of(time.getDate().toInstant().atOffset(ZoneOffset.UTC));
            })
            .orElse(null);

        // 2) Certificado
        X509Certificate cert = CryptoUtils.extractCertificate(signedCmsBase64);

        // 3) Valores del LTR
        String rawXml = getRawXml(cms);
        XMLExtractor extractor = new XMLExtractor(rawXml);

        System.out.println(extractor.extractValue("/loginTicketResponse/header/generationTime")); // <- ""
        System.out.println(extractor.extractValue("/loginTicketRequest/header/generationTime")); // <- 2025-06-02T12:48:00

        LoginTicketData ltd = LoginTicketData.parse(rawXml);

        //LoginTicketResponseData resp = (LoginTicketResponseData) LoginTicketData.parse(rawXml);
        LoginTicketRequestData resp2 = (LoginTicketRequestData) LoginTicketData.parse(rawXml);

        if (ltd instanceof LoginTicketResponseData r) {
            // —— caso LoginTicketResponseData ——
            String token = r.token();
            String sign  = r.sign();
            // …persistir TA, etc.

        } else if (ltd instanceof LoginTicketRequestData q) {
            // —— caso LoginTicketRequestData ——
            // aquí vendrá de un signedCmsBase64 inspeccionado
            String servicio = q.service();
            // etc.

        } else {
            // (en teoría no debería llegar aquí, pues LoginTicketData es sealed
            throw new IllegalStateException("Tipo de LoginTicketData inesperado: " + ltd.getClass());
        }

//        ArcaDateTime generationTime = ltr.generationTime == null || ltr.generationTime.isBlank()
//            ? null : ArcaDateTime.parse(ltr.generationTime);
//
//        ArcaDateTime expirationTime = ltr.expirationTime == null || ltr.expirationTime.isBlank()
//            ? null : ArcaDateTime.parse(ltr.expirationTime);

        return null;
//        return new CmsTimestamps(
//            signingTime == null ? null : signingTime.toOffsetDateTime(),
//            generationTime == null ? null : generationTime.toOffsetDateTime(),
//            expirationTime == null ? null : expirationTime.toOffsetDateTime(),
//            cert.getNotBefore().toInstant().atOffset(ZoneOffset.UTC),
//            cert.getNotAfter().toInstant().atOffset(ZoneOffset.UTC)
//        );
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
