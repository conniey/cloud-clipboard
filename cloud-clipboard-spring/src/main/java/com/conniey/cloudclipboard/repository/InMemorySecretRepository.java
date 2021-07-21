package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Secret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemorySecretRepository implements SecretRepository {
    private final ConcurrentHashMap<String, Secret> secrets = new ConcurrentHashMap<>();

    @Autowired
    public InMemorySecretRepository() {
        for (int i = 0; i < 5; i++) {
            final String key = "key " + i;
            secrets.put(key, new Secret(key, key, "secret " + i, "text/plain"));
        }
    }

    /**
     * Gets the names of all the secrets in the repository.
     *
     * @return A stream of all the secret names available in the repository.
     */
    @Override
    public Flux<Secret> listSecrets() {
        return Flux.fromIterable(secrets.values());
    }

    @Override
    public Mono<Secret> getSecret(String key) {
        if (key == null || key.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'key' is required and cannot be empty."));
        }

        return null;
    }

    @Override
    public Mono<Secret> addSecret(String key, String value) {
        if (key == null || key.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'key' is required and cannot be empty."));
        }

        return Mono.fromCallable(() -> secrets.put(key, new Secret(key, value)));
    }
}
