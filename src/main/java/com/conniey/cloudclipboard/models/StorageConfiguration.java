package com.conniey.cloudclipboard.models;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Configuration for accessing Azure Blob Storage.
 */
@ConstructorBinding
@ConfigurationProperties("storage")
public class StorageConfiguration {
    private final String accountName;
    private final String accessKey;
    private final String containerName;

    public StorageConfiguration(String accountName, String accessKey, String containerName) {
        this.accountName = accountName;
        this.accessKey = accessKey;
        this.containerName = containerName;
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
}
