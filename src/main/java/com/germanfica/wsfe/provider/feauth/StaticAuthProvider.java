package com.germanfica.wsfe.provider.feauth;

import com.germanfica.wsfe.param.FEAuthParams;
import fev1.dif.afip.gov.ar.FEAuthRequest;

public class StaticAuthProvider implements FEAuthProvider {

    private final FEAuthParams params;

    public StaticAuthProvider(FEAuthParams params) {
        this.params = params;
    }

    @Override
    public FEAuthRequest getAuth() {
        return toFEAuthRequest(params);
    }

    private static FEAuthRequest toFEAuthRequest(FEAuthParams p) {
        FEAuthRequest auth = new FEAuthRequest();
        auth.setToken(p.getToken());
        auth.setSign(p.getSign());
        auth.setCuit(p.getCuit());
        return auth;
    }
}
