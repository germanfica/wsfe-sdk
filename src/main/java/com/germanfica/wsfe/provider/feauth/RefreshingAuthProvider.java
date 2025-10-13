package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.model.LoginTicketResponseData;
import com.germanfica.wsfe.net.HttpStatus;
import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.provider.ProviderChain;
import com.germanfica.wsfe.provider.cms.ApplicationPropertiesCmsParamsProvider;
import com.germanfica.wsfe.provider.cms.EnvironmentCmsParamsProvider;
import com.germanfica.wsfe.provider.cms.FileSignedCmsProvider;
import com.germanfica.wsfe.provider.cms.SystemPropertyCmsParamsProvider;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.LoginTicketParser;
import fev1.dif.afip.gov.ar.FEAuthRequest;

/**
 * RefreshingAuthProvider
 *
 * Implementa la política de renovación del Ticket de Acceso (TA) según WSAA.
 *
 * - Solo el TA (FEAuthParams) se persiste y se reutiliza entre ejecuciones.
 * - Cada vez que se requiere un TA nuevo, se genera un CMS fresco
 *   (basado en un nuevo TRA firmado con el certificado digital).
 *
 * Este proveedor confía íntegramente en los tiempos devueltos por el WSAA
 * dentro del <loginTicketResponse>.
 */
public class RefreshingAuthProvider implements FEAuthProvider {

    private final WsaaClient wsaa;       // Cliente WSAA
    private volatile FEAuthParams cache; // TA cacheado mientras no expire

    public RefreshingAuthProvider(WsaaClient wsaa) {
        this.wsaa = wsaa;
    }

    @Override
    public FEAuthRequest getAuth() throws ApiException {
        FEAuthParams local = cache;
        if (local == null || local.isExpired()) {
            synchronized (this) {
                local = cache;
                if (local == null || local.isExpired()) {
                    refresh();                     // (re)genera TA y actualiza cache
                    local = cache;
                }
            }
        }
        return toFEAuthRequest(local);
    }

    private void refresh() throws ApiException {
        // (1) Intentar reutilizar TA válido persistido en disco
        cache = ProviderChain.<FEAuthParams>builder()
            .addProvider(new FileFEAuthParamsProvider())
            .build()
            .resolve()
            .orElse(null);

        if (cache != null && !cache.isExpired()) return;  // TA vigente, no hace nada

        // (2) No había TA o estaba vencido -> pedir uno nuevo a WSAA
        Cms cms = buildFreshCms();
        String xml = wsaa.authService().autenticar(cms);

        try {
            LoginTicketResponseData data = (LoginTicketResponseData) LoginTicketParser.parse(xml);

            cache = FEAuthParams.builder()
                .setToken(data.token())
                .setSign(data.sign())
                .setCuit(cms.getSubjectCuit())         // CUIT del titular del certificado que firma el CMS
                .setGenerationTime(ArcaDateTime.parse(data.generationTime()))
                .setExpirationTime(ArcaDateTime.parse(data.expirationTime()))
                .build();

        } catch (Exception e) {
            throw new ApiException(
                new ErrorDto("invalid_xml", "No se pudo parsear loginTicketResponse", null),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // (3) Persistir nuevo TA
        FileFEAuthParamsProvider.save(cache);
    }

    /**
     * Genera un nuevo CMS (y con él su Subject CUIT) cada vez que necesitemos
     * refrescar el Ticket de Acceso.
     *
     * @deprecated Use {@link #buildFreshCms()} instead.
     */
    @Deprecated
    private Cms buildCmsAutomatically() {
        String signedCmsBase64 = ProviderChain.<String>builder()
            .addProvider(new FileSignedCmsProvider())      // primero busca en el archivo
            //.addProvider(new EnvironmentSignedCmsProvider()) // (opcional) WSAA_SIGNED_CMS env var
            // ... cualquier otro provider (SystemProperty, etc.)
            .build()
            .resolve()
            .orElseGet(() -> {
                CmsParams cmsParams = ProviderChain.<CmsParams>builder()
                    .addProvider(new EnvironmentCmsParamsProvider())
                    .addProvider(new SystemPropertyCmsParamsProvider())
                    .addProvider(new ApplicationPropertiesCmsParamsProvider())
                    .build()
                    .resolve()
                    .orElseThrow(() -> new IllegalStateException("No se pudieron resolver CmsParams"));

                Cms cms = Cms.create(cmsParams);
                FileSignedCmsProvider.save(cms.getSignedValue());
                return cms.getSignedValue();
            });

        return Cms.create(signedCmsBase64);
    }

    /**
     * Genera siempre un CMS fresco firmado con el certificado configurado.
     * El CMS no se persiste, ya que su vigencia depende del TRA generado
     * dentro de la misma operación de solicitud de TA.
     */
    private Cms buildFreshCms() {
        CmsParams cmsParams = ProviderChain.<CmsParams>builder()
            .addProvider(new EnvironmentCmsParamsProvider())
            .addProvider(new SystemPropertyCmsParamsProvider())
            .addProvider(new ApplicationPropertiesCmsParamsProvider())
            .build()
            .resolve()
            .orElseThrow(() -> new IllegalStateException("No se pudieron resolver CmsParams"));

        return Cms.create(cmsParams);
    }

    private static FEAuthRequest toFEAuthRequest(FEAuthParams p) {
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(p.getToken());
        auth.setSign(p.getSign());
        auth.setCuit(p.getCuit());
        return auth;
    }
}
