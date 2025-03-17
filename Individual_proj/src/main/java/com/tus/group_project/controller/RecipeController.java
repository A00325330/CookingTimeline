package com.tus.group_project.controller;

import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.RecipeDto;
import com.tus.group_project.model.*;
import com.tus.group_project.service.IRecipeService;
import org.springframework.hateoas.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final IRecipeService recipeService;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public RecipeController(IRecipeService recipeService, UserRepository userRepository, TagRepository tagRepository) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @PostMapping
    public ResponseEntity<EntityModel<RecipeDto>> createRecipe(@RequestBody RecipeDto recipeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = optionalUser.get();

        // Correct instance method usage here:
        Recipe newRecipe = recipeDto.toEntity(user, tagRepository);
        Recipe savedRecipe = recipeService.createRecipe(newRecipe, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildRecipeModel(savedRecipe, user));
    }


    @GetMapping("/mine")
    public ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> getMyRecipes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        List<EntityModel<RecipeDto>> recipeModels = recipeService.getUserRecipes(user).stream()
            .map(r -> buildRecipeModel(r, user)).collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(recipeModels,
            linkTo(methodOn(RecipeController.class).getMyRecipes()).withSelfRel()));
    }

    @GetMapping("/public")
    public ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> getPublicRecipes() {
        List<EntityModel<RecipeDto>> recipes = recipeService.getPublicRecipes().stream()
            .map(r -> buildRecipeModel(r, null)).collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(recipes,
            linkTo(methodOn(RecipeController.class).getPublicRecipes()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RecipeDto>> getRecipeById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        Recipe recipe = recipeService.getRecipeById(id).orElseThrow();

        if (recipe.getVisibility() == Visibility.PUBLIC || recipe.getUser().equals(user)) {
            return ResponseEntity.ok(buildRecipeModel(recipe, user));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<RecipeDto>> updateRecipe(@PathVariable Long id, @RequestBody RecipeDto recipeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = optionalUser.get();
        Optional<Recipe> existingRecipeOpt = recipeService.getRecipeById(id);
        if (existingRecipeOpt.isEmpty() || !existingRecipeOpt.get().getUser().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Recipe existingRecipe = existingRecipeOpt.get();
        recipeDto.updateEntity(existingRecipe, tagRepository);
        Recipe updatedRecipe = recipeService.updateRecipe(id, existingRecipe, user);

        return ResponseEntity.ok(buildRecipeModel(updatedRecipe, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RepresentationModel<?>> deleteRecipe(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            recipeService.deleteRecipe(id, user);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }

        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(RecipeController.class).getMyRecipes()).withRel("mine"));
        model.add(linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes"));
        model.add(linkTo(methodOn(RecipeController.class).createRecipe(null)).withRel("createRecipe"));

        return ResponseEntity.ok(model);
    }




    private EntityModel<RecipeDto> buildRecipeModel(Recipe recipe, User currentUser) {
        RecipeDto dto = RecipeDto.fromEntity(recipe);
        EntityModel<RecipeDto> recipeModel = EntityModel.of(dto,
            linkTo(methodOn(RecipeController.class).getRecipeById(recipe.getId())).withSelfRel(),
            linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes"));

        if (recipe.getUser() != null) {
            recipeModel.add(linkTo(methodOn(UserController.class)
                .getUserById(recipe.getUser().getId())).withRel("author"));
        }

        if (currentUser != null && recipe.getUser().equals(currentUser)) {
            recipeModel.add(linkTo(methodOn(RecipeController.class)
                .getMyRecipes()).withRel("mine"));

            recipeService.getUserRecipes(currentUser).forEach(userRecipe ->
                recipeModel.add(linkTo(methodOn(RecipeController.class)
                    .getRecipeById(userRecipe.getId())).withRel("recipe_" + userRecipe.getId())));
        }

        return recipeModel;
    }
}
