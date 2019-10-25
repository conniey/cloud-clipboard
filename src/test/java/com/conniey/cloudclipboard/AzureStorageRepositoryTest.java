package com.conniey.cloudclipboard;

import com.conniey.cloudclipboard.models.StorageConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AzureStorageRepositoryTest {

    @Autowired
    private StorageConfiguration configuration;

    @Test
    public void getBook() {
        assertThat(configuration.getContainerName()).isNotNull();

    }
}
