package com.tus.group_project.dao;

import com.tus.group_project.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByRecipeId(Long recipeId);
}
