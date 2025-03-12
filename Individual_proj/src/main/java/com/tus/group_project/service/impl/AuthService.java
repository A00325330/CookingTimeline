package com.tus.group_project.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.exception.InvalidCredentialsException;
import com.tus.group_project.exception.UserAlreadyExistsException;
import com.tus.group_project.model.Role;
import com.tus.group_project.model.User;
import com.tus.group_project.service.IAuthService;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService implements IAuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public UserDetails authenticate(String email, String password) throws InvalidCredentialsException {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new InvalidCredentialsException("Invalid password");
            }
            return userDetails;
        } catch (UsernameNotFoundException e) {
            throw new InvalidCredentialsException("Invalid email");
        }
    }

    @Override
    public void registerUser(UserRegistrationDto userRegistrationDto) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(userRegistrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        String encodedPassword = passwordEncoder.encode(userRegistrationDto.getPassword());

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER); // ✅ Assign default role without querying a database

        User newUser = new User();
        newUser.setEmail(userRegistrationDto.getEmail().trim().toLowerCase());
        newUser.setPassword(encodedPassword);
        newUser.setRoles(roles);

        userRepository.save(newUser);
    }
}
