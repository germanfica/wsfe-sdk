package com.germanfica.wsfe.param;

import lombok.Getter;

@Getter
public class CmsParams {
    private String keystorePath;
    private String password;
    private String signer;
    private String dstDn;
    private String service;
    private Long ticketTime;

    private CmsParams() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CmsParams params = new CmsParams();

        public Builder setKeystorePath(String keystorePath) {
            params.keystorePath = keystorePath;
            return this;
        }

        public Builder setPassword(String password) {
            params.password = password;
            return this;
        }

        public Builder setSigner(String signer) {
            params.signer = signer;
            return this;
        }

        public Builder setDstDn(String dstDn) {
            params.dstDn = dstDn;
            return this;
        }

        public Builder setService(String service) {
            params.service = service;
            return this;
        }

        public Builder setTicketTime(Long ticketTime) {
            params.ticketTime = ticketTime;
            return this;
        }

        public CmsParams build() {
            // validaciones opcionales
            if (params.keystorePath == null || params.signer == null) {
                throw new IllegalArgumentException("Faltan campos obligatorios para construir CmsParams.");
            }
            return params;
        }
    }
}
