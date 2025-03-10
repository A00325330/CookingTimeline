package com.tus.group_project.service.impl;

import com.tus.group_project.dao.RecipeRepository;
import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.RecipeIngredient;
import com.tus.group_project.model.User;
import com.tus.group_project.model.Visibility;
import com.tus.group_project.service.IRecipeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeService implements IRecipeService {

    private final RecipeRepository recipeRepository;
    private Recipe temporaryRecipe; // ✅ Holds the temporary recipe for non-logged-in users

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    public Recipe createRecipe(Recipe recipe, User user) {
        recipe.setUser(user);
        return recipeRepository.save(recipe);
    }

    @Override
    public List<Recipe> getPublicRecipes() {
        return recipeRepository.findByVisibility(Visibility.PUBLIC);
    }

    @Override
    public List<Recipe> getUserRecipes(User user) {
        return recipeRepository.findByUser(user);
    }

    @Override
    public Optional<Recipe> getRecipeById(Long id, User user) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        if (recipe.isPresent()) {
            Recipe foundRecipe = recipe.get();
            if (foundRecipe.getVisibility() == Visibility.PUBLIC || (user != null && foundRecipe.getUser().equals(user))) {
                return Optional.of(foundRecipe);
            }
        }
        return Optional.empty();
    }

    @Override
    public Recipe updateRecipe(Long id, Recipe updatedRecipe, User user) {
        Optional<Recipe> existingRecipe = getRecipeById(id, user);

        if (existingRecipe.isPresent()) {
            Recipe recipe = existingRecipe.get();
            recipe.setName(updatedRecipe.getName());
            recipe.setIngredients(updatedRecipe.getIngredients());
            recipe.setSteps(updatedRecipe.getSteps());
            recipe.setCookingTime(updatedRecipe.getCookingTime());
            recipe.setVisibility(updatedRecipe.getVisibility());
            return recipeRepository.save(recipe);
        }
        throw new RuntimeException("Unauthorized or recipe not found");
    }

    @Override
    public void deleteRecipe(Long id, User user) {
        Optional<Recipe> recipe = getRecipeById(id, user);
        recipe.ifPresent(recipeRepository::delete);
    }

    @Override
    public int calculateRecipeCookTime(Long recipeId) {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        return recipe.map(value -> value.getIngredients().stream()
                .mapToInt(RecipeIngredient::getCookingTime)
                .max()
                .orElse(0)).orElse(0);
    }

    @Override
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findByIdAndVisibility(id, Visibility.PUBLIC);
    }

    // ✅ Create or retrieve a temporary recipe for non-logged-in users
    @Override
    public Recipe createOrGetTemporaryRecipe() {
        Optional<Recipe> existingTempRecipe = recipeRepository.findFirstByIsTemporaryTrue();

        if (existingTempRecipe.isPresent()) {
            return existingTempRecipe.get(); // ✅ Return the existing temporary recipe
        }

        Recipe tempRecipe = new Recipe();
        tempRecipe.setName("Temporary Recipe");
        tempRecipe.setVisibility(Visibility.PRIVATE);
        tempRecipe.setTemporary(true); // ✅ Ensure the recipe is marked as temporary

        return recipeRepository.save(tempRecipe);
    }
    @Override
    public Optional<Recipe> getTemporaryRecipeById(Long id) {
        return recipeRepository.findById(id).filter(Recipe::isTemporary);
    }


    // ✅ Add an ingredient to the temporary recipe
    @Override
    public Recipe addIngredientToTemporaryRecipe(Long tempRecipeId, RecipeIngredient ingredient) {
        // ✅ Fetch the temporary recipe from the database
        Optional<Recipe> tempRecipeOptional = recipeRepository.findById(tempRecipeId);
        
        if (tempRecipeOptional.isEmpty() || !tempRecipeOptional.get().isTemporary()) {
            return null; // ❌ If no temp recipe exists or it's not marked temporary, return null
        }

        Recipe tempRecipe = tempRecipeOptional.get();

        // ✅ Ensure ingredients list is initialized
        if (tempRecipe.getIngredients() == null) {
            tempRecipe.setIngredients(new ArrayList<>());
        }

        // ✅ Add ingredient and save
        ingredient.setRecipe(tempRecipe);
        tempRecipe.getIngredients().add(ingredient);
        return recipeRepository.save(tempRecipe);
    }

}
