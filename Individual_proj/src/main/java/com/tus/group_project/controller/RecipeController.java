package com.tus.group_project.controller;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final IRecipeService recipeService;
    private final UserRepository userRepository;

    public RecipeController(IRecipeService recipeService, UserRepository userRepository) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
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

        Recipe savedRecipe = recipeService.createRecipe(newRecipe, user);
        return ResponseEntity.ok(buildRecipeModel(savedRecipe));
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
        List<Recipe> myRecipes = recipeService.getUserRecipes(user);
        List<EntityModel<Recipe>> recipeModels = myRecipes.stream()
                .map(this::buildRecipeModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(
                        recipeModels,
                        linkTo(methodOn(RecipeController.class).getMyRecipes()).withSelfRel()
                )
        );
    }

    @GetMapping("/public")
    public ResponseEntity<CollectionModel<EntityModel<Recipe>>> getPublicRecipes() {
        List<EntityModel<Recipe>> recipeModels = recipeService.getPublicRecipes().stream()
                .map(this::buildRecipeModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                CollectionModel.of(
                        recipeModels,
                        linkTo(methodOn(RecipeController.class).getPublicRecipes()).withSelfRel()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Recipe>> getRecipeById(@PathVariable Long id) {
        System.out.println("🔍 DEBUG: Fetching recipe with ID " + id);

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
        System.out.println("✅ DEBUG: Authenticated user " + user.getEmail() + " (ID: " + user.getId() + ")");

        Optional<Recipe> recipe = recipeService.getRecipeById(id); // NO VISIBILITY FILTER
        if (recipe.isEmpty()) {
            System.out.println("❌ DEBUG: Recipe not found in DB!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Recipe foundRecipe = recipe.get();
        System.out.println("🔍 DEBUG: Recipe ID " + foundRecipe.getId() + " | Owner ID: " + foundRecipe.getUser().getId() + " | Visibility: " + foundRecipe.getVisibility());

        // ✅ Allow access if the recipe is PUBLIC or the current user is the owner
        if (foundRecipe.getVisibility() == Visibility.PUBLIC || foundRecipe.getUser().equals(user)) {
            System.out.println("✅ DEBUG: User has access to this recipe!");
            return ResponseEntity.ok(buildRecipeModel(foundRecipe));
        }

        System.out.println("❌ DEBUG: User is not authorized to access this recipe!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }



    private EntityModel<Recipe> buildRecipeModel(Recipe recipe) {
        EntityModel<Recipe> recipeModel = EntityModel.of(
                recipe,
                linkTo(methodOn(RecipeController.class).getRecipeById(recipe.getId())).withSelfRel(),
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes")
        );

        if (recipe.getUser() != null) {
            recipeModel.add(
                    linkTo(methodOn(UserController.class).getUserById(recipe.getUser().getId()))
                            .withRel("author")
            );
        }

        return recipeModel;
    }
}