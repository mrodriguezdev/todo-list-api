package tech.mrodriguez.todo;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tech.mrodriguez.todo.config.*;
import tech.mrodriguez.todo.controller.*;
import tech.mrodriguez.todo.exception.GlobalExceptionHandler;
import tech.mrodriguez.todo.mapper.*;
import tech.mrodriguez.todo.model.entity.User;
import tech.mrodriguez.todo.repository.UserRepository;
import tech.mrodriguez.todo.service.*;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(JwtAuthenticationTests.Config.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=todo-list-api", "app.jwt.ttl=PT30M"})
class JwtAuthenticationTests {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, JwtConfig.class, PasswordConfig.class, JwtService.class,
            AuthController.class, UserController.class, AuthService.class, UserService.class,
            EncryptService.class, AuthMapper.class, UserMapper.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
    }
    @Autowired WebApplicationContext context;
    @Autowired UserRepository repository;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtDecoder decoder;
    @Autowired JwtEncoder encoder;

    @Test
    void registerLoginAndAuthenticateWithoutSessionOrCsrf() throws Exception {
        reset(repository);
        User user = new User();
        user.setId(1L);
        user.setUsername("mario");
        user.setFirstName("Mario");
        user.setPasswordHash(passwords.encode("password123"));
        when(repository.findByUsername("mario")).thenReturn(Optional.of(user));
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        var mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
        var registration = mvc.perform(post("/auth/register").contentType("application/json")
                .content("{\"username\":\"nuevo\",\"firstName\":\"Mario\",\"email\":\"nuevo@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn();
        String registeredToken = com.jayway.jsonpath.JsonPath.read(registration.getResponse().getContentAsString(), "$.token");
        assertEquals("2", decoder.decode(registeredToken).getSubject());
        assertNull(registration.getRequest().getSession(false));
        mvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"username\":\"mario\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"username\":\"mario\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        assertNull(login.getRequest().getSession(false));
        assertEquals("1", decoder.decode(token).getSubject());
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("mario"));
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + String.join(".", parts)))
                .andExpect(status().isUnauthorized());
        var expired = JwtClaimsSet.builder().issuer("todo-list-api").subject("1")
                .issuedAt(Instant.now().minusSeconds(3600)).expiresAt(Instant.now().minusSeconds(120)).build();
        String expiredToken = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), expired)).getTokenValue();
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
