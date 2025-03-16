package com.tus.group_project.service.impl;

import com.tus.group_project.dao.RecipeRepository;
import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.RecipeIngredient;
import com.tus.group_project.model.Tag;
import com.tus.group_project.model.User;
import com.tus.group_project.model.Visibility;
import com.tus.group_project.service.IRecipeService;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService implements IRecipeService {

    private final RecipeRepository recipeRepository;
    private final TagRepository tagRepository; // ✅ Inject TagRepository


    public RecipeService(RecipeRepository recipeRepository, TagRepository tagRepository) {
        this.recipeRepository = recipeRepository;
        this.tagRepository = tagRepository;
    }


    @Override
    public Recipe createRecipe(Recipe recipe, User user) {
        recipe.setUser(user);

        // ✅ Ensure `tags` is not null before iteration
        if (recipe.getTags() == null) {
            recipe.setTags(new ArrayList<>()); 
        }

        List<Tag> updatedTags = new ArrayList<>();
        for (Tag tag : recipe.getTags()) {
            Tag existingTag = tagRepository.findByName(tag.getName()).orElse(null);
            if (existingTag == null) {
                existingTag = tagRepository.save(tag); // Create new tag if it doesn't exist
            }
            updatedTags.add(existingTag);
        }
        recipe.setTags(updatedTags);

        return recipeRepository.save(recipe);
    }



    @Override
    public List<Recipe> getPublicRecipes() {
        return recipeRepository.findByVisibility(Visibility.PUBLIC);
    }

    @Override
    public List<Recipe> getUserRecipes(User user) {
        List<Recipe> recipes = recipeRepository.findByUser(user);

        for (Recipe recipe : recipes) {
            Hibernate.initialize(recipe.getTags()); // ✅ Ensure Hibernate fetches tags before serialization
        }

        return recipes;
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
        return recipeRepository.findById(id);
    }

    // ✅ Implement getRecipesByTag method
    @Override
    public List<Recipe> getRecipesByTag(String tagName, User user) {
        Optional<Tag> tagOpt = tagRepository.findByName(tagName);

        // ✅ Ensure tag exists, otherwise return an empty list
        if (tagOpt.isEmpty()) {
            return List.of();
        }

        Tag tag = tagOpt.get();
        
        // ✅ Convert PersistentSet to a List before processing
        List<Recipe> recipeList = new ArrayList<>(tag.getRecipes());

        return recipeList.stream()
                .filter(recipe -> recipe.getVisibility() == Visibility.PUBLIC ||
                        (user != null && recipe.getUser().equals(user)))
                .toList();
    }




    @Override
    public List<Tag> getAllTags() {
        return new ArrayList<>(tagRepository.findAll());
    } 
}
