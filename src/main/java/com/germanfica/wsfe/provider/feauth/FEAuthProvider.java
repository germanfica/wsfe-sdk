package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.exception.ApiException;
import fev1.dif.afip.gov.ar.FEAuthRequest;

/**
 * Provee credenciales listas para enviar a WSFE.
 * Implementaciones pueden:
 *   • devolver valores fijos (StaticAuthProvider)
 *   • refrescar el TA cuando expire (RefreshingAuthProvider)
 *   • leer de Profile/Env/etc. (ProviderChain ya existente)
 */
public interface FEAuthProvider {
    FEAuthRequest getAuth() throws ApiException;
}
