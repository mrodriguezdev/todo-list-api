package tech.mrodriguez.todo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncryptService {

    private final PasswordEncoder passwordEncoder;

    public String encode(String password) {
        return this.passwordEncoder.encode(password);
    }

    public boolean match(String password, String passwordHash) {
        return this.passwordEncoder.matches(password, passwordHash);
    }
}
