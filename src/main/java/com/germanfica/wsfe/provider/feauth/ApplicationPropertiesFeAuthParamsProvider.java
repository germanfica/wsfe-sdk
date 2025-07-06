package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.provider.CredentialsProvider;
import com.germanfica.wsfe.util.ConfigUtils;

import java.util.Optional;

/**
 * Extrae TOKEN, SIGN y CUIT de *application.properties* (o donde apunte tu `ApplicationProperties`).
 *
 * Claves esperadas:
 *   wsfe.token, wsfe.sign, wsfe.cuit, wsfe.generation-time, wsfe.expiration-time
 */
public class ApplicationPropertiesFeAuthParamsProvider implements CredentialsProvider<FEAuthParams> {

    @Override
    public Optional<FEAuthParams> resolve() {
        String token = ConfigUtils.getAppProperty("wsfe.token");
        String sign  = ConfigUtils.getAppProperty("wsfe.sign");
        Long   cuit  = ConfigUtils.getAppProperty("wsfe.cuit", -1L); // No existe CUIT negativo: -1L representa CUIT no definido

        if (token == null || sign == null || cuit <= 0) {
            return Optional.empty();
        }

        FEAuthParams.FEAuthParamsBuilder b = FEAuthParams.builder()
            .setToken(token)
            .setSign(sign)
            .setCuit(cuit);

        String gen = ConfigUtils.getAppProperty("wsfe.generation-time"); //.map(ArcaDateTime::parse).orElseThrow();
        String exp = ConfigUtils.getAppProperty("wsfe.expiration-time"); //.map(ArcaDateTime::parse).orElseThrow();
        if (gen != null) b.setGenerationTime(ArcaDateTime.parse(gen));
        if (exp != null) b.setExpirationTime(ArcaDateTime.parse(exp));
        //if (gen == null || exp == null) return Optional.empty();

        return Optional.of(b.build());
    }
}
