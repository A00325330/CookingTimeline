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
    public ResponseEntity<EntityModel<UserLoginResponse>> createJwt(@Valid @RequestBody UserLoginDto userLoginDto) {
        try {
            UserDetails userDetails = authService.authenticate(
                    userLoginDto.getEmail().trim().toLowerCase(),
                    userLoginDto.getPassword()
            );

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("USER");

            String jwt = jwtService.generateToken(userDetails, role);
            UserLoginResponse response = new UserLoginResponse(jwt);

            EntityModel<UserLoginResponse> entityModel = EntityModel.of(response,
                linkTo(methodOn(AuthController.class).createJwt(userLoginDto)).withSelfRel(),
                linkTo(methodOn(AuthController.class).registerUser(null)).withRel("register"),
                linkTo(methodOn(RecipeController.class).getPublicRecipes()).withRel("recipes")
            );

            // ✅ Add link to /api/users if user is ADMIN
            Optional<User> user = userRepository.findByEmail(userLoginDto.getEmail());
            user.ifPresent(value -> {
                if (value.getRoles().contains(Role.ADMIN)) {
                    entityModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
                }
            });

            return ResponseEntity.ok(entityModel);

        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(401).body(EntityModel.of(new UserLoginResponse("Invalid email or password.")));
        }
    }

    /**
     * 🆕 Register a new user (USER role assigned by default) with HATEOAS links.
     */
    @PostMapping("/register")
    public ResponseEntity<EntityModel<UserRegistrationResponse>> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        String email = userRegistrationDto.getEmail().trim().toLowerCase();
        
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(400).body(EntityModel.of(new UserRegistrationResponse("Email is already taken.")));
        }

        // ✅ Create a new user with the USER role
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(authService.encodePassword(userRegistrationDto.getPassword()));  // Uses service for password encoding
        newUser.setRoles(EnumSet.of(Role.USER));  // 🔥 Assigns USER role only

        userRepository.save(newUser);

        UserRegistrationResponse response = new UserRegistrationResponse("User registered successfully!");
        EntityModel<UserRegistrationResponse> entityModel = EntityModel.of(response,
            linkTo(methodOn(AuthController.class).registerUser(userRegistrationDto)).withSelfRel(),
            linkTo(methodOn(AuthController.class).createJwt(null)).withRel("login")
        );

        return ResponseEntity.ok(entityModel);
    }
}
