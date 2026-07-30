package com.aidb.assistant.service;

import com.aidb.assistant.dto.AuthRequest;
import com.aidb.assistant.dto.AuthResponse;
import com.aidb.assistant.dto.RegisterRequest;
import com.aidb.assistant.entity.User;
import com.aidb.assistant.repository.UserRepository;
import com.aidb.assistant.security.JwtTokenProvider;
import com.aidb.assistant.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Uses interface-based mocks only to avoid Java 25 ByteBuddy instrumentation issues.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // These are all interfaces or abstract classes — safely mockable on Java 25
    @Mock private UserRepository userRepository;       // JpaRepository interface
    @Mock private PasswordEncoder passwordEncoder;     // PasswordEncoder interface
    @Mock private AuthenticationManager authenticationManager; // interface

    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    private User testUser;
    private UserPrincipal testPrincipal;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        // A real base64-encoded secret key that's at least 32 bytes (256 bits) for HMAC-SHA256
        String testSecret = "Y2hhbmdlbWVjaGFuZ2VtZWNoYW5nZW1lY2hhbmdlbWVjaGFuZ2VtZWNoYW5nZW1l";
        tokenProvider = new JwtTokenProvider(testSecret, 86400000L);
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, tokenProvider);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("$2a$12$encodedpassword")
                .role("ROLE_USER")
                .build();

        testPrincipal = UserPrincipal.create(testUser);
        mockAuthentication = new UsernamePasswordAuthenticationToken(
                testPrincipal, null, testPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("Successful login returns AuthResponse — no second DB query for user")
    void login_success_noExtraDbQuery() {
        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);

        AuthResponse response = authService.login(new AuthRequest("testuser", "password"));

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");

        // CRITICAL: Verify no second DB round-trip happens — the optimization test
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Login with bad credentials propagates BadCredentialsException")
    void login_badCredentials_throws() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new AuthRequest("testuser", "wrongpassword")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Registration with duplicate username rejected before DB save")
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() ->
            authService.register(new RegisterRequest("testuser", "new@example.com", "password")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username is already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration with duplicate email rejected before DB save")
    void register_duplicateEmail_throws() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
            authService.register(new RegisterRequest("newuser", "test@example.com", "password")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any());
    }
}
