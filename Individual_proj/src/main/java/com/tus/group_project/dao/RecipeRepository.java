package com.tus.group_project.dao;

import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.User;
import com.tus.group_project.model.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // ✅ Get all public recipes
    List<Recipe> findByVisibility(Visibility visibility);

    // ✅ Get a public recipe by ID (guest-friendly)
    Optional<Recipe> findByIdAndVisibility(Long id, Visibility visibility);

    // ✅ Add this method to find a temporary recipe
    Optional<Recipe> findFirstByIsTemporaryTrue();
    
    // ✅ Get a private recipe by ID (only if owned by user)
    Optional<Recipe> findByIdAndUser(Long id, User user);

    // ✅ Find all recipes for a specific user (includes private)
    List<Recipe> findByUser(User user);

    // ✅ Find public or owned recipes
    List<Recipe> findByVisibilityOrUser(Visibility visibility, User user);
}
