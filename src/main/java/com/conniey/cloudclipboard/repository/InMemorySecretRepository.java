package com.conniey.cloudclipboard.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("dev")
public class InMemorySecretRepository implements SecretRepository {
    private final ConcurrentHashMap<String, String> secrets = new ConcurrentHashMap<>();

    @Autowired
    public InMemorySecretRepository() {
        for (int i = 0; i < 5; i++) {
            secrets.put("key " + i, "secret " + i);
        }
    }

    /**
     * Gets the names of all the secrets in the repository.
     *
     * @return A stream of all the secret names available in the repository.
     */
    @Override
    public Flux<String> listSecrets() {
        return Flux.fromIterable(secrets.keySet());
    }

    @Override
    public Mono<String> getSecret(String key) {
        if (key == null || key.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'key' is required and cannot be empty."));
        }

        return null;
    }

    @Override
    public Mono<String> addSecret(String key, String value) {
        if (key == null || key.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'key' is required and cannot be empty."));
        }

        return Mono.fromCallable(() -> secrets.put(key, value));
    }
}
