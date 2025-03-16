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
                // ✅ Allow only index, login, and register publicly
                .requestMatchers("/", "/index.html", "/api/auth/login","/api/auth/register").permitAll()
                .requestMatchers("/css/**", "/js/**").permitAll()

                // 🔐 **Require login for public and user recipes**
                .requestMatchers(HttpMethod.GET, "/api/recipes/public", "/api/recipes/public/**").permitAll() // ✅ Fix
                .requestMatchers(HttpMethod.GET, "/api/recipes/mine").authenticated()

                // 🔐 **All other recipe endpoints require authentication**
                .requestMatchers(HttpMethod.POST, "/api/recipes").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/recipes/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/recipes/by-tag/**").authenticated()  // ✅ Public access for tag search

                // 🔐 **Only admins can access user management**
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")

                // 🔐 **Protect everything else**
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
