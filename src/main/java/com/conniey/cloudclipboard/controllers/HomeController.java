package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.repository.ClipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

@Controller
public class HomeController {
    @Autowired
    private ClipRepository repository;

    @RequestMapping("/")
    public String index(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                new ReactiveDataDriverContextVariable(repository.getClips(), 1);

        // classic, wait repository loaded all and display it.
        //model.addAttribute("movies", movieRepository.findAll());

        model.addAttribute("clips", reactiveDataDrivenMode);

        return "index";
    }
}
