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

                // ✅ Public auth endpoints, static files
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/", "/index.html", "/login.html", "/admin.html", "/recipes.html",
                                 "/css/**", "/js/**").permitAll()

                // ✅ Allow public recipes (GET)
                .requestMatchers(HttpMethod.GET, "/api/recipes/public", "/api/recipes/public/**").permitAll()

                // ✅ Allow anyone to create & modify temporary recipes
                .requestMatchers(HttpMethod.POST, "/api/recipes/temp", "/api/recipes/temp/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/recipes/temp/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/recipes/temp/{id}/**").permitAll()  // ✅ Allow DELETE temp recipes


                // 🔐 All other recipe endpoints require auth
                .requestMatchers(HttpMethod.GET, "/api/recipes/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/recipes/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/recipes/**").authenticated()

                // 🔐 Only admin can view user endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")

                // 🔐 Protect everything else
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
