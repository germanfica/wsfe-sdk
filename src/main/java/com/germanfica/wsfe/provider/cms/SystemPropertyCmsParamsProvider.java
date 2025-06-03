package com.germanfica.wsfe.provider.cms;

import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.provider.CredentialsProvider;
import com.germanfica.wsfe.util.ConfigUtils;

import java.util.Optional;

public class SystemPropertyCmsParamsProvider implements CredentialsProvider<CmsParams> {
    @Override
    public Optional<CmsParams> resolve() {
        try {
            return Optional.of(CmsParams.builder()
                    .setKeystorePath(ConfigUtils.getProperty("wsaa.cms.keystore-path"))
                    .setPassword(ConfigUtils.getProperty("wsaa.cms.keystore-password"))
                    .setSigner(ConfigUtils.getProperty("wsaa.cms.keystore-signer"))
                    .setDstDn(ConfigUtils.getProperty("wsaa.cms.dstdn", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239"))
                    .setService(ConfigUtils.getProperty("wsaa.cms.service", "wsfe"))
                    .setTicketTime(ConfigUtils.getProperty("wsaa.cms.ticket-time", 36000L))
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
