package com.tus.group_project.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNotNull(user.getRoles(), "Expected roles to be initialized");
        assertTrue(user.getRoles().isEmpty(), "Expected roles to be empty by default");
    }

    @Test
    void testAllArgsConstructor() {
        Set<Role> roles = Set.of(Role.USER);
        User user = new User(1L, "test@example.com", "securePass", roles);

        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("securePass", user.getPassword());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(Role.USER));
    }

    @Test
    void testSetters() {
        User user = new User();
        user.setId(2L);
        user.setEmail("admin@site.com");
        user.setPassword("adminPass");
        user.setRoles(Set.of(Role.ADMIN, Role.USER));

        assertEquals(2L, user.getId());
        assertEquals("admin@site.com", user.getEmail());
        assertEquals("adminPass", user.getPassword());
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void testIsAdminTrue() {
        User user = new User();
        user.setRoles(Set.of(Role.ADMIN));

        assertTrue(user.isAdmin(), "Expected isAdmin() to return true for ADMIN role");
    }

    @Test
    void testIsAdminFalse() {
        User user = new User();
        user.setRoles(Set.of(Role.USER));

        assertFalse(user.isAdmin(), "Expected isAdmin() to return false for non-ADMIN roles");
    }

    @Test
    void testIsAdminEmptyRoles() {
        User user = new User();
        assertFalse(user.isAdmin(), "Expected isAdmin() to return false when roles are empty");
    }
}
