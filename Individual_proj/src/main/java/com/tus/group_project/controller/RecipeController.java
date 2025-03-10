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
     * ✅ Create a new temporary recipe for non-logged-in users.
     */
    @PostMapping("/temp")
    public ResponseEntity<EntityModel<Recipe>> createTemporaryRecipe() {
        Recipe tempRecipe = recipeService.createOrGetTemporaryRecipe();
        return ResponseEntity.ok(buildRecipeModel(tempRecipe));
    }
    /**
     * ✅ Get the temporary recipe (for non-logged-in users).
     */
    @GetMapping("/temp/{id}")
    public ResponseEntity<EntityModel<Recipe>> getTemporaryRecipe(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeService.getTemporaryRecipeById(id);

        return recipe.map(this::buildRecipeModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * ✅ Add an ingredient to a temporary recipe.
     */
    @PostMapping("/temp/{id}/ingredients")
    public ResponseEntity<EntityModel<Recipe>> addIngredientToTempRecipe(@PathVariable Long id,
                                                                         @RequestBody RecipeDto.IngredientDto ingredientDto) {
        RecipeIngredient ingredient = new RecipeIngredient(null, null, ingredientDto.getName(), ingredientDto.getCookingTime(), ingredientDto.getCookingMethod());
        Recipe updatedRecipe = recipeService.addIngredientToTemporaryRecipe(id, ingredient);
        return updatedRecipe != null ? ResponseEntity.ok(buildRecipeModel(updatedRecipe)) : ResponseEntity.notFound().build();
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
        newRecipe.setDescription(recipeDto.getDescription());
        newRecipe.setSteps(recipeDto.getSteps());
        newRecipe.setVisibility(recipeDto.getVisibility());

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

        newRecipe.setCookingTime(ingredientEntities.stream()
                .mapToInt(RecipeIngredient::getCookingTime)
                .max()
                .orElse(0));

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
    @GetMapping("/public/{id}")
    public ResponseEntity<EntityModel<Recipe>> getPublicRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeService.getRecipeById(id);

        if (recipe.isPresent() && recipe.get().getVisibility() == Visibility.PUBLIC) {
            return ResponseEntity.ok(buildRecipeModel(recipe.get()));
        }

        return ResponseEntity.notFound().build();
    }


    /**
     * ✅ Update a recipe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Recipe>> updateRecipe(@PathVariable Long id,
                                                            @RequestBody RecipeDto recipeDto,
                                                            @AuthenticationPrincipal User user) {
        Optional<Recipe> existingRecipe = recipeService.getRecipeById(id, user);

        if (existingRecipe.isPresent()) {
            Recipe recipe = existingRecipe.get();
            recipe.setName(recipeDto.getName());
            recipe.setDescription(recipeDto.getDescription());
            recipe.setSteps(recipeDto.getSteps());

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

            recipe.setCookingTime(recipeService.calculateRecipeCookTime(recipe.getId()));
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe, user);
            return ResponseEntity.ok(buildRecipeModel(updatedRecipe));
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * ✅ Delete a recipe.
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
        EntityModel<Recipe> recipeModel = EntityModel.of(recipe,
                linkTo(methodOn(RecipeController.class).getPublicRecipeById(recipe.getId())).withSelfRel(),
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("publicRecipes"));

        recipeModel.add(
                linkTo(methodOn(RecipeController.class).updateRecipe(recipe.getId(), new RecipeDto(), null))
                        .withRel("update").withType("PUT"),
                linkTo(methodOn(RecipeController.class).deleteRecipe(recipe.getId(), null))
                        .withRel("delete").withType("DELETE")
        );

        if (recipe.getUser() != null) {
            recipeModel.add(linkTo(methodOn(UserController.class).getUserById(recipe.getUser().getId()))
                    .withRel("author"));
        }

        return recipeModel;
    }
}
