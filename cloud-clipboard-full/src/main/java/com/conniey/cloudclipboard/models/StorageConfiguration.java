package com.conniey.cloudclipboard.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;

/**
 * Configuration for accessing Azure Blob Storage.
 */
@ConstructorBinding
@ConfigurationProperties("storage")
public class StorageConfiguration {
    private final String accountName;
    private final String accessKey;
    private final String containerName;
    private final String endpoint;

    public StorageConfiguration(String accountName, String accessKey, String containerName, String endpoint) {
        this.accountName = accountName;
        this.accessKey = accessKey;
        this.containerName = containerName;
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
