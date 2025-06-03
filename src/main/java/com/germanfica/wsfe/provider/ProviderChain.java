package com.germanfica.wsfe.provider;

import com.germanfica.wsfe.util.CredentialsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProviderChain<T> implements CredentialsProvider<T> {

    private final List<CredentialsProvider<T>> providers;

    public ProviderChain(List<CredentialsProvider<T>> providers) {
        this.providers = providers;
    }

    @Override
    public Optional<T> resolve() {
        for (CredentialsProvider<T> provider : providers) {
            Optional<T> result = provider.resolve();
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final List<CredentialsProvider<T>> providers = new ArrayList<>();

        public Builder<T> addProvider(CredentialsProvider<T> provider) {
            providers.add(provider);
            return this;
        }

        public ProviderChain<T> build() {
            return new ProviderChain<>(providers);
        }
    }
}
