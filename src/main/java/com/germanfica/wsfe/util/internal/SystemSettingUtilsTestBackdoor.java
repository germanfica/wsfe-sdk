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

package com.germanfica.wsfe.util.internal;

import com.germanfica.wsfe.util.SystemSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a backdoor to add overrides to the results of querying {@link SystemSetting}s. This is used for testing environment
 * variables within the SDK
 */
public final class SystemSettingUtilsTestBackdoor {
    private static final Map<String, String> ENVIRONMENT_OVERRIDES = new HashMap<>();

    private SystemSettingUtilsTestBackdoor() {
    }

    public static void addEnvironmentVariableOverride(String key, String value) {
        ENVIRONMENT_OVERRIDES.put(key, value);
    }

    public static void clearEnvironmentVariableOverrides() {
        ENVIRONMENT_OVERRIDES.clear();
    }

    static String getEnvironmentVariable(String key) {
        if (!ENVIRONMENT_OVERRIDES.isEmpty() && ENVIRONMENT_OVERRIDES.containsKey(key)) {
            return ENVIRONMENT_OVERRIDES.get(key);
        }
        // CHECKSTYLE:OFF - This is the only place we should access environment variables
        return System.getenv(key);
        // CHECKSTYLE:ON
    }
}
