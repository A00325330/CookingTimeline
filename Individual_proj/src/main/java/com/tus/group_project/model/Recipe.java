package com.tus.group_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    private boolean isTemporary = false; // Indicates if the recipe is temporary

    // ✅ Many-to-Many relationship with Tags
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "recipe_tags",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();  // ✅ Ensure this is initialized
}
