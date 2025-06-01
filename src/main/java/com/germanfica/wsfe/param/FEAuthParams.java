package com.germanfica.wsfe.param;

import com.germanfica.wsfe.time.ArcaDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "set")
@EqualsAndHashCode(callSuper = false)
public class FEAuthParams {
    private final String token;
    private final String sign;
    private final long cuit;
    private final ArcaDateTime generationTime;
    private final ArcaDateTime expirationTime;
    public boolean isExpired() {
        return ArcaDateTime.now().isAfter(expirationTime);
    }
}
