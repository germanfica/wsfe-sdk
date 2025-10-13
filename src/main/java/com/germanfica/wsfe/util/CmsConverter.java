package com.germanfica.wsfe.util;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import java.util.Base64;

public class CmsConverter {

    /**
     * Converts a Base64-encoded CMS string into a CMSSignedData object.
     */
    public static CMSSignedData fromBase64(String cmsBase64) throws CMSException {
        byte[] cmsBytes = Base64.getDecoder().decode(cmsBase64);
        return new CMSSignedData(cmsBytes);
    }

    /**
     * Converts a binary CMS file (DER or PEM without headers) into CMSSignedData.
     */
    public static CMSSignedData fromBytes(byte[] cmsBytes) throws CMSException {
        return new CMSSignedData(cmsBytes);
    }
}
