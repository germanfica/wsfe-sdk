package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.ConfigUtils;
import com.germanfica.wsfe.provider.CredentialsProvider;

import java.util.Optional;

/**
 * Lee TOKEN, SIGN y CUIT desde variables de entorno:
 *
 *   WSFE_TOKEN, WSFE_SIGN, WSFE_CUIT
 *   (opcionales) WSFE_GENERATION_TIME, WSFE_EXPIRATION_TIME en ISO-8601 completo.
 */
public class EnvironmentFEAuthParamsProvider implements CredentialsProvider<FEAuthParams> {

    @Override
    public Optional<FEAuthParams> resolve() {
        String token = ConfigUtils.getenv("WSFE_TOKEN");
        String sign  = ConfigUtils.getenv("WSFE_SIGN");
        Long   cuit  = ConfigUtils.getenv("WSFE_CUIT", -1L); // No existe CUIT negativo: -1L representa CUIT no definido

        if (token == null || sign == null || cuit <= 0) {
            return Optional.empty();
        }

        FEAuthParams.FEAuthParamsBuilder b = FEAuthParams.builder()
            .setToken(token)
            .setSign(sign)
            .setCuit(cuit);

        String gen = ConfigUtils.getenv("WSFE_GENERATION_TIME");
        String exp = ConfigUtils.getenv("WSFE_EXPIRATION_TIME");
        if (gen != null) b.setGenerationTime(ArcaDateTime.parse(gen));
        if (exp != null) b.setExpirationTime(ArcaDateTime.parse(exp));
        //if (gen == null || exp == null) return Optional.empty();

        return Optional.of(b.build());
        //FEAuthParams p = b.build();
        //return p.isExpired() ? Optional.empty() : Optional.of(p);
    }
}
