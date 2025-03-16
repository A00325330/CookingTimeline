package com.tus.group_project.controller;

import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.RecipeDto;
import com.tus.group_project.model.*;
import com.tus.group_project.service.IRecipeService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final IRecipeService recipeService;
    private final UserRepository userRepository;
    private final TagRepository tagRepository; // ✅ Inject TagRepository

    public RecipeController(IRecipeService recipeService, UserRepository userRepository, TagRepository tagRepository) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository; // ✅ Initialize it
    }

    @PostMapping
    public ResponseEntity<EntityModel<Recipe>> createRecipe(@RequestBody RecipeDto recipeDto) {
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
        Recipe newRecipe = new Recipe();
        newRecipe.setName(recipeDto.getName());
        newRecipe.setDescription(recipeDto.getDescription());
        newRecipe.setSteps(recipeDto.getSteps());
        newRecipe.setVisibility(recipeDto.getVisibility());
        newRecipe.setUser(user);

        List<RecipeIngredient> ingredientEntities = recipeDto.getIngredients().stream()
                .map(ingredientDto -> new RecipeIngredient(
                        null,
                        newRecipe,
                        ingredientDto.getName(),
                        ingredientDto.getCookingTime(),
                        ingredientDto.getCookingMethod()
                ))
                .toList();
        newRecipe.setIngredients(ingredientEntities);

        newRecipe.setCookingTime(
                ingredientEntities.stream()
                        .mapToInt(RecipeIngredient::getCookingTime)
                        .max()
                        .orElse(0)
        );

        // ✅ Convert String tags into Tag entities
        List<Tag> tags = new ArrayList<>();
        if (recipeDto.getTags() != null) {
            for (String tagName : recipeDto.getTags()) {
                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });
                tags.add(tag);
            }
        }
        newRecipe.setTags(tags);

        Recipe savedRecipe = recipeService.createRecipe(newRecipe, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildRecipeModel(savedRecipe, user));
    }



    @GetMapping("/mine")
    public ResponseEntity<CollectionModel<EntityModel<Recipe>>> getMyRecipes() {
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
        List<Recipe> myRecipes = recipeService.getUserRecipes(user)
                .stream()
                .map(this::detachRecipe)  // ✅ Ensures a detached copy before returning
                .toList();

        List<EntityModel<Recipe>> recipeModels = myRecipes.stream()
                .map(r -> buildRecipeModel(r, user))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(recipeModels, linkTo(methodOn(RecipeController.class).getMyRecipes()).withSelfRel())
        );
    }

    /**
     * ✅ Creates a detached copy of the recipe to prevent ConcurrentModificationException.
     */
    private Recipe detachRecipe(Recipe recipe) {
        Recipe copy = new Recipe();
        copy.setId(recipe.getId());
        copy.setName(recipe.getName());
        copy.setDescription(recipe.getDescription());
        copy.setSteps(new ArrayList<>(recipe.getSteps())); // Copy steps
        copy.setIngredients(new ArrayList<>(recipe.getIngredients())); // Copy ingredients
        copy.setVisibility(recipe.getVisibility());
        copy.setUser(recipe.getUser());
        
        // ✅ Copy tags safely
        copy.setTags(new ArrayList<>(recipe.getTags()));

        return copy;
    }


    @GetMapping("/public")
    public ResponseEntity<CollectionModel<EntityModel<Recipe>>> getPublicRecipes() {
        List<EntityModel<Recipe>> recipeModels = recipeService.getPublicRecipes().stream()
                .map(r -> buildRecipeModel(r, null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(recipeModels, linkTo(methodOn(RecipeController.class).getPublicRecipes()).withSelfRel())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Recipe>> getRecipeById(@PathVariable Long id) {
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
        Optional<Recipe> recipeOpt = recipeService.getRecipeById(id);
        if (recipeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Recipe foundRecipe = recipeOpt.get();
        if (foundRecipe.getVisibility() == Visibility.PUBLIC || foundRecipe.getUser().equals(user)) {
            return ResponseEntity.ok(buildRecipeModel(foundRecipe, user));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/by-tag/{tagName}")
    public ResponseEntity<List<Recipe>> getRecipesByTag(@PathVariable String tagName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            System.out.println("❌ No valid authentication found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
        User user = optionalUser.orElse(null);

        System.out.println("✅ Authenticated user: " + (user != null ? user.getEmail() : "Guest"));

        return ResponseEntity.ok(recipeService.getRecipesByTag(tagName, user));
    }


    private EntityModel<Recipe> buildRecipeModel(Recipe recipe, User currentUser) {
        EntityModel<Recipe> recipeModel = EntityModel.of(
                recipe,
                linkTo(methodOn(RecipeController.class).getRecipeById(recipe.getId())).withSelfRel(),
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes")
        );

        if (recipe.getUser() != null) {
            recipeModel.add(
                    linkTo(methodOn(UserController.class).getUserById(recipe.getUser().getId())).withRel("author")
            );
        }

        if (currentUser != null && recipe.getUser().equals(currentUser)) {
            recipeModel.add(
                    linkTo(methodOn(RecipeController.class).getMyRecipes()).withRel("mine")
            );
        }

        return recipeModel;
    }
}