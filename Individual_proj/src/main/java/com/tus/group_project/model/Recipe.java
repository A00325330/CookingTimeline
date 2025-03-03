package com.tus.group_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends RepresentationModel<Recipe> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    // ✅ Replace simple String list with proper RecipeIngredient list
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<RecipeIngredient> ingredients;

    @ElementCollection
    private List<String> steps;

    @Enumerated(EnumType.STRING)
    private CookingMethod cookingMethod;

    private int cookingTime;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE; // Default to PRIVATE

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime startTime;
   
    private LocalDateTime finishTime;
}
