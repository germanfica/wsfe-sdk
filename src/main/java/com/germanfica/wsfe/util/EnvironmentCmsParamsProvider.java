package com.germanfica.wsfe.util;

import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.provider.CredentialsProvider;

import java.util.Optional;

public class EnvironmentCmsParamsProvider implements CredentialsProvider<CmsParams> {
    @Override
    public Optional<CmsParams> resolve() {
        try {
            return Optional.of(CmsParams.builder()
                    .setKeystorePath(ConfigUtils.getenv("WSAA_CMS_KEYSTORE_PATH"))
                    .setPassword(ConfigUtils.getenv("WSAA_CMS_KEYSTORE_PASSWORD"))
                    .setSigner(ConfigUtils.getenv("WSAA_CMS_KEYSTORE_SIGNER"))
                    .setDstDn(ConfigUtils.getenv("WSAA_CMS_DSTDN", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239"))
                    .setService(ConfigUtils.getenv("WSAA_CMS_SERVICE", "wsfe"))
                    .setTicketTime(ConfigUtils.getenv("WSAA_CMS_TICKET_TIME", 36000L))
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
