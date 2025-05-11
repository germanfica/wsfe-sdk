package com.germanfica.wsfe.util;

import com.germanfica.wsfe.param.CmsParams;

import java.util.Optional;

/**
 * Provider que obtiene los valores desde application.properties
 * usando la clase ConfigLoader como fuente.
 */
public class ApplicationPropertiesCmsParamsProvider implements CredentialsProvider<CmsParams> {
    @Override
    public Optional<CmsParams> resolve() {
        try {
            return Optional.of(CmsParams.builder()
                    .setKeystorePath(ConfigLoader.KEYSTORE_PATH)
                    .setPassword(ConfigLoader.KEYSTORE_PASSWORD)
                    .setSigner(ConfigLoader.KEYSTORE_SIGNER)
                    .setDstDn(ConfigLoader.DSTDN)
                    .setService(ConfigLoader.SERVICE)
                    .setTicketTime(ConfigLoader.TICKET_TIME)
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
