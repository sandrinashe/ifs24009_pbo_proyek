package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthContextTest {

    private AuthContext authContext;

    @BeforeEach
    void setUp() {
        // Kita tidak perlu @Autowired, cukup instansiasi manual
        // karena kita hanya mengetes logika getter/setter/isAuthenticated
        authContext = new AuthContext();
    }

    @Test
    void testInitialState() {
        // Secara default, authUser harus null dan tidak terautentikasi
        assertNull(authContext.getAuthUser());
        assertFalse(authContext.isAuthenticated());
    }

    @Test
    void testSetAuthUser_Success() {
        // Given
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setName("Test User");

        // When
        authContext.setAuthUser(user);

        // Then
        assertNotNull(authContext.getAuthUser());
        assertEquals("Test User", authContext.getAuthUser().getName());
        assertTrue(authContext.isAuthenticated());
    }

    @Test
    void testSetAuthUser_Null() {
        // Test case: Reset user menjadi null (logout logic)
        
        // Given
        authContext.setAuthUser(new User()); // Set user dulu
        assertTrue(authContext.isAuthenticated());

        // When
        authContext.setAuthUser(null); // Set jadi null

        // Then
        assertNull(authContext.getAuthUser());
        assertFalse(authContext.isAuthenticated());
    }
}