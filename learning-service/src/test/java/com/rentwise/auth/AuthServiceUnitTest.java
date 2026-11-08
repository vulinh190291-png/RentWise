package com.rentwise.auth;

import com.rentwise.auth.dto.RegisterRequest;
import com.rentwise.auth.service.AuthService;
import com.rentwise.common.exception.DomainException;
import com.rentwise.security.JwtService;
import com.rentwise.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceUnitTest {

    @Test
    void databaseUniqueConstraintRaceStillReturnsConflict() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);

        when(userRepository.existsByUsernameIgnoreCase("Atopos")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("$2a$10$encoded");
        when(userRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate username"));

        AuthService authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Atopos", "12345678")))
                .isInstanceOfSatisfying(DomainException.class, ex -> {
                    assertThat(ex.status()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getMessage()).isEqualTo("Username already exists");
                });
    }
}
