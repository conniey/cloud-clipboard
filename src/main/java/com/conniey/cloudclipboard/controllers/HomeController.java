package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.repository.ClipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    private final ClipRepository repository;

    @Autowired
    public HomeController(ClipRepository repository) {
        this.repository = repository;
    }

    @RequestMapping("/")
    public String index(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                new ReactiveDataDriverContextVariable(repository.getClips(), 1);

        // classic, wait repository loaded all and display it.
        //model.addAttribute("movies", movieRepository.findAll());

        model.addAttribute("clips", reactiveDataDrivenMode);

        return "index";
    }

    @GetMapping("/clips")
    public String getClip(@RequestParam(name="id")String id, Model model) {
        IReactiveDataDriverContextVariable reactiveDataDriverContextVariable =
                new ReactiveDataDriverContextVariable(repository.getClip(id), 1);

        model.addAttribute("clip", reactiveDataDriverContextVariable);

        return "clip";
    }

    @PostMapping("/")
    public Mono<Clip> upload(@RequestBody Clip clip) {
        return repository.addClip(clip);
    }
}
