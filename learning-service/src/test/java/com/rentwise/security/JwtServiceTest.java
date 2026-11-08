package com.rentwise.security;

import com.rentwise.user.domain.UserAccount;
import com.rentwise.user.domain.UserRole;
import com.rentwise.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JwtServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef-test-secret";
    private static final String OTHER_SECRET = "abcdef0123456789abcdef0123456789-other-secret";

    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;

    @Test
    void generatedTokenParsesToSameIdentityAndHasOneHourLifetime() {
        UserAccount user = userRepository.save(new UserAccount("Atopos", "$2a$10$hash", UserRole.LEARNER, true));

        String token = jwtService.generateToken(user);
        CurrentUser currentUser = jwtService.parse(token);

        assertThat(currentUser.id()).isEqualTo(user.getId());
        assertThat(currentUser.username()).isEqualTo("Atopos");
        assertThat(currentUser.role()).isEqualTo(UserRole.LEARNER);

        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        long lifetime = claims.getExpiration().toInstant().getEpochSecond()
                - claims.getIssuedAt().toInstant().getEpochSecond();
        assertThat(lifetime).isEqualTo(3600);
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        UserAccount user = userRepository.save(new UserAccount("Atopos", "$2a$10$hash", UserRole.LEARNER, true));
        String token = jwtService.generateToken(user);
        JwtService other = new JwtService(OTHER_SECRET, 3600);

        assertThatThrownBy(() -> other.parse(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        UserAccount user = userRepository.save(new UserAccount("Atopos", "$2a$10$hash", UserRole.LEARNER, true));
        JwtService expiredIssuer = new JwtService(SECRET, -1);
        String token = expiredIssuer.generateToken(user);

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void malformedTokenIsRejected() {
        assertThatThrownBy(() -> jwtService.parse("not-a-jwt")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void tooShortSecretIsRejected() {
        assertThatThrownBy(() -> new JwtService("too-short", 3600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }
}
