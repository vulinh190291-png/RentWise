package com.rentwise.user.service;

import com.rentwise.user.domain.UserAccount;
import com.rentwise.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserDemoDataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;

    public UserDemoDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            userRepository.save(new UserAccount("demo_user"));
        }
    }
}
