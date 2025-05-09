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

import com.germanfica.wsfe.identity.IdentityProperty;
import com.germanfica.wsfe.identity.ResolveIdentityRequest;
import com.germanfica.wsfe.util.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DefaultResolveIdentityRequest implements ResolveIdentityRequest {

    private final Map<IdentityProperty<?>, Object> properties;

    private DefaultResolveIdentityRequest(BuilderImpl builder) {
        this.properties = new HashMap<>(builder.properties);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public <T> T property(IdentityProperty<T> property) {
        return (T) properties.get(property);
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    @Override
    public String toString() {
        return ToString.builder("ResolveIdentityRequest")
                .add("properties", properties)
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
        DefaultResolveIdentityRequest that = (DefaultResolveIdentityRequest) o;
        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(properties);
    }

    public static final class BuilderImpl implements Builder {
        private final Map<IdentityProperty<?>, Object> properties = new HashMap<>();

        private BuilderImpl() {
        }

        private BuilderImpl(DefaultResolveIdentityRequest resolveIdentityRequest) {
            this.properties.putAll(resolveIdentityRequest.properties);
        }

        public <T> Builder putProperty(IdentityProperty<T> key, T value) {
            this.properties.put(key, value);
            return this;
        }

        public ResolveIdentityRequest build() {
            return new DefaultResolveIdentityRequest(this);
        }
    }
}
