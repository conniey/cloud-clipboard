package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Profile("production")
public class AzureStorageRepository implements ClipRepository {
    @Override
    public Flux<Clip> getClips() {
        return null;
    }

    @Override
    public Mono<Clip> getClip(String id) {
        return null;
    }
}
