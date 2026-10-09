package tech.mrodriguez.todo.mapper;

import org.springframework.stereotype.Component;
import tech.mrodriguez.todo.model.dto.AuthResponse;
import tech.mrodriguez.todo.model.entity.User;

import java.util.Objects;

@Component
public class AuthMapper {
    public AuthResponse toAuthResponse(User user, String token) {
        if (Objects.isNull(user)) return null;
        return new AuthResponse(user.getId(), user.getUsername(), String.format("%s %s", user.getFirstName(), user.getLastName()), token);
    }
}
