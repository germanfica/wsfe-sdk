package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.auth.credentials.*;
import com.germanfica.wsfe.util.Logger;

public class AwsCredentialsProviderChainExample {
    private static final Logger log = Logger.loggerFor(AwsCredentialsProviderChainExample.class);

    public static void main(String[] args) {
        log.info(() -> "Inicio del programa 🚀");

        // Cadena de proveedores: intenta en este orden hasta encontrar uno válido
        AwsCredentialsProvider credentialsProvider = AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())  // AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())      // -Daws.accessKeyId -Daws.secretAccessKey
                .addCredentialsProvider(ProfileCredentialsProvider.create())             // ~/.aws/credentials
                .build();

        AwsCredentials creds = credentialsProvider.resolveCredentials();

    }
}
