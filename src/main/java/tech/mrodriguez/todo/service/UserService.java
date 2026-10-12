package tech.mrodriguez.todo.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import tech.mrodriguez.todo.exception.CustomException;
import tech.mrodriguez.todo.model.entity.User;
import tech.mrodriguez.todo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }
}