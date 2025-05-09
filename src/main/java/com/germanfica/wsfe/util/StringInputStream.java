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

package com.germanfica.wsfe.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Simple wrapper for ByteArrayInputStream that will automatically encode the
 * string as UTF-8 data, and still allows access to the original string.
 */
public class StringInputStream extends ByteArrayInputStream {

    private final String string;

    public StringInputStream(String s) {
        this(s, StandardCharsets.UTF_8);
    }

    public StringInputStream(String s, Charset charset) {
        super(s.getBytes(charset));
        this.string = s;
    }

    /**
     * Returns the original string specified when this input stream was
     * constructed.
     *
     * @return The original string specified when this input stream was
     *         constructed.
     */
    public String getString() {
        return string;
    }
}
