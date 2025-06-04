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
        String gen = ConfigLoader.GENERATION_TIME;
        String exp = ConfigLoader.EXPIRATION_TIME;

        if (token == null || sign == null || cuit == null || gen == null || exp == null) {
            return Optional.empty();
        }

        FEAuthParams.FEAuthParamsBuilder b = FEAuthParams.builder()
            .setToken(token)
            .setSign(sign)
            .setCuit(cuit)
            .setGenerationTime(ArcaDateTime.parse(gen))
            .setExpirationTime(ArcaDateTime.parse(exp));

        FEAuthParams feAuthParams = b.build();
        if(feAuthParams.isExpired()) return Optional.empty(); // ticket vencido -> forzar refresco

        return Optional.of(feAuthParams);
    }
}
