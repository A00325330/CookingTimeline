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
import java.util.List;
import java.util.Optional;
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

    /**
     * ✅ Create a new recipe (returns RecipeDto)
     */
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

        // Convert String tags into Tag entities
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

    /**
     * ✅ Get recipes created by the logged-in user (returns a list of RecipeDto)
     */
    @GetMapping("/mine")
    public ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> getMyRecipes() {
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
        List<Recipe> myRecipes = recipeService.getUserRecipes(user);

        List<EntityModel<RecipeDto>> recipeModels = myRecipes.stream()
                .map(r -> buildRecipeModel(r, user))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(recipeModels, linkTo(methodOn(RecipeController.class).getMyRecipes()).withSelfRel())
        );
    }

    /**
     * ✅ Get all public recipes (returns RecipeDto list)
     */
    @GetMapping("/public")
    public ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> getPublicRecipes() {
        List<EntityModel<RecipeDto>> recipeModels = recipeService.getPublicRecipes().stream()
                .map(r -> buildRecipeModel(r, null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(recipeModels, linkTo(methodOn(RecipeController.class).getPublicRecipes()).withSelfRel())
        );
    }

    /**
     * ✅ Get a recipe by ID (only visible if public or belongs to user)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RecipeDto>> getRecipeById(@PathVariable Long id) {
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

    /**
     * ✅ Get recipes by tag (returns RecipeDto list)
     */
    @GetMapping("/by-tag/{tagName}")
    public ResponseEntity<List<RecipeDto>> getRecipesByTag(@PathVariable String tagName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername());
        User user = optionalUser.orElse(null);

        List<RecipeDto> recipes = recipeService.getRecipesByTag(tagName, user)
                .stream()
                .map(RecipeDto::fromEntity)
                .toList();

        return ResponseEntity.ok(recipes);
    }

    /**
     * ✅ Convert Recipe entity to DTO and add HATEOAS links
     */
    private EntityModel<RecipeDto> buildRecipeModel(Recipe recipe, User currentUser) {
        RecipeDto dto = RecipeDto.fromEntity(recipe); // Convert entity to DTO

        EntityModel<RecipeDto> recipeModel = EntityModel.of(
                dto,
                linkTo(methodOn(RecipeController.class).getRecipeById(recipe.getId())).withSelfRel(),
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes")
        );

        // Add author link (avoiding exposing full user data)
        if (recipe.getUser() != null) {
            recipeModel.add(
                    linkTo(methodOn(UserController.class).getUserById(recipe.getUser().getId())).withRel("author")
            );
        }

        // Add "mine" link if the recipe belongs to the current user
        if (currentUser != null && recipe.getUser().getId().equals(currentUser.getId())) {
            recipeModel.add(
                    linkTo(methodOn(RecipeController.class).getMyRecipes()).withRel("mine")
            );
        }

        return recipeModel;
    }
}
