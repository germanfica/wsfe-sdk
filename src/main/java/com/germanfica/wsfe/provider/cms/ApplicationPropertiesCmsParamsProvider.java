package com.germanfica.wsfe.provider.cms;

import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.provider.CredentialsProvider;
import com.germanfica.wsfe.util.ConfigUtils;

import java.util.Optional;

/**
 * Provider que obtiene los valores desde application.properties
 * usando la clase ApplicationProperties como fuente.
 */
public class ApplicationPropertiesCmsParamsProvider implements CredentialsProvider<CmsParams> {
    @Override
    public Optional<CmsParams> resolve() {
        try {
            return Optional.of(CmsParams.builder()
                .setKeystorePath(ConfigUtils.getAppProperty("wsaa.cms.keystore-path"))
                .setPassword(ConfigUtils.getAppProperty("wsaa.cms.keystore-password"))
                .setSigner(ConfigUtils.getAppProperty("wsaa.cms.keystore-signer"))
                .setDstDn(ConfigUtils.getAppProperty("wsaa.cms.dstdn", "cn=wsaahomo,o=afip,c=ar,serialNumber=CUIT 33693450239"))
                .setService(ConfigUtils.getAppProperty("wsaa.cms.service", "wsfe"))
                .setTicketTime(ConfigUtils.getAppProperty("wsaa.cms.ticket-time", 36_000L))
                .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
