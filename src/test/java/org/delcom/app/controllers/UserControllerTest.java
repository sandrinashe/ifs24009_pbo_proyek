package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private UserController userController;

    private User mockUser;
    private UUID userId;
    private String rawPassword = "password123";
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword(encodedPassword);

        // Inject AuthContext ke field private/protected controller
        ReflectionTestUtils.setField(userController, "authContext", authContext);
    }

    // ==========================================
    // 1. REGISTER USER TESTS
    // ==========================================

    @Test
    void registerUser_Success() {
        User reqUser = new User();
        reqUser.setName("New User");
        reqUser.setEmail("new@example.com");
        reqUser.setPassword("pass123");

        when(userService.getUserByEmail(reqUser.getEmail())).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(mockUser);

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(reqUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertNotNull(response.getBody().getData().get("id"));
    }

    // --- Validation Splits (Untuk menghilangkan Kuning pada operator ||) ---

    @Test
    void registerUser_NameNull() {
        User req = new User();
        req.setName(null);
        ResponseEntity<?> response = userController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>) response.getBody()).getMessage().contains("Data nama tidak valid"));
    }

    @Test
    void registerUser_NameEmpty() {
        User req = new User();
        req.setName("");
        ResponseEntity<?> response = userController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>) response.getBody()).getMessage().contains("Data nama tidak valid"));
    }

    @Test
    void registerUser_EmailNull() {
        User req = new User();
        req.setName("Valid");
        req.setEmail(null);
        ResponseEntity<?> response = userController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>) response.getBody()).getMessage().contains("Data email tidak valid"));
    }

    @Test
    void registerUser_EmailEmpty() {
        User req = new User();
        req.setName("Valid");
        req.setEmail("");
        ResponseEntity<?> response = userController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>) response.getBody()).getMessage().contains("Data email tidak valid"));
    }

    @Test
    void registerUser_PasswordNull() {
        User req = new User();
        req.setName("Valid");
        req.setEmail("valid@mail.com");
        req.setPassword(null);
        ResponseEntity<?> response = userController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>) response.getBody()).getMessage().contains("Data password tidak valid"));
    }

    @Test
    void registerUser_PasswordEmpty() {
        User req = new User();
        req.setName("Valid");
        req.setEmail("valid@mail.com");
        req.setPassword("");
        ResponseEntity<?> response = userController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>) response.getBody()).getMessage().contains("Data password tidak valid"));
    }

    // --- Logic Branches ---

    @Test
    void registerUser_EmailAlreadyExists() {
        User reqUser = new User();
        reqUser.setName("User");
        reqUser.setEmail("exist@example.com");
        reqUser.setPassword("pass");

        when(userService.getUserByEmail(reqUser.getEmail())).thenReturn(new User()); // Return something not null

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(reqUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Pengguna sudah terdaftar"));
    }

    // ==========================================
    // 2. LOGIN USER TESTS
    // ==========================================

    @Test
    void loginUser_Success_NoExistingToken() {
        User loginReq = new User();
        loginReq.setEmail("test@example.com");
        loginReq.setPassword(rawPassword);

        when(userService.getUserByEmail(loginReq.getEmail())).thenReturn(mockUser);
        when(authTokenService.findUserToken(eq(userId), any())).thenReturn(null);
        
        AuthToken newToken = new AuthToken(userId, "new-token-123");
        when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(newToken);

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(loginReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login berhasil", response.getBody().getMessage());
    }

    @Test
    void loginUser_Success_WithExistingToken() {
        User loginReq = new User();
        loginReq.setEmail("test@example.com");
        loginReq.setPassword(rawPassword);

        when(userService.getUserByEmail(loginReq.getEmail())).thenReturn(mockUser);
        
        // Mock existing token
        AuthToken existingToken = new AuthToken(userId, "old-token");
        when(authTokenService.findUserToken(eq(userId), any())).thenReturn(existingToken);
        
        // Mock new token creation
        AuthToken newToken = new AuthToken(userId, "new-token");
        when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(newToken);

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(loginReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Verifikasi token lama dihapus
        verify(authTokenService).deleteAuthToken(userId);
    }

    // --- Validation Splits ---

    @Test
    void loginUser_EmailNull() {
        User req = new User();
        req.setEmail(null);
        ResponseEntity<?> response = userController.loginUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginUser_EmailEmpty() {
        User req = new User();
        req.setEmail("");
        ResponseEntity<?> response = userController.loginUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginUser_PasswordNull() {
        User req = new User();
        req.setEmail("valid@mail.com");
        req.setPassword(null);
        ResponseEntity<?> response = userController.loginUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginUser_PasswordEmpty() {
        User req = new User();
        req.setEmail("valid@mail.com");
        req.setPassword("");
        ResponseEntity<?> response = userController.loginUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- Logic Branches (Wrong Creds, etc) ---

    @Test
    void loginUser_UserNotFound() {
        User loginReq = new User();
        loginReq.setEmail("unknown@example.com");
        loginReq.setPassword("pass");

        when(userService.getUserByEmail(loginReq.getEmail())).thenReturn(null); // Return null

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(loginReq);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Email atau password salah"));
    }

    @Test
    void loginUser_WrongPassword() {
        User loginReq = new User();
        loginReq.setEmail("test@example.com");
        loginReq.setPassword("WRONG_PASS"); // Password beda

        when(userService.getUserByEmail(loginReq.getEmail())).thenReturn(mockUser);

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(loginReq);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Email atau password salah"));
    }

    @Test
    void loginUser_TokenCreationFailure() {
        // Simulasi error 500 jika gagal buat token
        User loginReq = new User();
        loginReq.setEmail("test@example.com");
        loginReq.setPassword(rawPassword);

        when(userService.getUserByEmail(loginReq.getEmail())).thenReturn(mockUser);
        when(authTokenService.findUserToken(any(), any())).thenReturn(null);
        when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(null); // Return Null

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(loginReq);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Gagal membuat token"));
    }

    // ==========================================
    // 3. GET USER INFO TESTS
    // ==========================================

    @Test
    void getUserInfo_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = userController.getUserInfo();
        // Berdasarkan screenshot, method ini mengembalikan 401 Unauthorized atau 403
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void getUserInfo_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);

        ResponseEntity<ApiResponse<Map<String, User>>> response = userController.getUserInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody().getData().get("user").getId());
    }

    // ==========================================
    // 4. UPDATE USER TESTS
    // ==========================================

    @Test
    void updateUser_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = userController.updateUser(new User());
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void updateUser_Success() {
        User reqUpdate = new User();
        reqUpdate.setName("Updated Name");
        reqUpdate.setEmail("update@mail.com");

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(userService.updateUser(eq(userId), anyString(), anyString())).thenReturn(mockUser);

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(reqUpdate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User berhasil diupdate", response.getBody().getMessage());
    }

    @Test
    void updateUser_NotFound() {
        // Simulasi user tidak ditemukan di service
        User reqUpdate = new User();
        reqUpdate.setName("Updated Name");
        reqUpdate.setEmail("update@mail.com");

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(userService.updateUser(any(), any(), any())).thenReturn(null); // Return null

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(reqUpdate);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- Validation Splits ---

    @Test
    void updateUser_NameNull() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User req = new User();
        req.setName(null);
        ResponseEntity<?> response = userController.updateUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUser_NameEmpty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User req = new User();
        req.setName("");
        ResponseEntity<?> response = userController.updateUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void updateUser_EmailNull() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User req = new User();
        req.setName("Valid");
        req.setEmail(null);
        ResponseEntity<?> response = userController.updateUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUser_EmailEmpty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User req = new User();
        req.setName("Valid");
        req.setEmail("");
        ResponseEntity<?> response = userController.updateUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==========================================
    // 5. UPDATE PASSWORD TESTS
    // ==========================================

    @Test
    void updateUserPassword_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = userController.updateUserPassword(Map.of());
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void updateUserPassword_Success() {
        Map<String, String> payload = Map.of(
            "password", rawPassword,
            "newPassword", "newPass123"
        );

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(userService.updatePassword(eq(userId), anyString())).thenReturn(mockUser);

        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password berhasil diupdate", response.getBody().getMessage());
    }

    @Test
    void updateUserPassword_WrongOldPassword() {
        Map<String, String> payload = Map.of(
            "password", "WRONG_PASSWORD",
            "newPassword", "newPass123"
        );

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        // Tidak perlu mock userService.updatePassword karena akan gagal di cek BCrypt Controller

        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Konfirmasi password tidak cocok") || response.getBody().getMessage().contains("salah"));
    }

    @Test
    void updateUserPassword_NotFound() {
        Map<String, String> payload = Map.of(
            "password", rawPassword,
            "newPassword", "newPass123"
        );

        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(userService.updatePassword(any(), anyString())).thenReturn(null); // Fail update

        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- Validation Splits (Complex OR logic) ---
    // if (oldPass == null || oldPass.isEmpty() || newPass == null || newPass.isEmpty())

    @Test
    void updateUserPassword_OldPassNull() {
        when(authContext.isAuthenticated()).thenReturn(true);
        // Map tidak boleh null value, jadi kita simulasi kondisi dimana key tidak ada -> get() return null
        Map<String, String> payload = new HashMap<>(); 
        payload.put("newPassword", "val");
        
        ResponseEntity<?> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((ApiResponse<?>)response.getBody()).getMessage().contains("wajib diisi"));
    }

    @Test
    void updateUserPassword_OldPassEmpty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        Map<String, String> payload = Map.of("password", "", "newPassword", "val");
        
        ResponseEntity<?> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserPassword_NewPassNull() {
        when(authContext.isAuthenticated()).thenReturn(true);
        Map<String, String> payload = new HashMap<>();
        payload.put("password", "val");
        
        ResponseEntity<?> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserPassword_NewPassEmpty() {
        when(authContext.isAuthenticated()).thenReturn(true);
        Map<String, String> payload = Map.of("password", "val", "newPassword", "");
        
        ResponseEntity<?> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}