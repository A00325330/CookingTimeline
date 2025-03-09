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
     * ✅ Create a new recipe with individual ingredient cooking methods.
     */
    @PostMapping
    public ResponseEntity<EntityModel<Recipe>> createRecipe(
            @RequestBody RecipeDto recipeDto,
            @AuthenticationPrincipal User user) {

        Recipe newRecipe = new Recipe();
        newRecipe.setName(recipeDto.getName());

        // ✅ Convert DTO ingredients into `RecipeIngredient` objects
        List<RecipeIngredient> ingredientEntities = recipeDto.getIngredients().stream()
                .map(ingredientDto -> new RecipeIngredient(
                        null, 
                        newRecipe, 
                        ingredientDto.getName(), 
                        ingredientDto.getCookingTime(), 
                        ingredientDto.getCookingMethod()  // ✅ Store cooking method per ingredient
                ))
                .toList();
        newRecipe.setIngredients(ingredientEntities);
        newRecipe.setSteps(recipeDto.getSteps());

        // ✅ Default visibility to PRIVATE unless admin sets PUBLIC
        if (recipeDto.getVisibility() == Visibility.PUBLIC && !user.isAdmin()) {
            return ResponseEntity.status(403).body(null);
        }
        newRecipe.setVisibility(recipeDto.getVisibility());

        // ✅ Save recipe & calculate cooking time
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
     * ✅ Get a single public recipe by ID (Includes cooking methods per ingredient).
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<EntityModel<Recipe>> getPublicRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeService.getRecipeById(id);

        if (recipe.isPresent() && recipe.get().getVisibility() == Visibility.PUBLIC) {
            // ✅ Ensure ingredients include cooking methods
            recipe.get().getIngredients().forEach(ingredient -> {
                System.out.println("Ingredient: " + ingredient.getName() + 
                                   ", Cooking Time: " + ingredient.getCookingTime() + 
                                   " mins, Method: " + ingredient.getCookingMethod());
            });

            return ResponseEntity.ok(buildRecipeModel(recipe.get()));
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * ✅ Update a recipe (including cooking methods per ingredient).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id, @AuthenticationPrincipal User user) {
        recipeService.deleteRecipe(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Recipe>> updateRecipe(@PathVariable Long id,
                                                            @RequestBody RecipeDto recipeDto,
                                                            @AuthenticationPrincipal User user) {
        Optional<Recipe> existingRecipe = recipeService.getRecipeById(id, user);

        if (existingRecipe.isPresent()) {
            Recipe recipe = existingRecipe.get();
            recipe.setName(recipeDto.getName());
            recipe.setSteps(recipeDto.getSteps());

            // ✅ Convert DTO ingredients to entity (ensure `cookingMethod` is stored)
            List<RecipeIngredient> ingredientEntities = recipeDto.getIngredients().stream()
                    .map(ingredientDto -> new RecipeIngredient(
                            null, 
                            recipe, 
                            ingredientDto.getName(), 
                            ingredientDto.getCookingTime(), 
                            ingredientDto.getCookingMethod()
                    ))
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
     * ✅ Helper method to build HATEOAS links for a recipe.
     */
    private EntityModel<Recipe> buildRecipeModel(Recipe recipe) {
        EntityModel<Recipe> recipeModel = EntityModel.of(recipe,
                linkTo(methodOn(RecipeController.class).getPublicRecipeById(recipe.getId())).withSelfRel(),
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes"));

        recipeModel.add(
                linkTo(methodOn(RecipeController.class).updateRecipe(recipe.getId(), new RecipeDto(), null))
                        .withRel("update").withType("PUT"),
                linkTo(methodOn(RecipeController.class).deleteRecipe(recipe.getId(), null)) // ✅ Fixed
                        .withRel("delete").withType("DELETE")
        );

        if (recipe.getUser() != null) {
            recipeModel.add(linkTo(methodOn(UserController.class).getUserById(recipe.getUser().getId()))
                    .withRel("author"));
        }

        return recipeModel;
    }

}
