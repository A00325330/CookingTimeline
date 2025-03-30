package com.tus.group_project.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.UserLoginDto;
import com.tus.group_project.dto.UserLoginResponse;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.dto.UserRegistrationResponse;
import com.tus.group_project.exception.InvalidCredentialsException;
import com.tus.group_project.model.Role;
import com.tus.group_project.model.User;
import com.tus.group_project.service.IAuthService;
import com.tus.group_project.service.IJwtService;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.EnumSet;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final IJwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(IAuthService authService, IJwtService jwtService, UserRepository userRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * 🔑 Login a user and return a JWT token with HATEOAS links.
     */
    @PostMapping("/login")
    public ResponseEntity<EntityModel<UserLoginResponse>> createJwt(
            @Valid @RequestBody UserLoginDto userLoginDto
    ) {
        try {
            // 1) Authenticate user
            UserDetails userDetails = authService.authenticate(
                userLoginDto.getEmail().trim().toLowerCase(),
                userLoginDto.getPassword()
            );

            // 2) Determine role and generate JWT
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("USER");

            String jwt = jwtService.generateToken(userDetails, role);
            UserLoginResponse response = new UserLoginResponse(jwt);

            // 3) Build the HATEOAS response
            EntityModel<UserLoginResponse> entityModel = EntityModel.of(response);

            // Self link (login itself)
            entityModel.add(
                linkTo(methodOn(AuthController.class).createJwt(null))
                    .withSelfRel()
            );

            // Register link
            entityModel.add(
                linkTo(methodOn(AuthController.class).registerUser(new UserRegistrationDto()))
                    .withRel("register")
            );

            // Link to public recipes
            entityModel.add(
                linkTo(methodOn(RecipeController.class).getPublicRecipes())
                    .withRel("recipes")
            );

            // Fetch the actual User object
            Optional<User> user = userRepository.findByEmail(userLoginDto.getEmail());
            user.ifPresent(u -> {
                if (u.getRoles().contains(Role.ADMIN)) {
                    entityModel.add(
                        linkTo(methodOn(UserController.class).getAllUsers())
                            .withRel("users")
                    );
                } else {
                    // ✅ Only non-admin users see "myRecipes"
                    entityModel.add(
                        linkTo(methodOn(RecipeController.class).getMyRecipes())
                            .withRel("myRecipes")
                    );
                }

                // ✅ Allow ALL authenticated users to create a PRIVATE recipe
                entityModel.add(
                    linkTo(methodOn(RecipeController.class).createRecipe(null))
                        .withRel("createRecipe")
                );
            });

            return ResponseEntity.ok(entityModel);

        } catch (InvalidCredentialsException e) {
            // Return 401 with a basic error message (no extra links)
            return ResponseEntity.status(401)
                    .body(EntityModel.of(
                        new UserLoginResponse("Invalid email or password.")
                    ));
        }
    }



    /**
     * 🆕 Register a new user (USER role assigned by default) with HATEOAS links.
     */
    @PostMapping("/register")
    public ResponseEntity<EntityModel<UserRegistrationResponse>> registerUser(
            @Valid @RequestBody UserRegistrationDto userRegistrationDto
    ) {
        String email = userRegistrationDto.getEmail().trim().toLowerCase();

        // Check if email is already taken
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(400)
                    .body(EntityModel.of(
                        new UserRegistrationResponse("Email is already taken.")
                    ));
        }

        // ✅ Create a new user with the USER role
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(
            authService.encodePassword(userRegistrationDto.getPassword())
        );
        newUser.setRoles(EnumSet.of(Role.USER));  // Only USER role by default
        userRepository.save(newUser);

        UserRegistrationResponse response = new UserRegistrationResponse(
                "User registered successfully!"
        );

        // Build the HATEOAS entity model
        EntityModel<UserRegistrationResponse> entityModel = EntityModel.of(response);

        // Self link (passing null for simplicity)
        entityModel.add(
            linkTo(methodOn(AuthController.class).registerUser(null))
                .withSelfRel()
        );

        // Link to login
        entityModel.add(
            linkTo(methodOn(AuthController.class).createJwt(null))
                .withRel("login")
        );

        return ResponseEntity.ok(entityModel);
    }
}
