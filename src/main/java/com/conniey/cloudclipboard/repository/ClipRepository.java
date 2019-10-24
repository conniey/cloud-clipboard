package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClipRepository {
    Flux<Clip> getClips();

    Mono<Clip> getClip(String id);

    Mono<Clip> addClip(Clip clip);
}
