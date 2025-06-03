package com.germanfica.wsfe.provider;

import java.util.Optional;

@FunctionalInterface
public interface CredentialsProvider<T> {
    Optional<T> resolve();
}
