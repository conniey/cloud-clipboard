package com.conniey.cloudclipboard;

import com.conniey.cloudclipboard.models.AzureConfiguration;
import com.conniey.cloudclipboard.models.KeyVaultConfiguration;
import com.conniey.cloudclipboard.models.StorageConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({StorageConfiguration.class, KeyVaultConfiguration.class, AzureConfiguration.class})
@SpringBootApplication
public class CloudClipboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudClipboardApplication.class, args);
    }
}
