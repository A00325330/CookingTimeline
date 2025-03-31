package com.tus.group_project.controller;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private IAuthService authService;
    private IJwtService jwtService;
    private UserRepository userRepository;
    private AuthController authController;

    private final String email = "test@example.com";
    private final String password = "secure123";
    private final String token = "jwt.token.here";

    private final UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            email, password, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @BeforeEach
    void setup() {
        authService = mock(IAuthService.class);
        jwtService = mock(IJwtService.class);
        userRepository = mock(UserRepository.class);
        authController = new AuthController(authService, jwtService, userRepository);
    }

    @Test
    void createJwt_shouldReturnTokenOnSuccess() throws InvalidCredentialsException {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);

        when(authService.authenticate(email, password)).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails, "ROLE_USER")).thenReturn(token);
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRoles(EnumSet.of(Role.USER));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<EntityModel<UserLoginResponse>> response = authController.createJwt(loginDto);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getContent().getToken());
    }

    @Test
    void createJwt_shouldReturn401OnInvalidCredentials() throws InvalidCredentialsException {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);

        when(authService.authenticate(email, password)).thenThrow(new InvalidCredentialsException("Invalid"));

        ResponseEntity<EntityModel<UserLoginResponse>> response = authController.createJwt(loginDto);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid email or password.", response.getBody().getContent().getToken());
    }

    @Test
    void registerUser_shouldReturnSuccessIfNewUser() {
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setEmail(email);
        regDto.setPassword(password);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(authService.encodePassword(password)).thenReturn("hashed_password");

        ResponseEntity<EntityModel<UserRegistrationResponse>> response = authController.registerUser(regDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered successfully!", response.getBody().getContent().getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldReturn400IfEmailTaken() {
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setEmail(email);
        regDto.setPassword(password);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        ResponseEntity<EntityModel<UserRegistrationResponse>> response = authController.registerUser(regDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email is already taken.", response.getBody().getContent().getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
