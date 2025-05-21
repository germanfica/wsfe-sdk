package com.germanfica.wsfe.net;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseApiRequest {
    private final RequestOptions options;
}
