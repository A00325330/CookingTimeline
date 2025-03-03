package com.tus.group_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tus.group_project.filter.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ✅ Public endpoints
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/", "/index.html","/login.html","/admin.html", "/recipes.html", "/css/**", "/js/**").permitAll()
                
                // 🔐 Recipes: Allow logged-in users to view recipes
                .requestMatchers(HttpMethod.GET, "/api/recipes/public","/api/recipes/public/**").permitAll()  // ✅ Allow public access
                .requestMatchers(HttpMethod.GET, "/api/recipes/**").authenticated()

                
                // 🔐 Only allow owners/admins to edit/delete their recipes
                .requestMatchers(HttpMethod.PUT, "/api/recipes/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").authenticated()
                
                // 🔐 ✅ Allow ADMIN to access user-related endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN") 
                
                // 🔐 Protect all other endpoints
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }}
