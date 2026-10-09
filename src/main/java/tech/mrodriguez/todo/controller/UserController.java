package tech.mrodriguez.todo.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import tech.mrodriguez.todo.model.dto.UserResponse;
import tech.mrodriguez.todo.service.UserService;

@RestController
@RequestMapping(value = "users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("me")
    public UserResponse me(Authentication authentication) {
        return this.userService.findById(Long.parseLong(authentication.getName()));
    }
}