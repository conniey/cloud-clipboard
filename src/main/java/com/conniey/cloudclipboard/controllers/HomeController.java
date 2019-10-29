package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.repository.ClipRepository;
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
    private static final String IS_SAVE_SUCCESS = "isSaveSuccess";

    private final ClipRepository repository;

    @Autowired
    public HomeController(ClipRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                new ReactiveDataDriverContextVariable(repository.getClips(), 1);

        model.addAttribute(CLIPS_SET, reactiveDataDrivenMode);
        model.addAttribute(CLIP_SAVE, new Clip());

        return "index";
    }

    @PostMapping("/clips")
    public Mono<String> saveClip(@ModelAttribute Clip clip, Model model) {
        return repository.addClip(clip).map(added -> {
            model.addAttribute(IS_SAVE_SUCCESS, true);
            return added;
        }).onErrorContinue((error, ob) -> {
            model.addAttribute(IS_SAVE_SUCCESS, false);
        }).then(Mono.fromCallable(() -> {
            IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                    new ReactiveDataDriverContextVariable(repository.getClips(), 1);

            model.addAttribute(CLIPS_SET, reactiveDataDrivenMode);
            model.addAttribute(CLIP_SAVE, new Clip());
            return "index";
        }));
    }
}
