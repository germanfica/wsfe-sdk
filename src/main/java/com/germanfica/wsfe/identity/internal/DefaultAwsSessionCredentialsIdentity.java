/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * Modificado por German Fica en 2025 para su integración en un proyecto MIT.
 * Este archivo conserva la licencia Apache 2.0 conforme a sus términos.
 */

package com.germanfica.wsfe.identity.internal;

import com.germanfica.wsfe.identity.AwsSessionCredentialsIdentity;
import com.germanfica.wsfe.util.ToString;
import com.germanfica.wsfe.util.Validate;

import java.util.Objects;
import java.util.Optional;

public final class DefaultAwsSessionCredentialsIdentity implements AwsSessionCredentialsIdentity {

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final String providerName;
    private final String accountId;


    private DefaultAwsSessionCredentialsIdentity(Builder builder) {
        this.accessKeyId = builder.accessKeyId;
        this.secretAccessKey = builder.secretAccessKey;
        this.sessionToken = builder.sessionToken;
        this.providerName = builder.providerName;
        this.accountId = builder.accountId;

        Validate.paramNotNull(accessKeyId, "accessKeyId");
        Validate.paramNotNull(secretAccessKey, "secretAccessKey");
        Validate.paramNotNull(sessionToken, "sessionToken");
    }

    public static AwsSessionCredentialsIdentity.Builder builder() {
        return new Builder();
    }

    @Override
    public String accessKeyId() {
        return accessKeyId;
    }

    @Override
    public String secretAccessKey() {
        return secretAccessKey;
    }

    @Override
    public Optional<String> accountId() {
        return Optional.ofNullable(accountId);
    }

    @Override
    public String sessionToken() {
        return sessionToken;
    }

    @Override
    public Optional<String> providerName() {
        return Optional.ofNullable(providerName);
    }

    @Override
    public String toString() {
        return ToString.builder("AwsSessionCredentialsIdentity")
                .add("accessKeyId", accessKeyId)
                .add("providerName", providerName)
                .add("accountId", accountId)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AwsSessionCredentialsIdentity that = (AwsSessionCredentialsIdentity) o;
        return Objects.equals(accessKeyId, that.accessKeyId()) &&
                Objects.equals(secretAccessKey, that.secretAccessKey()) &&
                Objects.equals(sessionToken, that.sessionToken()) &&
                Objects.equals(accountId, that.accountId().orElse(null));
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + Objects.hashCode(accessKeyId);
        hashCode = 31 * hashCode + Objects.hashCode(secretAccessKey);
        hashCode = 31 * hashCode + Objects.hashCode(sessionToken);
        hashCode = 31 * hashCode + Objects.hashCode(accountId);
        return hashCode;
    }

    private static final class Builder implements AwsSessionCredentialsIdentity.Builder {
        private String accessKeyId;
        private String secretAccessKey;
        private String sessionToken;
        private String providerName;
        private String accountId;

        private Builder() {
        }

        @Override
        public Builder accessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
            return this;
        }

        @Override
        public Builder secretAccessKey(String secretAccessKey) {
            this.secretAccessKey = secretAccessKey;
            return this;
        }

        @Override
        public Builder sessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
            return this;
        }

        @Override
        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }


        @Override
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        @Override
        public AwsSessionCredentialsIdentity build() {
            return new DefaultAwsSessionCredentialsIdentity(this);
        }
    }
}
