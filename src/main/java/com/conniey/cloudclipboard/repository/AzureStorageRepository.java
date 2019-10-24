package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.AzureConfiguration;
import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.StorageConfiguration;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Profile("production")
public class AzureStorageRepository implements ClipRepository {
    private final List<Clip> clips = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final AzureConfiguration configuration;

    @Autowired
    public AzureStorageRepository(AzureConfiguration configuration, StorageConfiguration storageConfiguration)
            throws StorageException, InvalidKeyException, URISyntaxException {
        this.configuration = configuration;
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 0; i < 5; i++) {
            final int id = counter.getAndIncrement();
            final Clip clip = new Clip()
                    .setId(String.valueOf(id))
                    .setContents("Hey! Number: " + id)
                    .setCreated(now.minusMinutes(id));
            clips.add(clip);
        }

        final CloudStorageAccount storageAccount = CloudStorageAccount.parse(
                storageConfiguration.getAccessKey());
        final CloudBlobClient cloudBlobClient = storageAccount.createCloudBlobClient();
        cloudBlobClient.getContainerReference(storageConfiguration.getContainerName());
    }

    @Override
    public Flux<Clip> getClips() {
        return Flux.fromIterable(clips);
    }

    @Override
    public Mono<Clip> getClip(String id) {
        return null;
    }

    @Override
    public Mono<Clip> addClip(Clip clip) {
        return null;
    }
}
