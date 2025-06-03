package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.ConfigLoader;
import com.germanfica.wsfe.provider.CredentialsProvider;

import java.util.Optional;

/**
 * Extrae TOKEN, SIGN y CUIT de *application.properties* (o donde apunte tu `ConfigLoader`).
 *
 * Claves esperadas:
 *   wsfe.token, wsfe.sign, wsfe.cuit
 *   (opcionales) wsfe.generation-time, wsfe.expiration-time
 */
public class ConfigFileFEAuthParamsProvider implements CredentialsProvider<FEAuthParams> {

    @Override
    public Optional<FEAuthParams> resolve() {
        String token = ConfigLoader.TOKEN;
        String sign  = ConfigLoader.SIGN;
        Long   cuit  = ConfigLoader.CUIT;

        if (token == null || sign == null || cuit == null) {
            return Optional.empty();
        }

        FEAuthParams.FEAuthParamsBuilder b = FEAuthParams.builder()
            .setToken(token)
            .setSign(sign)
            .setCuit(cuit);

        String gen = ConfigLoader.GENERATION_TIME;
        String exp = ConfigLoader.EXPIRATION_TIME;
        if (gen != null) b.setGenerationTime(ArcaDateTime.parse(gen));
        if (exp != null) b.setExpirationTime(ArcaDateTime.parse(exp));

        return Optional.of(b.build());
    }
}
