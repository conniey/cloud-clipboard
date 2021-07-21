package com.conniey.cloudclipboard.repository;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.conniey.cloudclipboard.models.AuthenticationProvider;
import com.conniey.cloudclipboard.models.KeyVaultConfiguration;
import com.conniey.cloudclipboard.models.Secret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Profile("production")
public class KeyVaultRepository implements SecretRepository {
    private final SecretAsyncClient secretClient;

    @Autowired
    public KeyVaultRepository(AuthenticationProvider authenticationProvider, KeyVaultConfiguration configuration) {
        this.secretClient = new SecretClientBuilder()
                .credential(authenticationProvider.getTokenCredential())
                .vaultUrl(configuration.getEndpoint())
                .buildAsyncClient();
    }

    @Override
    public Flux<Secret> listSecrets() {
        return secretClient.listPropertiesOfSecrets()
                .flatMap(secret -> secretClient.getSecret(secret.getName()))
                .map(secret -> new Secret(secret.getId(), secret.getName(), secret.getValue(),
                        secret.getProperties().getContentType()));
    }

    @Override
    public Mono<Secret> getSecret(String key) {
        return secretClient.getSecret(key)
                .map(secret -> new Secret(secret.getId(), secret.getName(), secret.getValue(),
                        secret.getProperties().getContentType()));
    }

    @Override
    public Mono<Secret> addSecret(String key, String value) {
        return secretClient.setSecret(key, value)
                .map(secret -> new Secret(secret.getId(), secret.getName(), secret.getValue(),
                        secret.getProperties().getContentType()));
    }
}
