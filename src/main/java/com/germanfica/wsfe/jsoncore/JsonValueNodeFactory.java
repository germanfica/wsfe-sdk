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

package com.germanfica.wsfe.jsoncore;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.germanfica.wsfe.jsoncore.internal.*;

import java.io.IOException;

/**
 * Parses JSON tokens into JsonNode's values. Used only for atomic values.
 */
public interface JsonValueNodeFactory {

    /**
     * Default implementation. Takes the tokens and returns JsonNode values based on its string representation.
     */
    JsonValueNodeFactory DEFAULT = (parser, token) -> {
        switch (token) {
            case VALUE_STRING:
                return new StringJsonNode(parser.getText());
            case VALUE_FALSE:
                return new BooleanJsonNode(false);
            case VALUE_TRUE:
                return new BooleanJsonNode(true);
            case VALUE_NULL:
                return NullJsonNode.instance();
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                return new NumberJsonNode(parser.getText());
            case VALUE_EMBEDDED_OBJECT:
                return new EmbeddedObjectJsonNode(parser.getEmbeddedObject());
            default:
                throw new IllegalArgumentException("Unexpected JSON token - " + token);
        }
    };

    JsonNode node(JsonParser parser, JsonToken token) throws IOException;
}
