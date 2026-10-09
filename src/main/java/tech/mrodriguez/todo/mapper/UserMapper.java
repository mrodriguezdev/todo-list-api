package tech.mrodriguez.todo.mapper;

import org.springframework.stereotype.Component;
import tech.mrodriguez.todo.model.dto.RegisterRequest;
import tech.mrodriguez.todo.model.dto.UserResponse;
import tech.mrodriguez.todo.model.entity.User;

import java.util.Objects;

@Component
public class UserMapper {
    public User toUser(RegisterRequest registerRequest) {
        if (Objects.isNull(registerRequest)) return null;
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());
        user.setEmail(registerRequest.email());
        return user;
    }
}
