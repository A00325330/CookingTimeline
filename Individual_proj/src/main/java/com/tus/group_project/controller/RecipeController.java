package com.tus.group_project.controller;

import com.tus.group_project.dto.RecipeDto;
import com.tus.group_project.model.*;
import com.tus.group_project.service.IRecipeService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final IRecipeService recipeService;

    public RecipeController(IRecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * ✅ Create a new recipe with HATEOAS links and visibility control.
     */
    @PostMapping
    public ResponseEntity<EntityModel<Recipe>> createRecipe(
            @RequestBody RecipeDto recipeDto,
            @AuthenticationPrincipal User user) {

        Recipe newRecipe = new Recipe();
        newRecipe.setName(recipeDto.getName());

        // ✅ Convert ingredient names into RecipeIngredient objects
        List<RecipeIngredient> ingredientEntities = recipeDto.getIngredients().stream()
                .map(name -> new RecipeIngredient(null, newRecipe, name, 5)) // Default 5 mins cook time
                .toList();
        newRecipe.setIngredients(ingredientEntities);

        newRecipe.setSteps(recipeDto.getSteps());
        newRecipe.setCookingMethod(recipeDto.getCookingMethod());

        // ✅ Default visibility to PRIVATE, unless admin sets it to PUBLIC
        if (recipeDto.getVisibility() == Visibility.PUBLIC && !user.isAdmin()) {
            return ResponseEntity.status(403).body(null);
        }
        newRecipe.setVisibility(recipeDto.getVisibility());

        // ✅ Save recipe & calculate cooking time based on longest ingredient
        Recipe savedRecipe = recipeService.createRecipe(newRecipe, user);
        return ResponseEntity.ok(buildRecipeModel(savedRecipe));
    }

    /**
     * ✅ Get all public recipes.
     */
    @GetMapping("/public")
    public ResponseEntity<CollectionModel<EntityModel<Recipe>>> getPublicRecipes() {
        List<EntityModel<Recipe>> recipeModels = recipeService.getPublicRecipes().stream()
                .map(this::buildRecipeModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(recipeModels,
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withSelfRel()));
    }

    /**
     * ✅ Get a single public recipe by ID.
     */
    @GetMapping("/public/{id}") // ✅ Correct: Explicit `/`
    public ResponseEntity<EntityModel<Recipe>> getPublicRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeService.getRecipeById(id);

        if (recipe.isPresent() && recipe.get().getVisibility() == Visibility.PUBLIC) {
            return ResponseEntity.ok(buildRecipeModel(recipe.get()));
        }

        return ResponseEntity.notFound().build();
    }


    /**
     * ✅ Get all recipes owned by the logged-in user.
     */
    @GetMapping("/user")
    public ResponseEntity<CollectionModel<EntityModel<Recipe>>> getUserRecipes(@AuthenticationPrincipal User user) {
        List<EntityModel<Recipe>> recipeModels = recipeService.getUserRecipes(user).stream()
                .map(this::buildRecipeModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(recipeModels,
                linkTo(methodOn(RecipeController.class).getUserRecipes(user)).withSelfRel()));
    }

    /**
     * ✅ Get a private recipe by ID (only if the user is the owner).
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Recipe>> getRecipeById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Optional<Recipe> recipe = recipeService.getRecipeById(id, user);
        return recipe.map(value -> ResponseEntity.ok(buildRecipeModel(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ✅ Update a recipe (only if the user owns it).
     */
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Recipe>> updateRecipe(@PathVariable Long id,
                                                            @RequestBody RecipeDto recipeDto,
                                                            @AuthenticationPrincipal User user) {
        Optional<Recipe> existingRecipe = recipeService.getRecipeById(id, user);

        if (existingRecipe.isPresent()) {
            Recipe recipe = existingRecipe.get();
            recipe.setName(recipeDto.getName());
            recipe.setSteps(recipeDto.getSteps());
            recipe.setCookingMethod(recipeDto.getCookingMethod());

            // ✅ Convert ingredient names into RecipeIngredient objects
            List<RecipeIngredient> ingredientEntities = recipeDto.getIngredients().stream()
                    .map(name -> new RecipeIngredient(null, recipe, name, 5)) // Default 5 mins cook time
                    .toList();
            recipe.setIngredients(ingredientEntities);

            // ✅ Recalculate cooking time
            recipe.setCookingTime(recipeService.calculateRecipeCookTime(recipe.getId()));

            // ✅ Save updated recipe
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe, user);
            return ResponseEntity.ok(buildRecipeModel(updatedRecipe));
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * ✅ Delete a recipe (only if the user owns it).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id, @AuthenticationPrincipal User user) {
        recipeService.deleteRecipe(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ Helper method to build HATEOAS links for a recipe.
     */
    private EntityModel<Recipe> buildRecipeModel(Recipe recipe) {
        EntityModel<Recipe> recipeModel;

        if (recipe.getVisibility() == Visibility.PUBLIC) {
            recipeModel = EntityModel.of(recipe,
                    linkTo(methodOn(RecipeController.class).getPublicRecipeById(recipe.getId())).withSelfRel(), // ✅ For public
                    linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes")
            );
        } else {
            recipeModel = EntityModel.of(recipe,
                    linkTo(methodOn(RecipeController.class).getRecipeById(recipe.getId(), null)).withSelfRel(), // ✅ For private
                    linkTo(methodOn(RecipeController.class).getUserRecipes(null)).withRel("userRecipes")
            );
        }

        // ✅ Common links
        recipeModel.add(
                linkTo(methodOn(RecipeController.class).updateRecipe(recipe.getId(), new RecipeDto(), null))
                        .withRel("update").withType("PUT"),
                linkTo(methodOn(RecipeController.class).deleteRecipe(recipe.getId(), null))
                        .withRel("delete").withType("DELETE")
        );

        // ✅ Only add author link if user exists
        if (recipe.getUser() != null) {
            recipeModel.add(linkTo(methodOn(UserController.class).getUserById(recipe.getUser().getId()))
                    .withRel("author"));
        }

        return recipeModel;
    }

}
