package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public interface ClipRepository {
    Flux<Clip> getClips();

    Mono<Clip> getClip(String id);

    Mono<Clip> addClip(Clip clip);

    default Mono<Void> addClips(List<Clip> clips) {
        return Mono.when(clips.stream().map(clip -> addClip(clip).then()).collect(Collectors.toList()));
    }
}
