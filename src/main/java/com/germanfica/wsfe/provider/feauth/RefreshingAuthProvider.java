package com.germanfica.wsfe.provider.feauth;


import com.germanfica.wsfe.WsaaClient;
import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.dto.ErrorDto;
import com.germanfica.wsfe.exception.ApiException;
import com.germanfica.wsfe.net.HttpStatus;
import com.germanfica.wsfe.param.CmsParams;
import com.germanfica.wsfe.param.FEAuthParams;
import com.germanfica.wsfe.time.ArcaDateTime;
import com.germanfica.wsfe.util.*;
import fev1.dif.afip.gov.ar.FEAuthRequest;

public class RefreshingAuthProvider implements FEAuthProvider {
    private final WsaaClient wsaa;            // ya lo tenés
    private FEAuthParams cache;

    public RefreshingAuthProvider(WsaaClient wsaa) {
        this.wsaa = wsaa;
    }

    @Override
    public synchronized FEAuthRequest getAuth() throws ApiException {
        if (cache == null || cache.isExpired()) {
            String xml = wsaa.authService().autenticar(buildCmsAutomatically());

            try {
                XMLExtractor.LoginTicketData data = new XMLExtractor(xml).extractLoginTicketData();

                cache = FEAuthParams.builder()
                    .setToken(data.token)
                    .setSign(data.sign)
                    .setCuit(cache.getCuit())                       // CUIT real no requiere renovación
                    .setGenerationTime(ArcaDateTime.parse(data.generationTime))
                    .setExpirationTime(ArcaDateTime.parse(data.expirationTime))
                    .build();

            } catch (Exception e) {
                throw new ApiException(
                    new ErrorDto("invalid_xml", "No se pudo parsear loginTicketResponse", null),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        }
        return toFEAuthRequest(cache);
    }

    private String buildCmsAutomatically() {
        CmsParams cmsParams = ProviderChain.<CmsParams>builder()
            .addProvider(new EnvironmentCmsParamsProvider())
            .addProvider(new SystemPropertyCmsParamsProvider())
            .addProvider(new ApplicationPropertiesCmsParamsProvider())
            .build()
            .resolve()
            .orElseThrow(() -> new IllegalStateException("No se pudieron resolver CmsParams"));

        return Cms.create(cmsParams).getSignedValue();   // Base64 listo
    }

    private static FEAuthRequest toFEAuthRequest(FEAuthParams p) {
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(p.getToken());
        auth.setSign(p.getSign());
        auth.setCuit(p.getCuit());
        return auth;
    }
}
