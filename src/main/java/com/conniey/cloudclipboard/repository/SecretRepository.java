package com.conniey.cloudclipboard.repository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SecretRepository {
    /**
     * Gets the names of all the secrets in the repository.
     *
     * @return A stream of all the secret names available in the repository.
     */
    Flux<String> listSecrets();

    /**
     * Gets the value of a secret with the matching key.
     *
     * @param key Key of a secret in the repository.
     * @return Value of a seceret with a matching key.
     * @throws IllegalArgumentException if {@code key} is null or an empty string.
     */
    Mono<String> getSecret(String key);

    /**
     * Adds a secret to the repository. If a secret already exists with the same key, will replace it.
     *
     * @param key The key of the secret.
     * @param value The value to add.
     * @return The value with that matching key that was added.
     */
    Mono<String> addSecret(String key, String value);
}
