package com.tus.group_project.service;

import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.User;

import java.util.List;
import java.util.Optional;

public interface IRecipeService {
    Recipe createRecipe(Recipe recipe, User user);
    Optional<Recipe> getRecipeById(Long id); // ✅ Public recipe access

    List<Recipe> getPublicRecipes(); // ✅ Only public recipes
    List<Recipe> getUserRecipes(User user); // ✅ Only user-owned recipes
    Optional<Recipe> getRecipeById(Long id, User user); // ✅ Updated to check visibility
    Recipe updateRecipe(Long id, Recipe updatedRecipe, User user);
    int calculateRecipeCookTime(Long recipeId); 
    void deleteRecipe(Long id, User user);
}
