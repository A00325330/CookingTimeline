package com.tus.group_project.controller;

import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.UserDto;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.dto.UserRegistrationResponse;
import com.tus.group_project.model.Role;
import com.tus.group_project.model.User;
import com.tus.group_project.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private IUserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = mock(IUserService.class);
        userController = new UserController(userRepository, passwordEncoder, userService);
    }

    @Test
    void getAllUsers_shouldReturnCollection() {
        User user = new User(1L, "admin@test.com", "password", Set.of(Role.ADMIN));
        when(userRepository.findAll()).thenReturn(List.of(user));

        CollectionModel<EntityModel<UserDto>> response = userController.getAllUsers();
        assertNotNull(response);
        assertTrue(response.getLinks().hasLink("self"));
    }

    @Test
    void createUser_shouldReturnCreatedResponse() {
    	UserRegistrationDto dto = new UserRegistrationDto();
    	dto.setEmail("new@test.com");
    	dto.setPassword("pass");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        ResponseEntity<EntityModel<UserRegistrationResponse>> response = userController.createUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getContent());
        assertEquals("User successfully registered.", response.getBody().getContent().getMessage());
    }

    @Test
    void getUser_shouldReturnUser() {
        String email = "user@test.com";
        User user = new User(1L, email, "pass", Set.of(Role.USER));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<EntityModel<UserDto>> response = userController.getUser(email);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getUserById_shouldReturnUser() {
        Long userId = 5L;
        User user = new User(userId, "iduser@test.com", "pass", Set.of(Role.USER));
        when(userService.getUserById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<EntityModel<User>> response = userController.getUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody().getContent().getId());
    }

    @Test
    void updateUser_shouldUpdateAndReturnOk() {
        String email = "update@test.com";
        User user = new User(10L, email, "oldpass", Set.of(Role.USER));
        UserRegistrationDto updateDto = new UserRegistrationDto();
        updateDto.setEmail("updated@test.com");
        updateDto.setPassword("newpass");


        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("hashedNewPass");

        ResponseEntity<EntityModel<String>> response = userController.updateUser(email, updateDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User successfully updated.", response.getBody().getContent());
    }

    @Test
    void deleteUser_shouldReturnOk() {
        String email = "delete@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        ResponseEntity<EntityModel<String>> response = userController.deleteUser(email);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User successfully deleted.", response.getBody().getContent());
    }
}
