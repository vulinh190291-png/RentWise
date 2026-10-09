package com.rentwise.auth.service;

import com.rentwise.auth.dto.LoginRequest;
import com.rentwise.auth.dto.LoginResponse;
import com.rentwise.auth.dto.RegisterRequest;
import com.rentwise.auth.dto.RegisterResponse;
import com.rentwise.common.exception.DomainException;
import com.rentwise.security.JwtService;
import com.rentwise.user.domain.UserAccount;
import com.rentwise.user.domain.UserRole;
import com.rentwise.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DomainException(HttpStatus.CONFLICT, "Username already exists");
        }

        var user = new UserAccount(
                request.username(),
                passwordEncoder.encode(request.password()),
                UserRole.LEARNER,
                true);
        var saved = userRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getRole());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            var user = userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .orElseThrow(() -> new DomainException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
            return new LoginResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds());
        } catch (AuthenticationException ex) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }
}
