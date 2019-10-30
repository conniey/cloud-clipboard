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
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    private static final String CLIPS_SET = "clips";
    private static final String CLIP_SAVE = "clipSave";
    private static final String SAVE_STATUS = "saveStatus";
    private static final String SECRETS_LIST = "secrets";

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
        return repository.addClip(clip).map(added -> {
            model.addAttribute(SAVE_STATUS, new ClipSaveStatus(true));
            return added;
        }).onErrorContinue((error, ob) -> {
            model.addAttribute(SAVE_STATUS, new ClipSaveStatus(false));
        }).then(Mono.fromCallable(() -> {
            IReactiveDataDriverContextVariable reactorModel =
                    new ReactiveDataDriverContextVariable(repository.getClips(), 1);

            if (!model.containsAttribute(SAVE_STATUS)) {
                model.addAttribute(SAVE_STATUS, new ClipSaveStatus(true));
            }

            model.addAttribute(CLIPS_SET, reactorModel);
            model.addAttribute(CLIP_SAVE, new Clip());
            return "index";
        }));
    }
}
