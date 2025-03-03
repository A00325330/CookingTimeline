package com.tus.group_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // ✅ Store roles as an enum collection
    @Enumerated(EnumType.STRING) // ✅ Store role names as strings (e.g., "USER", "ADMIN")
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) // ✅ Join table
    @Column(name = "role") // ✅ Column inside `user_roles`
    private Set<Role> roles = new HashSet<>();

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN); // ✅ Proper role check using enum
    }
}
