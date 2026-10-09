package tech.mrodriguez.todo.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;

    public UserResponse() {
    }
}
