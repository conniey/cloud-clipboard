package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.repository.ClipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ClipsController {
    private final ClipRepository repository;

    @Autowired
    public ClipsController(ClipRepository repository) {
        this.repository = repository;
    }
}
