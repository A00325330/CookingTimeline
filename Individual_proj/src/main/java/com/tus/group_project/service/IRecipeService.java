package com.tus.group_project.service;

import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.Tag;
import com.tus.group_project.model.User;

import java.util.List;
import java.util.Optional;

public interface IRecipeService {

    Recipe createRecipe(Recipe recipe, User user);

    Optional<Recipe> getRecipeById(Long id);

    Optional<Recipe> getRecipeById(Long id, User user);

    List<Recipe> getPublicRecipes();

    List<Recipe> getUserRecipes(User user);

    Recipe updateRecipe(Long id, Recipe updatedRecipe, User user);

    void deleteRecipe(Long id, User user);

    int calculateRecipeCookTime(Long recipeId);

    List<Tag> getAllTags();

    List<Recipe> getRecipesByTag(String tagName, User user);
}
