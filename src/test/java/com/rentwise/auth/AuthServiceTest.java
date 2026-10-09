package com.rentwise.auth;

import com.rentwise.auth.dto.LoginRequest;
import com.rentwise.auth.dto.RegisterRequest;
import com.rentwise.auth.service.AuthService;
import com.rentwise.common.exception.DomainException;
import com.rentwise.user.domain.UserAccount;
import com.rentwise.user.domain.UserRole;
import com.rentwise.user.repository.UserRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired Validator validator;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registrationCreatesEnabledLearnerWithBcryptHash() {
        var response = authService.register(new RegisterRequest("Atopos", "12345678"));
        var saved = userRepository.findById(response.id()).orElseThrow();

        assertThat(response.username()).isEqualTo("Atopos");
        assertThat(response.role()).isEqualTo(UserRole.LEARNER);
        assertThat(saved.getRole()).isEqualTo(UserRole.LEARNER);
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getPasswordHash()).isNotEqualTo("12345678");
        assertThat(passwordEncoder.matches("12345678", saved.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateUsernameIsRejectedIgnoringCase() {
        authService.register(new RegisterRequest("Atopos", "12345678"));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("atopos", "abcdefgh")))
                .isInstanceOfSatisfying(DomainException.class, ex -> {
                    assertThat(ex.status()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getMessage()).isEqualTo("Username already exists");
                });
    }

    @Test
    void registrationRequestDoesNotExposeRoleField() {
        assertThat(Arrays.stream(RegisterRequest.class.getRecordComponents())
                .map(component -> component.getName()))
                .containsExactly("username", "password");
    }

    @Test
    void registrationValidationEnforcesUsernameAndPasswordBoundaries() {
        assertThat(validator.validate(new RegisterRequest("ab", "12345678"))).isNotEmpty();
        assertThat(validator.validate(new RegisterRequest("a".repeat(33), "12345678"))).isNotEmpty();
        assertThat(validator.validate(new RegisterRequest("valid", "1234567"))).isNotEmpty();
        assertThat(validator.validate(new RegisterRequest("valid", "a".repeat(65)))).isNotEmpty();
        assertThat(validator.validate(new RegisterRequest("valid", "12345678"))).isEmpty();
    }

    @Test
    void validCredentialsReturnBearerJwt() {
        authService.register(new RegisterRequest("Atopos", "12345678"));

        var response = authService.login(new LoginRequest("atopos", "12345678"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
    }

    @Test
    void wrongPasswordAndUnknownUserReturnSameUnauthorizedError() {
        authService.register(new RegisterRequest("Atopos", "12345678"));

        assertInvalidCredentials(() -> authService.login(new LoginRequest("Atopos", "wrongpass")));
        assertInvalidCredentials(() -> authService.login(new LoginRequest("Nobody", "12345678")));
    }

    @Test
    void disabledUserCannotLogin() {
        userRepository.save(new UserAccount(
                "Disabled",
                passwordEncoder.encode("12345678"),
                UserRole.LEARNER,
                false));

        assertInvalidCredentials(() -> authService.login(new LoginRequest("Disabled", "12345678")));
    }

    private void assertInvalidCredentials(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(DomainException.class, ex -> {
                    assertThat(ex.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getMessage()).isEqualTo("Invalid username or password");
                });
    }
}
