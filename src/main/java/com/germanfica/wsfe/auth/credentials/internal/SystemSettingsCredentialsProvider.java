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

package com.germanfica.wsfe.auth.credentials.internal;

import com.germanfica.wsfe.auth.credentials.AwsBasicCredentials;
import com.germanfica.wsfe.auth.credentials.AwsCredentials;
import com.germanfica.wsfe.auth.credentials.AwsCredentialsProvider;
import com.germanfica.wsfe.auth.credentials.AwsSessionCredentials;
import com.germanfica.wsfe.exception.SdkClientException;
import com.germanfica.wsfe.net.SdkSystemSetting;
import com.germanfica.wsfe.util.StringUtils;
import com.germanfica.wsfe.util.SystemSetting;

import static com.germanfica.wsfe.util.StringUtils.trim;

import java.util.Optional;

/**
 * Loads credentials providers from the {@link SdkSystemSetting#AWS_ACCESS_KEY_ID},
 * {@link SdkSystemSetting#AWS_SECRET_ACCESS_KEY}, and {@link SdkSystemSetting#AWS_SESSION_TOKEN} system settings.
 *
 * This does not load the credentials directly. Instead, the actual mapping of setting to credentials is done by child classes.
 * This allows us to separately load the credentials from system properties and environment variables so that customers can
 * remove one or the other from their credential chain, or build a different chain with these pieces of functionality separated.
 *
 * @see EnvironmentVariableCredentialsProvider
 * @see SystemPropertyCredentialsProvider
 */
public abstract class SystemSettingsCredentialsProvider implements AwsCredentialsProvider {

    @Override
    public AwsCredentials resolveCredentials() {
        String accessKey = trim(loadSetting(SdkSystemSetting.AWS_ACCESS_KEY_ID).orElse(null));
        String secretKey = trim(loadSetting(SdkSystemSetting.AWS_SECRET_ACCESS_KEY).orElse(null));
        String sessionToken = trim(loadSetting(SdkSystemSetting.AWS_SESSION_TOKEN).orElse(null));
        String accountId = trim(loadSetting(SdkSystemSetting.AWS_ACCOUNT_ID).orElse(null));

        if (StringUtils.isBlank(accessKey)) {
            throw SdkClientException.builder()
                    .message(String.format("Unable to load credentials from system settings. Access key must be" +
                                    " specified either via environment variable (%s) or system property (%s).",
                            SdkSystemSetting.AWS_ACCESS_KEY_ID.environmentVariable(),
                            SdkSystemSetting.AWS_ACCESS_KEY_ID.property()))
                    .build();
        }

        if (StringUtils.isBlank(secretKey)) {
            throw SdkClientException.builder()
                    .message(String.format("Unable to load credentials from system settings. Secret key must be" +
                                    " specified either via environment variable (%s) or system property (%s).",
                            SdkSystemSetting.AWS_SECRET_ACCESS_KEY.environmentVariable(),
                            SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property()))
                    .build();
        }

        return StringUtils.isBlank(sessionToken) ? AwsBasicCredentials.builder()
                .accessKeyId(accessKey)
                .secretAccessKey(secretKey)
                .accountId(accountId)
                .providerName(provider())
                .build()
                : AwsSessionCredentials.builder()
                .accessKeyId(accessKey)
                .secretAccessKey(secretKey)
                .sessionToken(sessionToken)
                .accountId(accountId)
                .providerName(provider())
                .build();
    }

    /**
     * Implemented by child classes to load the requested setting.
     */
    protected abstract Optional<String> loadSetting(SystemSetting setting);

    protected abstract String provider();
}
