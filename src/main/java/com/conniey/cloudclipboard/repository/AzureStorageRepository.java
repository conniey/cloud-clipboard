package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.StorageConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
@Profile("production")
public class AzureStorageRepository implements ClipRepository {
    private final CloudBlobContainer containerClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AzureStorageRepository(StorageConfiguration configuration, ObjectMapper objectMapper)
            throws StorageException, URISyntaxException {
        this.objectMapper = objectMapper;
        this.containerClient = initializeClient(configuration);
    }

    @Override
    public Flux<Clip> getClips() {
        return fetchBlobs();
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

        try {
            if (!containerClient.exists()) {
                return Mono.empty();
            }
        } catch (StorageException e) {
            return Mono.error(e);
        }

        return Mono.create(sink -> {
            try {
                final CloudBlockBlob blob = containerClient.getBlockBlobReference(id);
                if (!blob.exists()) {
                    sink.success();
                    return;
                }

                final String contents = blob.downloadText();
                final Clip clip = objectMapper.convertValue(contents, Clip.class);
                sink.success(clip);

            } catch (URISyntaxException | StorageException e) {
                sink.error(e);
            } catch (IOException e) {
                sink.error(new RuntimeException("Could not download blob: " + id, e));
            }
        });
    }

    @Override
    public Mono<Clip> addClip(Clip clip) {
        if (clip == null) {
            return Mono.error(new IllegalArgumentException("'clip' is required."));
        }

        return Mono.fromCallable(() -> {
            final String id = UUID.randomUUID().toString();
            final CloudBlockBlob reference;
            try {
                reference = containerClient.getBlockBlobReference(id);
            } catch (URISyntaxException | StorageException e) {
                throw Exceptions.propagate(e);
            }

            clip.setId(id)
                    .setCreated(OffsetDateTime.now());

            try {
                final String serialized = objectMapper.writeValueAsString(clip);
                reference.uploadText(serialized);
                return clip;
            } catch (JsonProcessingException e) {
                throw Exceptions.propagate(new RuntimeException("Could not serialize clip.", e));
            }
        });
    }

    private CloudBlobContainer initializeClient(StorageConfiguration configuration)
            throws URISyntaxException, StorageException {
        final StorageCredentials credentials = new StorageCredentialsAccountAndKey(
                configuration.getAccountName(), configuration.getAccessKey());
        final CloudStorageAccount storageAccount = new CloudStorageAccount(credentials, true);
        final CloudBlobClient cloudBlobClient = storageAccount.createCloudBlobClient();
        return cloudBlobClient.getContainerReference(configuration.getContainerName());
    }

    private Flux<Clip> fetchBlobs() {
        return Flux.create(sink -> {
            try {
                containerClient.createIfNotExists();
            } catch (StorageException ex) {
                sink.error(ex);
                return;
            }

            ResultContinuation continuation = null;
            boolean hasMoreResults;
            do {
                final ResultSegment<ListBlobItem> segment;

                try {
                    segment = containerClient.listBlobsSegmented(
                            null, true, null, 50, continuation,
                            null, null);
                } catch (StorageException e) {
                    final String marker = continuation != null ? continuation.getNextMarker() : "n/a";
                    throw Exceptions.propagate(
                            new RuntimeException("Could not fetch more blobs with continuation: " + marker, e));
                }

                for (ListBlobItem blob : segment.getResults()) {
                    final String path = blob.getUri().getPath();
                    final String[] split = path.split("/");
                    final String blobName = split[split.length - 1];

                    try {
                        final CloudBlockBlob blockBlobReference = containerClient.getBlockBlobReference(blobName);
                        final String contents = blockBlobReference.downloadText();
                        final Clip clip = objectMapper.readValue(contents, Clip.class);
                        sink.next(clip);

                    } catch (URISyntaxException | StorageException | IOException e) {
                        System.err.println("Error fetching blob: " + e);
                    }
                }

                continuation = segment.getContinuationToken();
                hasMoreResults = segment.getHasMoreResults();
            } while (hasMoreResults);

            sink.complete();
        });
    }
}
