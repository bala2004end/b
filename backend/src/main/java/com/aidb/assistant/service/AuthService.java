package com.aidb.assistant.service;

import com.aidb.assistant.dto.AuthRequest;
import com.aidb.assistant.dto.AuthResponse;
import com.aidb.assistant.dto.RegisterRequest;
import com.aidb.assistant.entity.User;
import com.aidb.assistant.repository.UserRepository;
import com.aidb.assistant.security.JwtTokenProvider;
import com.aidb.assistant.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login.
 * On login, the UserPrincipal from the Authentication context is reused
 * to avoid a redundant DB query for the same user record.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Reuse the UserPrincipal already loaded during authentication — no second DB query
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(principal.getUsername())
                .email(principal.getEmail())
                .role(principal.getAuthorities().iterator().next().getAuthority())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        // Authenticate immediately after registration so the token is returned
        return login(new AuthRequest(request.getUsername(), request.getPassword()));
    }
}
