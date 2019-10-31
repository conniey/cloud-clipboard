package com.conniey.cloudclipboard.repository;

import com.azure.core.credentials.TokenCredential;
import com.azure.identity.credential.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.conniey.cloudclipboard.models.AzureConfiguration;
import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.StorageConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

@Repository
@Profile("production")
public class AzureStorageRepository implements ClipRepository {
    private final ObjectMapper objectMapper;
    private final BlobContainerAsyncClient containerClient;

    @Autowired
    public AzureStorageRepository(AzureConfiguration azureConfiguration, StorageConfiguration configuration,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        final TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(azureConfiguration.getClientId())
                .clientSecret(azureConfiguration.getClientSecret())
                .tenantId(azureConfiguration.getTenantId())
                .build();

        containerClient = new BlobServiceClientBuilder()
                .credential(clientSecretCredential)
                .endpoint(configuration.getEndpoint())
                .buildAsyncClient()
                .getBlobContainerAsyncClient(configuration.getContainerName());
    }

    @Override
    public Flux<Clip> getClips() {
        return containerClient.listBlobsFlat().flatMap(blob -> containerClient
                .getBlobAsyncClient(blob.getName())
                .download()
                .reduce((first, second) -> {
                    first.rewind();
                    second.rewind();
                    final ByteBuffer allocated = ByteBuffer.allocate(first.limit() + second.limit())
                            .put(first).put(second);
                    allocated.flip();
                    return allocated;
                })
                .map(buffer -> {
                    try {
                        return objectMapper.readValue(new ByteBufferBackedInputStream(buffer), Clip.class);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }));
    }

    /**
     * Gets a blob based on its URI.
     *
     * @param id The URI of the storage blob.
     *
     * @return The storage blob associated with the given URI, or an en empty Mono if none could be found.
     */
    @Override
    public Mono<Clip> getClip(String id) {
        if (id == null || id.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'id' is required."));
        }
        return containerClient.getBlobAsyncClient(id)
                .download()
                .reduce((first, second) -> {
                    first.rewind();
                    second.rewind();
                    final ByteBuffer allocated = ByteBuffer.allocate(first.limit() + second.limit())
                            .put(first).put(second);
                    allocated.flip();
                    return allocated;
                })
                .map(buffer -> {
                    try {
                        return objectMapper.readValue(new ByteBufferBackedInputStream(buffer), Clip.class);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    @Override
    public Mono<Clip> addClip(Clip clip) {
        return Mono.empty();
    }
}
