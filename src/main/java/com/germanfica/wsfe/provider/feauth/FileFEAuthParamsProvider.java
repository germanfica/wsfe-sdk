package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.CredentialsProvider;
import com.germanfica.wsfe.util.SimpleIni;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Persiste y recupera FEAuthParams (token, sign, cuit, generation/expiration) en
 * <code>~/.wsfe/ta.ini</code>. Si el TA está expirado se ignora.
 */
public class FileFEAuthParamsProvider implements CredentialsProvider<FEAuthParams> {

    private static final Path TA_FILE = Paths.get(
        System.getProperty("user.home"), ".wsfe", "ta.ini"
    );
    private static final String SECTION = "default";

    @Override
    public Optional<FEAuthParams> resolve() {
        try {
            if (!Files.exists(TA_FILE)) return Optional.empty();

            SimpleIni ini = SimpleIni.load(TA_FILE);

            String token   = ini.get(SECTION, "token");
            String sign    = ini.get(SECTION, "sign");
            String cuitStr = ini.get(SECTION, "cuit");
            if (token == null || sign == null || cuitStr == null) return Optional.empty();

            FEAuthParams.FEAuthParamsBuilder b = FEAuthParams.builder()
                .setToken(token)
                .setSign(sign)
                .setCuit(Long.parseLong(cuitStr));

            String gen = ini.get(SECTION, "generationTime");
            String exp = ini.get(SECTION, "expirationTime");
            if (gen != null) b.setGenerationTime(ArcaDateTime.parse(gen));
            if (exp != null) b.setExpirationTime(ArcaDateTime.parse(exp));

            FEAuthParams p = b.build();
            return p.isExpired() ? Optional.empty() : Optional.of(p);
        } catch (IOException | NumberFormatException e) {
            return Optional.empty();   // Problemas de lectura/formato => "no encontrado"
        }
    }

    /** Guarda (o reemplaza) el TA en <code>~/.wsfe/ta.ini</code> */
    public static void save(FEAuthParams p) {
        try {
            SimpleIni ini = new SimpleIni();
            ini.put(SECTION, "token", p.getToken());
            ini.put(SECTION, "sign",  p.getSign());
            ini.put(SECTION, "cuit",  String.valueOf(p.getCuit()));

            if (p.getGenerationTime() != null)
                ini.put(SECTION, "generationTime", p.getGenerationTime().toString());
            if (p.getExpirationTime() != null)
                ini.put(SECTION, "expirationTime", p.getExpirationTime().toString());

            ini.save(TA_FILE);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar TA en disco", e);
        }
    }
}
