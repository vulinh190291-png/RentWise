package com.rentwise.user;

import com.rentwise.user.domain.UserAccount;
import com.rentwise.user.domain.UserRole;
import com.rentwise.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    void findsUserIgnoringCaseAndPersistsIdentityFields() {
        var saved = userRepository.saveAndFlush(
                new UserAccount("Atopos", "$2a$10$placeholderHashForRepositoryTest", UserRole.LEARNER, true));

        var found = userRepository.findByUsernameIgnoreCase("atopos");

        assertThat(found).isPresent();
        assertThat(found.orElseThrow().getId()).isEqualTo(saved.getId());
        assertThat(found.orElseThrow().getUsername()).isEqualTo("Atopos");
        assertThat(found.orElseThrow().getRole()).isEqualTo(UserRole.LEARNER);
        assertThat(found.orElseThrow().isEnabled()).isTrue();
        assertThat(found.orElseThrow().getCreatedAt()).isNotNull();
        assertThat(userRepository.existsByUsernameIgnoreCase("ATOPOS")).isTrue();
    }
}
