package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.AzureConfiguration;
import com.conniey.cloudclipboard.models.KeyVaultConfiguration;
import com.conniey.cloudclipboard.models.Secret;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientSecret;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.AuthenticationResult;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Repository
@Profile("oldsdk")
public class OldKeyVaultRepository implements SecretRepository {
    private final KeyVaultClient keyVaultClient;
    private final String keyVaultUrl;

    @Autowired
    public OldKeyVaultRepository(AzureConfiguration azureConfiguration, KeyVaultConfiguration configuration) {
        this.keyVaultUrl = configuration.getEndpoint();

        final ClientSecret clientSecret = new ClientSecret(azureConfiguration.getClientSecret());
        final String authority = "https://login.microsoftonline.com/" + azureConfiguration.getTenantId();
        this.keyVaultClient = new KeyVaultClient(new KeyVaultCredentials() {
            @Override
            public AuthenticationResult doAuthenticate(String authorization,
                    String resource, String scope, String schema) {
                final ExecutorService service = Executors.newFixedThreadPool(1);
                final ClientCredentialParameters parameters =
                        ClientCredentialParameters.builder(Collections.singleton("https://vault.azure.net/.default"))
                                .build();
                try {
                    final ConfidentialClientApplication application =
                            ConfidentialClientApplication.builder(azureConfiguration.getClientId(), clientSecret)
                                    .authority(authority)
                                    .executorService(service)
                                    .build();

                    return application.acquireToken(parameters)
                            .thenApply(result -> new AuthenticationResult(result.accessToken(), null)).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Problem happened while getting secret.", e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Exception occurred getting authority", e);
                } finally {
                    service.shutdown();
                }
            }
        });
    }

    @Override
    public Flux<Secret> listSecrets() {
        return Flux.create(sink -> {
            boolean hasNextPage;
            PagedList<SecretItem> secrets = keyVaultClient.getSecrets(keyVaultUrl);

            do {
                final Page<SecretItem> current = secrets.currentPage();
                for (SecretItem secret : current.items()) {
                    final SecretBundle secretBundle = keyVaultClient.getSecret(secret.id());
                    final String[] split = secret.id().split("/");
                    final String name = split[split.length - 1];

                    sink.next(new Secret(secret.id(), name, secretBundle.value(), secretBundle.contentType()));
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
    public Mono<Secret> getSecret(String key) {
        if (key == null) {
            return Mono.error(new IllegalArgumentException("'key' cannot be null."));
        }

        final SecretBundle secret = keyVaultClient.getSecret(key);
        return Mono.just(new Secret(secret.id(), secret.id(), secret.value(), secret.contentType()));
    }

    @Override
    public Mono<Secret> addSecret(String key, String value) {
        if (key == null) {
            return Mono.error(new IllegalArgumentException("'key' cannot be null."));
        }

        final SecretBundle secretBundle = keyVaultClient.setSecret(keyVaultUrl, key, value);
        return Mono.just(new Secret(secretBundle.id(), secretBundle.id(), secretBundle.value(), secretBundle.contentType()));
    }
}
