package tech.mrodriguez.todo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.mrodriguez.todo.exception.CustomException;
import tech.mrodriguez.todo.mapper.AuthMapper;
import tech.mrodriguez.todo.mapper.UserMapper;
import tech.mrodriguez.todo.model.dto.AuthResponse;
import tech.mrodriguez.todo.model.dto.LoginRequest;
import tech.mrodriguez.todo.model.dto.RegisterRequest;
import tech.mrodriguez.todo.model.entity.User;
import tech.mrodriguez.todo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final EncryptService encryptService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        this.validateEmailExists(request.email());
        this.validateUsernameExists(request.username());
        User user = this.userMapper.toUser(request);
        user.setPasswordHash(this.encryptService.encode(request.password()));
        User savedUser = this.userRepository.save(user);
        return this.authMapper.toAuthResponse(savedUser, this.jwtService.issue(savedUser));
    }

    private void validateEmailExists(String email) {
        if (this.userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, "Email or Username already registered");
        }
    }

    private void validateUsernameExists(String username) {
        if (this.userRepository.findByUsername(username).isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, "Email or Username already registered");
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = this.findUserForLogin(request.username());
        if (!this.encryptService.match(request.password(), user.getPasswordHash())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return this.authMapper.toAuthResponse(user, jwtService.issue(user));
    }

    private User findUserForLogin(String username) {
        return this.userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }
}