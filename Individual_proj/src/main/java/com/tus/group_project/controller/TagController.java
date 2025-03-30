package com.tus.group_project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tus.group_project.model.Tag;
import com.tus.group_project.service.IRecipeService;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final IRecipeService recipeService;

    public TagController(IRecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(recipeService.getAllTags());
    }
}
