package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Profile("production")
public class AzureStorageRepository implements ClipRepository {
    @Autowired
    public AzureStorageRepository() {

    }

    @Override
    public Flux<Clip> getClips() {
        return null;
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
