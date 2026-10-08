package com.rentwise.user.repository;

import com.rentwise.user.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
}
