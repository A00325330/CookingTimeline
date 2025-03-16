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
    @PreAuthorize("hasRole('ADMIN')")
    public CollectionModel<EntityModel<UserDto>> getAllUsers() {
        List<EntityModel<UserDto>> users = StreamSupport.stream(userRepo.findAll().spliterator(), false)
                .map(user -> {
                    UserDto userDto = new UserDto();
                    UserMapper.toUserDto(user, userDto); // Convert user to DTO

                    return EntityModel.of(
                            userDto,
                            // Self link -> GET user by ID
                            WebMvcLinkBuilder.linkTo(
                                    WebMvcLinkBuilder.methodOn(UserController.class)
                                            .getUserById(user.getId())
                            ).withSelfRel()
                    );
                })
                .toList();

        // Collection-level self link
        Link selfLink = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()
        ).withSelfRel();

        // Optional: Add a link to "createUser" since it's an admin-only method
        Link createUserLink = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(UserController.class).createUser(null)
        ).withRel("create_user");

        return CollectionModel.of(users, selfLink, createUserLink);
    }

    /**
     * ✅ Create a new User (ADMIN ONLY)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<UserRegistrationResponse>> createUser(
            @Valid @RequestBody UserRegistrationDto userRegDto
    ) {
        String email = userRegDto.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        // Create user with default ROLE_USER
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userRegDto.getPassword()));
        user.setRoles(EnumSet.of(Role.USER));
        userRepo.save(user);

        // Build response entity model
        EntityModel<UserRegistrationResponse> responseModel = EntityModel.of(
                new UserRegistrationResponse("User successfully registered.")
        );

        // Link to newly created user (by email)
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getUser(email)
                ).withRel("user")
        );

        // Link back to all users
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()
                ).withRel("all_users")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseModel);
    }

    /**
     * ✅ Get User by Email (ADMIN ONLY)
     */
    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<EntityModel<UserDto>> getUser(@Valid @PathVariable String email) {
        email = email.trim().toLowerCase();
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Convert to DTO
        UserDto userDto = new UserDto();
        UserMapper.toUserDto(userOptional.get(), userDto);

        EntityModel<UserDto> responseModel = EntityModel.of(userDto);

        // Self link: GET user by Email
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getUser(email)
                ).withSelfRel()
        );

        // Link to "all_users"
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()
                ).withRel("all_users")
        );

        // Because the endpoint is admin-only, we can also include links to update/delete
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).updateUser(email, null)
                ).withRel("update_user")
        );
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).deleteUser(email)
                ).withRel("delete_user")
        );

        return ResponseEntity.ok(responseModel);
    }

    /**
     * ✅ Get User by ID (ADMIN ONLY)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<User>> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User foundUser = userOpt.get();
        EntityModel<User> responseModel = EntityModel.of(foundUser);

        // Self link
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getUserById(id)
                ).withSelfRel()
        );

        // Link to all users
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()
                ).withRel("all_users")
        );

        // Also link to update/delete (using foundUser's email)
        String foundEmail = foundUser.getEmail().trim().toLowerCase();
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).updateUser(foundEmail, null)
                ).withRel("update_user")
        );
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).deleteUser(foundEmail)
                ).withRel("delete_user")
        );

        return ResponseEntity.ok(responseModel);
    }

    /**
     * ✅ Update User (ADMIN ONLY)
     */
    @PutMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<EntityModel<String>> updateUser(
            @PathVariable String email,
            @Valid @RequestBody UserRegistrationDto userRegDto
    ) {
        email = email.trim().toLowerCase();
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();
        user.setEmail(userRegDto.getEmail().trim().toLowerCase());
        if (userRegDto.getPassword() != null && !userRegDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRegDto.getPassword()));
        }
        userRepo.save(user);

        EntityModel<String> responseModel = EntityModel.of(USER_UPDATED_MESSAGE);

        // Link to updated user
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getUser(user.getEmail())
                ).withRel("user")
        );

        // Link to all users
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()
                ).withRel("all_users")
        );

        return ResponseEntity.ok(responseModel);
    }

    /**
     * ✅ Delete User (ADMIN ONLY)
     */
    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<EntityModel<String>> deleteUser(@Valid @PathVariable String email) {
        email = email.trim().toLowerCase();
        if (!userRepo.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        userRepo.deleteByEmail(email);

        EntityModel<String> responseModel = EntityModel.of(USER_DELETED_MESSAGE);

        // Link back to all users
        responseModel.add(
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(UserController.class).getAllUsers()
                ).withRel("all_users")
        );

        return ResponseEntity.ok(responseModel);
    }
}
