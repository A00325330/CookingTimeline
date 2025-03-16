package com.tus.group_project.service;

import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.RecipeIngredient;
import com.tus.group_project.model.Tag;
import com.tus.group_project.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRecipeService {
    Recipe createRecipe(Recipe recipe, User user);
    Optional<Recipe> getRecipeById(Long id);
    List<Recipe> getPublicRecipes();
    List<Recipe> getUserRecipes(User user);
    Optional<Recipe> getRecipeById(Long id, User user);
    Recipe updateRecipe(Long id, Recipe updatedRecipe, User user);
    int calculateRecipeCookTime(Long recipeId);
    void deleteRecipe(Long id, User user);



    // ✅ New methods for handling tags
    List<Tag> getAllTags(); // Fetch all available tags
	List<Recipe> getRecipesByTag(String tagName, User user);
}
