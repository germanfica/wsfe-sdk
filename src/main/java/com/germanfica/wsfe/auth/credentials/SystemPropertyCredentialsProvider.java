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

package com.germanfica.wsfe.auth.credentials;

import com.germanfica.wsfe.auth.credentials.internal.SystemSettingsCredentialsProvider;
import com.germanfica.wsfe.util.SystemSetting;
import com.germanfica.wsfe.util.ToString;

import java.util.Optional;
//import software.amazon.awssdk.annotations.SdkPublicApi;
//import software.amazon.awssdk.auth.credentials.internal.SystemSettingsCredentialsProvider;
//import software.amazon.awssdk.utils.SystemSetting;
//import software.amazon.awssdk.utils.ToString;

/**
 * {@link AwsCredentialsProvider} implementation that loads credentials from the aws.accessKeyId, aws.secretAccessKey and
 * aws.sessionToken system properties.
 */
public final class SystemPropertyCredentialsProvider extends SystemSettingsCredentialsProvider {

    private static final String PROVIDER_NAME = "SystemPropertyCredentialsProvider";

    private SystemPropertyCredentialsProvider() {
    }

    public static SystemPropertyCredentialsProvider create() {
        return new SystemPropertyCredentialsProvider();
    }

    @Override
    protected Optional<String> loadSetting(SystemSetting setting) {
        // CHECKSTYLE:OFF - Customers should be able to specify a credentials provider that only looks at the system properties,
        // but not the environment variables. For that reason, we're only checking the system properties here.
        return Optional.ofNullable(System.getProperty(setting.property()));
        // CHECKSTYLE:ON
    }

    @Override
    protected String provider() {
        return PROVIDER_NAME;
    }

    @Override
    public String toString() {
        return ToString.create(PROVIDER_NAME);
    }
}
