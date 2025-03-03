package com.tus.group_project.service.impl;

import com.tus.group_project.dao.RecipeRepository;
import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.User;
import com.tus.group_project.model.Visibility;
import com.tus.group_project.service.IRecipeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService implements IRecipeService {

    private final RecipeRepository recipeRepository;

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

        // ✅ Only return if recipe is PUBLIC or belongs to the user
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
            recipe.setCookingMethod(updatedRecipe.getCookingMethod());
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Optional<Recipe> getRecipeById(Long id) {
	    return recipeRepository.findByIdAndVisibility(id, Visibility.PUBLIC);
	}

}
