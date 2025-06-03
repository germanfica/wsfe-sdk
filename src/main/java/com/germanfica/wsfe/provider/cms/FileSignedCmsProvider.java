package com.germanfica.wsfe.provider.cms;

import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.util.CredentialsProvider;
import com.germanfica.wsfe.util.SimpleIni;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Persiste y recupera el valor {@code signedCmsBase64} empleando un fichero
 * INI (parseado con {@link SimpleIni}) en <code>~/.wsfe/cms.ini</code>.
 *
 * <p>La idea es que cualquier {@link ProviderChain} pueda
 * incluir este provider para reutilizar el CMS ya firmado (válido ≈ 2 años)
 * sin obligar al dev a volver a firmarlo en cada ejecución.</p>
 *
 * <pre>
 * [default]
 * signedCms = MII... (Base64)
 * </pre>
 * <h3>Uso</h3>
 * <pre>{@code
 * ProviderChain<String> chain = ProviderChain.<String>builder()
 *     .addProvider(new FileSignedCmsProvider())              // reutiliza CMS si existe
 *     .addProvider(new EnvironmentSignedCmsProvider())       // ejemplo: WSAA_SIGNED_CMS
 *     .addProvider(() -> Optional.of(Cms.create(params).getSignedValue()))
 *     .build();
 * }</pre>
 */
public final class FileSignedCmsProvider implements CredentialsProvider<String> {

    /* ~/.wsfe/cms.ini */
    private static final Path CMS_FILE = Paths.get(
        System.getProperty("user.home"), ".wsfe", "cms.ini"
    );
    private static final String SECTION = "default";
    private static final String KEY     = "signedCms";

    @Override
    public Optional<String> resolve() {
        try {
            if (!Files.exists(CMS_FILE)) return Optional.empty();

            SimpleIni ini = SimpleIni.load(CMS_FILE);
            String cms = ini.get(SECTION, KEY);
            return (cms == null || cms.isBlank()) ? Optional.empty() : Optional.of(cms.trim());
        } catch (IOException e) {
            // Problemas de lectura se tratan como "no encontrado" para no abortar la cadena.
            return Optional.empty();
        }
    }

    /**
     * Guarda (o reemplaza) el CMS firmado en <code>~/.wsfe/cms.ini</code>.
     * Crea la carpeta <i>.wsfe</i> si fuera necesario.
     *
     * @param signedCmsBase64 CMS firmado en Base64.
     * @throws IllegalArgumentException si el valor es nulo o vacío.
     */
    public static void save(String signedCmsBase64) {
        if (signedCmsBase64 == null || signedCmsBase64.isBlank()) {
            throw new IllegalArgumentException("signedCmsBase64 vacío o nulo");
        }
        try {
            SimpleIni ini = Files.exists(CMS_FILE) ? SimpleIni.load(CMS_FILE) : new SimpleIni();
            ini.put(SECTION, KEY, signedCmsBase64.trim());
            ini.save(CMS_FILE);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar signedCmsBase64 en disco", e);
        }
    }
}
