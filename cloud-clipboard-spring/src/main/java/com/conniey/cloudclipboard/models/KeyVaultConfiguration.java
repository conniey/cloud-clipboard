package com.conniey.cloudclipboard.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Configuration for accessing Azure Key Vault.
 */
@ConstructorBinding
@ConfigurationProperties("keyvault")
public class KeyVaultConfiguration {
    private String endpoint;

    public KeyVaultConfiguration(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the endpoint for the Key Vault.
     *
     * @return The endpoint for the Key Vault.
     */
    public String getEndpoint() {
        return endpoint;
    }
}
