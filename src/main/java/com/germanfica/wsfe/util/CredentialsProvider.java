package com.germanfica.wsfe.util;

import java.util.Optional;

@FunctionalInterface
public interface CredentialsProvider<T> {
    Optional<T> resolve();
}
