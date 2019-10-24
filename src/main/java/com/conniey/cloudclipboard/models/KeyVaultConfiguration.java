package com.conniey.cloudclipboard.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Configuration for accessing Azure Key Vault.
 */
@ConstructorBinding
@ConfigurationProperties("keyvault")
public class KeyVaultConfiguration {
    private String url;

    public KeyVaultConfiguration(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
