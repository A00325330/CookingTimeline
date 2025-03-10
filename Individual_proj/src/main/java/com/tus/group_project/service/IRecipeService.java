package com.tus.group_project.service;

import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.RecipeIngredient;
import com.tus.group_project.model.User;

import java.util.List;
import java.util.Optional;

public interface IRecipeService {
    Recipe createRecipe(Recipe recipe, User user);
    Optional<Recipe> getRecipeById(Long id);
    List<Recipe> getPublicRecipes();
    List<Recipe> getUserRecipes(User user);
    Optional<Recipe> getRecipeById(Long id, User user);
    Recipe updateRecipe(Long id, Recipe updatedRecipe, User user);
    int calculateRecipeCookTime(Long recipeId);
    void deleteRecipe(Long id, User user);

    // ✅ New methods for handling a temporary recipe
    Recipe createOrGetTemporaryRecipe();
    Recipe addIngredientToTemporaryRecipe(Long tempRecipeId, RecipeIngredient ingredient);
	Optional<Recipe> getTemporaryRecipeById(Long id);
}
