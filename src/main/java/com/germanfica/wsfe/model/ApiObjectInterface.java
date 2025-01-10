package com.germanfica.wsfe.model;

import com.germanfica.wsfe.net.ApiResponse;

public interface ApiObjectInterface {
    public ApiResponse getLastResponse();

    public void setLastResponse(ApiResponse response);
}
