package com.tus.group_project.service;

import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;

public interface IJwtService {

   /**
    * Generates a JWT Token for a User with a specific role.
    * @param userDetails The user details (username, password, etc.)
    * @param role The assigned role (e.g., "USER", "ADMIN")
    * @return The generated JWT token
    */
   String generateToken(UserDetails userDetails, String role);

   /**
    * Checks if the token is still valid and not expired.
    * @param token The JWT token to validate
    * @param userDetails The user details to match against
    * @return True if the token is valid, false otherwise
    */
   boolean isTokenValid(String token, UserDetails userDetails);

   /**
    * Extracts all claims (payload data) from a JWT token.
    * @param token The JWT token
    * @return The claims contained within the token
    */
   Claims extractAllClaims(String token);

   /**
    * Extracts the email (subject) from the JWT token.
    * @param token The JWT token
    * @return The extracted email (username)
    */
   String extractEmail(String token);

   /**
    * Extracts the role assigned to the user from the JWT token.
    * @param token The JWT token
    * @return The extracted role (e.g., "USER", "ADMIN")
    */
   String extractRole(String token);
}
