package com.conniey.cloudclipboard.repository;

import com.conniey.cloudclipboard.models.Clip;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("dev")
public class InMemoryClipRepository implements ClipRepository {
    private final static List<Clip> clips = new ArrayList<>();

    static {
        OffsetDateTime now = OffsetDateTime.now();
        clips.add(new Clip().setId("1").setContents("Heyo 1").setCreated(now.minusMinutes(1)));
        clips.add(new Clip().setId("2").setContents("Heyo 2").setCreated(now.minusMinutes(2)));
        clips.add(new Clip().setId("3").setContents("Heyo 3").setCreated(now.minusMinutes(3)));
        clips.add(new Clip().setId("4").setContents("Heyo 4").setCreated(now.minusMinutes(4)));
        clips.add(new Clip().setId("5").setContents("Heyo 5").setCreated(now.minusMinutes(5)));
    }

    @Override
    public Flux<Clip> getClips() {
        return Flux.fromIterable(clips).delayElements(Duration.ofSeconds(1));
    }

    @Override
    public Mono<Clip> getClip(String id) {
        return Flux.fromIterable(clips).filter(x -> x.getId().equals(id)).next();
    }
}
