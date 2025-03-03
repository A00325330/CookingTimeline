package com.tus.group_project.service;

import org.springframework.security.core.userdetails.UserDetails;
import com.tus.group_project.dto.UserRegistrationDto;
import com.tus.group_project.exception.InvalidCredentialsException;
import com.tus.group_project.exception.UserAlreadyExistsException;

public interface IAuthService {

    /**
     * Authenticate a user and return their details.
     * @param email The user's email
     * @param password The user's password
     * @return UserDetails if authentication is successful
     * @throws InvalidCredentialsException if credentials are incorrect
     */
    UserDetails authenticate(String email, String password) throws InvalidCredentialsException;

    /**
     * Register a new user in the system.
     * @param userRegistrationDto The registration details (email, password, role)
     * @throws UserAlreadyExistsException if the email is already taken
     */
    void registerUser(UserRegistrationDto userRegistrationDto) throws UserAlreadyExistsException;
}
