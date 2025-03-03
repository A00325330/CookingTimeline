package com.tus.group_project.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.UserDto;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.dto.UserRegistrationResponse;
import com.tus.group_project.exception.UserAlreadyExistsException;
import com.tus.group_project.mapper.UserMapper;
import com.tus.group_project.model.Role;
import com.tus.group_project.model.User;
import com.tus.group_project.service.IUserService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.EnumSet;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final IUserService userService;

    private static final String USER_NOT_FOUND_ERROR = "User with this email does not exist.";
    private static final String USER_UPDATED_MESSAGE = "User successfully updated.";
    private static final String USER_DELETED_MESSAGE = "User successfully deleted.";

    public UserController(UserRepository userRepo, PasswordEncoder passwordEncoder, IUserService userService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * ✅ Get All Users (ADMIN ONLY) with HATEOAS support.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Fixed role check
    public CollectionModel<EntityModel<UserDto>> getAllUsers() {
        List<EntityModel<UserDto>> users = StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .map(user -> {
                    UserDto userDto = new UserDto();
                    UserMapper.toUserDto(user, userDto); // Convert user to DTO

                    return EntityModel.of(
                        userDto,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserById(user.getId())).withSelfRel()
                    );
                })
                .toList();

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()).withSelfRel();
        return CollectionModel.of(users, selfLink);
    }

    /**
     * ✅ Create a new User (ADMIN ONLY)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Fixed role check
    public ResponseEntity<EntityModel<UserRegistrationResponse>> createUser(@Valid @RequestBody UserRegistrationDto userRegDto) {
        String email = userRegDto.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userRegDto.getPassword()));
        user.setRoles(EnumSet.of(Role.USER));

        userRepo.save(user);

        EntityModel<UserRegistrationResponse> response = EntityModel.of(
                new UserRegistrationResponse("User successfully registered."),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUser(email)).withRel("user"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()).withRel("all_users")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ✅ Get User by Email (ADMIN ONLY)
     */
    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Fixed role check
    @Transactional
    public ResponseEntity<EntityModel<UserDto>> getUser(@Valid @PathVariable String email) {
        email = email.trim().toLowerCase();
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        UserDto userDto = new UserDto();
        UserMapper.toUserDto(userOptional.get(), userDto);

        EntityModel<UserDto> response = EntityModel.of(
                userDto,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUser(email)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()).withRel("all_users")
        );

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Get User by ID (ADMIN ONLY)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Fixed role check (optional: remove if public)
    public ResponseEntity<EntityModel<User>> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        EntityModel<User> response = EntityModel.of(
                user.get(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserById(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()).withRel("all_users")
        );

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Update User (ADMIN ONLY)
     */
    @PutMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Fixed role check
    @Transactional
    public ResponseEntity<EntityModel<String>> updateUser(@PathVariable String email, @Valid @RequestBody UserRegistrationDto userRegDto) {
        email = email.trim().toLowerCase();
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        User user = userOptional.get();
        user.setEmail(userRegDto.getEmail().trim().toLowerCase());
        if (userRegDto.getPassword() != null && !userRegDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRegDto.getPassword()));
        }
        userRepo.save(user);

        EntityModel<String> response = EntityModel.of(
                USER_UPDATED_MESSAGE,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUser(email)).withRel("user"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()).withRel("all_users")
        );

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Delete User (ADMIN ONLY)
     */
    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")  // ✅ Fixed role check
    @Transactional
    public ResponseEntity<EntityModel<String>> deleteUser(@Valid @PathVariable String email) {
        email = email.trim().toLowerCase();
        if (!userRepo.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        userRepo.deleteByEmail(email);

        EntityModel<String> response = EntityModel.of(
                USER_DELETED_MESSAGE,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()).withRel("all_users")
        );

        return ResponseEntity.ok(response);
    }
}
