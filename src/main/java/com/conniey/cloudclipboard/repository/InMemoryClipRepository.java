package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@Profile("dev")
public class InMemoryClipRepository implements ClipRepository {
    private final List<Clip> clips = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(1);

    @Autowired
    public InMemoryClipRepository() {
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 0; i < 5; i++) {
            final int id = counter.getAndIncrement();
            final Clip clip = new Clip()
                    .setId(String.valueOf(id))
                    .setContents("Hey! Number: " + id)
                    .setCreated(now.minusMinutes(id));
            clips.add(clip);
        }
    }

    @Override
    public Flux<Clip> getClips() {
        return Flux.fromIterable(clips).delayElements(Duration.ofMillis(300));
    }

    @Override
    public Mono<Clip> getClip(String id) {
        return Flux.fromIterable(clips).filter(x -> x.getId().equals(id)).next();
    }

    @Override
    public Mono<Clip> addClip(Clip clip) {
        if (clip == null) {
            return Mono.error(new IllegalArgumentException("'clip' cannot be null."));
        }

        return Mono.fromRunnable(() -> {
            final int id = counter.getAndIncrement();
            clip.setCreated(OffsetDateTime.now());
            clip.setId(String.valueOf(id));
            clips.add(clip);
        });
    }
}
