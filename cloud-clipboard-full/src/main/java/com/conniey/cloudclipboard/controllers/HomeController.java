package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.ClipSaveStatus;
import com.conniey.cloudclipboard.repository.ClipRepository;
import com.conniey.cloudclipboard.repository.SecretRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    /**
     * Key to the list of clips available.
     */
    static final String CLIPS_SET = "clips";
    /**
     * Key to the clip to save.
     */
    static final String CLIP_SAVE = "clipSave";
    /**
     * Key to the save status when a clip is saved.
     */
    static final String SAVE_STATUS = "saveStatus";
    /**
     * Key to the secrets item.
     */
    static final String SECRETS_LIST = "secrets";

    private final ClipRepository repository;
    private final SecretRepository secretRepository;

    @Autowired
    public HomeController(ClipRepository repository, SecretRepository secretRepository) {
        this.repository = repository;
        this.secretRepository = secretRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                new ReactiveDataDriverContextVariable(repository.getClips().switchIfEmpty(Mono.empty()), 1);

        model.addAttribute(CLIPS_SET, reactiveDataDrivenMode);
        model.addAttribute(CLIP_SAVE, new Clip());
        model.addAttribute(SAVE_STATUS, new ClipSaveStatus());

        return "index";
    }

    @GetMapping("/secrets")
    public String getSecrets(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenModel =
                new ReactiveDataDriverContextVariable(secretRepository.listSecrets(), 1);

        model.addAttribute(SECRETS_LIST, reactiveDataDrivenModel);

        return "secrets";
    }

    @PostMapping("/clips")
    public Mono<String> saveClip(@ModelAttribute Clip clip, Model model) {
        return repository.addClip(clip)
                .map(e -> new ClipSaveStatus(true))
                .onErrorResume(error -> Mono.just(new ClipSaveStatus(false)))
                .map(status -> {
                    IReactiveDataDriverContextVariable reactorModel =
                            new ReactiveDataDriverContextVariable(repository.getClips(), 1);

                    model.addAttribute(SAVE_STATUS, status);
                    model.addAttribute(CLIPS_SET, reactorModel);
                    model.addAttribute(CLIP_SAVE, new Clip());
                    return "index";
                });
    }

    Flux<Clip> getClips() {
        return repository.getClips();
    }
}
