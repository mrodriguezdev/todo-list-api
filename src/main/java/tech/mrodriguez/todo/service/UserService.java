package tech.mrodriguez.todo.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import tech.mrodriguez.todo.exception.CustomException;
import tech.mrodriguez.todo.mapper.UserMapper;
import tech.mrodriguez.todo.model.dto.UserResponse;
import tech.mrodriguez.todo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "User not found"));
        var response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        String firstName = java.util.Objects.toString(user.getFirstName(), "").strip();
        String lastName = java.util.Objects.toString(user.getLastName(), "").strip();
        response.setFullName((firstName + " " + lastName).strip());
        return response;
    }
}
