package com.tus.group_project.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.Tag;
import com.tus.group_project.model.Visibility;
import com.tus.group_project.model.RecipeIngredient;
import com.tus.group_project.model.User;

@Data
public class RecipeDto {
    private String name;
    private String description;
    private List<String> steps;
    private int cookingTime;
    private Visibility visibility;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private List<String> tags;
    private List<IngredientDto> ingredients;

    public static RecipeDto fromEntity(Recipe recipe) {
        RecipeDto dto = new RecipeDto();
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setSteps(recipe.getSteps());
        dto.setCookingTime(recipe.getCookingTime());
        dto.setVisibility(recipe.getVisibility());
        dto.setStartTime(recipe.getStartTime());
        dto.setFinishTime(recipe.getFinishTime());

        dto.setTags(new ArrayList<>(recipe.getTags()).stream().map(Tag::getName).toList());

        dto.setIngredients(new ArrayList<>(recipe.getIngredients()).stream()
            .map(IngredientDto::fromEntity)
            .toList());

        return dto;
    }

    public Recipe toEntity(User user, TagRepository tagRepository) {
        Recipe recipe = new Recipe();
        recipe.setName(this.name);
        recipe.setDescription(this.description);
        recipe.setSteps(this.steps);
        recipe.setVisibility(this.visibility);
        recipe.setStartTime(this.startTime);
        recipe.setFinishTime(this.finishTime);
        recipe.setUser(user);

        List<Tag> tagEntities = (tags != null) ? tags.stream()
            .map(tagName -> tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                }))
            .collect(Collectors.toList()) : new ArrayList<>();

        recipe.setTags(tagEntities);

        List<RecipeIngredient> ingredientEntities = (ingredients != null) ? ingredients.stream()
            .map(ingredientDto -> {
                RecipeIngredient ingredient = new RecipeIngredient();
                ingredient.setRecipe(recipe);
                ingredient.setName(ingredientDto.getName());
                ingredient.setCookingTime(ingredientDto.getCookingTime());
                ingredient.setCookingMethod(ingredientDto.getCookingMethod());
                return ingredient;
            })
            .collect(Collectors.toList()) : new ArrayList<>();

        recipe.setIngredients(ingredientEntities);

        recipe.setCookingTime(
            ingredientEntities.stream()
                .mapToInt(RecipeIngredient::getCookingTime)
                .max()
                .orElse(0)
        );

        return recipe;
    }

    public void updateEntity(Recipe recipe, TagRepository tagRepository) {
        recipe.setName(this.name);
        recipe.setDescription(this.description);
        recipe.setSteps(this.steps);
        recipe.setVisibility(this.visibility);
        recipe.setStartTime(this.startTime);
        recipe.setFinishTime(this.finishTime);

        List<Tag> tagEntities = (tags != null) ? tags.stream()
            .map(tagName -> tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                }))
            .collect(Collectors.toList()) : new ArrayList<>();

        recipe.setTags(tagEntities);

        recipe.getIngredients().clear();

        if (ingredients != null) {
            List<RecipeIngredient> ingredientEntities = ingredients.stream()
                .map(ingredientDto -> {
                    RecipeIngredient ingredient = new RecipeIngredient();
                    ingredient.setRecipe(recipe);
                    ingredient.setName(ingredientDto.getName());
                    ingredient.setCookingTime(ingredientDto.getCookingTime());
                    ingredient.setCookingMethod(ingredientDto.getCookingMethod());
                    return ingredient;
                })
                .collect(Collectors.toList());

            recipe.getIngredients().addAll(ingredientEntities);

            recipe.setCookingTime(
                ingredientEntities.stream()
                    .mapToInt(RecipeIngredient::getCookingTime)
                    .max()
                    .orElse(0)
            );
        }
    }

    @Data
    public static class IngredientDto {
        private String name;
        private int cookingTime;
        private String cookingMethod;

        public static IngredientDto fromEntity(RecipeIngredient ingredient) {
            IngredientDto dto = new IngredientDto();
            dto.setName(ingredient.getName());
            dto.setCookingTime(ingredient.getCookingTime());
            dto.setCookingMethod(ingredient.getCookingMethod());
            return dto;
        }
    }
}
