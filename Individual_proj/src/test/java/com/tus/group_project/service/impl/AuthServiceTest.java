package com.tus.group_project.service.impl;

import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.exception.InvalidCredentialsException;
import com.tus.group_project.exception.UserAlreadyExistsException;
import com.tus.group_project.model.Role;
import com.tus.group_project.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEncodePassword() {
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        String result = authService.encodePassword("plain");

        assertEquals("encoded", result, () ->
            "Expected: encoded, but got: " + result
        );
        verify(passwordEncoder).encode("plain");
    }

    @Test
    void testAuthenticate_Success() throws InvalidCredentialsException {
        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getPassword()).thenReturn("encodedPassword");

        when(userDetailsService.loadUserByUsername("email@test.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("raw", "encodedPassword")).thenReturn(true);

        UserDetails result = authService.authenticate("email@test.com", "raw");

        assertEquals(mockUser, result, () -> 
            "Expected authenticated user to match mockUser, but got different instance."
        );
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getPassword()).thenReturn("encodedPassword");

        when(userDetailsService.loadUserByUsername("email@test.com")).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
            authService.authenticate("email@test.com", "wrong")
        );

        assertEquals("Invalid password", ex.getMessage(), () ->
            "Expected: Invalid password, but got: " + ex.getMessage()
        );
    }

    @Test
    void testAuthenticate_InvalidEmail() {
        when(userDetailsService.loadUserByUsername("bad@test.com"))
            .thenThrow(new UsernameNotFoundException("Not found"));

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
            authService.authenticate("bad@test.com", "password")
        );

        assertEquals("Invalid email", ex.getMessage(), () ->
            "Expected: Invalid email, but got: " + ex.getMessage()
        );
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("Test@Email.com");
        dto.setPassword("secret");

        when(userRepository.existsByEmail("Test@Email.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        authService.registerUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals("test@email.com", saved.getEmail(), () ->
            "Expected saved email to be 'test@email.com', but got: " + saved.getEmail()
        );
        assertEquals("encoded", saved.getPassword(), () ->
            "Expected password: encoded, but got: " + saved.getPassword()
        );
        assertTrue(saved.getRoles().contains(Role.USER), () ->
            "Expected roles to contain USER, but got: " + saved.getRoles()
        );
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("duplicate@test.com");
        dto.setPassword("anything");

        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () ->
            authService.registerUser(dto)
        );

        assertEquals("Email is already taken.", ex.getMessage(), () ->
            "Expected: Email is already taken., but got: " + ex.getMessage()
        );

        verify(userRepository, never()).save(any());
    }
}
