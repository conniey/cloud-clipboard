package com.conniey.cloudclipboard.repository;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.conniey.cloudclipboard.models.AuthenticationProvider;
import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.StorageConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
@Profile("production")
public class AzureStorageRepository implements ClipRepository {
    private final ObjectMapper objectMapper;
    private final BlobContainerAsyncClient containerClient;

    @Autowired
    public AzureStorageRepository(AuthenticationProvider authenticationProvider, StorageConfiguration configuration,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.containerClient = new BlobServiceClientBuilder()
                .credential(authenticationProvider.getTokenCredential())
                .endpoint(configuration.getEndpoint())
                .buildAsyncClient()
                .getBlobContainerAsyncClient(configuration.getContainerName());
    }

    @Override
    public Flux<Clip> getClips() {
        return containerClient.listBlobs().flatMap(blob -> containerClient
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
        if (clip == null) {
            return Mono.error(new IllegalArgumentException("'clip' is required."));
        }

        final String id = UUID.randomUUID().toString();

        clip.setId(id).setCreated(OffsetDateTime.now());

        final String serialized;
        try {
            serialized = objectMapper.writeValueAsString(clip);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Unable to serialize clip.", e));
        }

        final ProgressReceiver receiver = progress -> System.out.printf("[%s] Progress: %s%n", id, progress);
        final ParallelTransferOptions options = new ParallelTransferOptions(1096, 4, receiver);

        return containerClient.getBlobAsyncClient(id)
                .upload(Flux.just(StandardCharsets.UTF_8.encode(serialized)), options)
                .thenReturn(clip);
    }
}
