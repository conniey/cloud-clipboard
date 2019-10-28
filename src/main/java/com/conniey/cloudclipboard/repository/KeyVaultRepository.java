package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.AzureConfiguration;
import com.conniey.cloudclipboard.models.KeyVaultConfiguration;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientSecret;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.AuthenticationResult;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.SecretItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Repository
@Profile("production")
public class KeyVaultRepository implements SecretRepository {
    private final KeyVaultClient keyVaultClient;
    private final String keyVaultUrl;

    @Autowired
    public KeyVaultRepository(AzureConfiguration azureConfiguration, KeyVaultConfiguration configuration) {
        this.keyVaultUrl = configuration.getUrl();
        this.keyVaultClient = initializeClient(azureConfiguration);
    }

    @Override
    public Flux<String> listSecrets() {
        return Flux.create(sink -> {
            boolean hasNextPage;
            PagedList<SecretItem> secrets = keyVaultClient.getSecrets(keyVaultUrl);

            do {
                final Page<SecretItem> current = secrets.currentPage();
                for (SecretItem secret : current.items()) {
                    sink.next(secret.id());
                }

                hasNextPage = secrets.hasNextPage();
                if (hasNextPage) {
                    try {
                        secrets.nextPage(current.nextPageLink());
                    } catch (IOException e) {
                        sink.error(e);
                        break;
                    }
                }

            } while (hasNextPage);
        });
    }

    @Override
    public Mono<String> getSecret(String key) {
        if (key == null) {
            return Mono.error(new IllegalArgumentException("'key' cannot be null."));
        }

        return Mono.just(keyVaultClient.getSecret(key).value());
    }

    @Override
    public Mono<String> addSecret(String key, String value) {
        if (key == null) {
            return Mono.error(new IllegalArgumentException("'key' cannot be null."));
        }

        return Mono.just(keyVaultClient.setSecret(keyVaultUrl, key, value).value());
    }

    private KeyVaultClient initializeClient(AzureConfiguration azureConfiguration) {
        final ClientSecret clientSecret = new ClientSecret(azureConfiguration.getClientSecret());
        final ConfidentialClientApplication application =
                ConfidentialClientApplication.builder(azureConfiguration.getClientId(), clientSecret)
                        .build();
        return new KeyVaultClient(new KeyVaultCredentials() {
            @Override
            public AuthenticationResult doAuthenticate(String authorization,
                    String resource, String scope, String schema) {
                final ClientCredentialParameters parameters =
                        ClientCredentialParameters.builder(Collections.singleton(scope))
                                .build();
                try {
                    return application.acquireToken(parameters)
                            .thenApply(result -> new AuthenticationResult(result.accessToken(), null)).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Problem happened while getting secret.", e);
                }
            }
        });
    }
}
