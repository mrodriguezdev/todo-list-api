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
import tech.mrodriguez.todo.interceptor.RateLimitInterceptor;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(JwtAuthenticationTests.Config.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=todo-list-api", "app.jwt.ttl=PT10M",
        "app.rate-limit.requests-per-minute=60"})
class JwtAuthenticationTests {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, JwtConfig.class, PasswordConfig.class, JwtService.class,
            AuthController.class, ProtectedController.class, AuthService.class,
            EncryptService.class, UserMapper.class, GlobalExceptionHandler.class,
            WebConfig.class, RateLimitInterceptor.class, RateLimitService.class})
    static class Config {
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
    }
    @RestController
    static class ProtectedController {
        @GetMapping("/tasks/rate-limit-test")
        Map<String, String> limited() {
            return Map.of("message", "ok");
        }

        @GetMapping("/test/authenticated")
        Map<String, String> authenticated(JwtAuthenticationToken authentication) {
            return Map.of("username", authentication.getToken().getClaimAsString("username"));
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired UserRepository repository;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtDecoder decoder;
    @Autowired JwtEncoder encoder;

    @Test
    void limitsByJwtSubjectAndKeepsUsersIndependent() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
        mvc.perform(get("/tasks/rate-limit-test")).andExpect(status().isUnauthorized());

        String firstToken = tokenFor("rate-user-1");
        for (int i = 0; i < 60; i++) {
            mvc.perform(get("/tasks/rate-limit-test").header("Authorization", "Bearer " + firstToken))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Rate-Limit-Remaining", String.valueOf(59 - i)));
        }
        // Issuing another JWT for the same user must not reset their quota.
        mvc.perform(get("/tasks/rate-limit-test").header("Authorization", "Bearer " + tokenFor("rate-user-1")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-Rate-Limit-Remaining", "0"))
                .andExpect(jsonPath("$.status").value(429));
        mvc.perform(get("/tasks/rate-limit-test").header("Authorization", "Bearer " + tokenFor("rate-user-2")))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Rate-Limit-Remaining", "59"));
    }

    private String tokenFor(String subject) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("todo-list-api").subject(subject)
                .issuedAt(now).expiresAt(now.plusSeconds(600)).build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void registerLoginAndAuthenticateWithoutSessionOrCsrf() throws Exception {
        reset(repository);
        User user = new User();
        user.setId(1L);
        user.setUsername("mario");
        user.setFirstName("Mario");
        user.setLastName("Rodriguez");
        user.setPasswordHash(passwords.encode("password123"));
        when(repository.findByUsername("mario")).thenReturn(Optional.of(user));
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
        mvc.perform(get("/test/authenticated")).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"username\":\"mario\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/auth/login").contentType("application/json")
                .content("{\"username\":\"mario\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user").doesNotExist()).andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        assertNull(login.getRequest().getSession(false));
        var jwt = decoder.decode(token);
        assertEquals("1", jwt.getSubject());
        assertEquals(1L, ((Number) jwt.getClaim("id")).longValue());
        assertEquals("mario", jwt.getClaimAsString("username"));
        assertEquals("Mario Rodriguez", jwt.getClaimAsString("name"));
        assertEquals(Duration.ofMinutes(10), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        mvc.perform(get("/test/authenticated").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("mario"));
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].startsWith("A") ? "B" : "A") + parts[2].substring(1);
        mvc.perform(get("/test/authenticated").header("Authorization", "Bearer " + String.join(".", parts)))
                .andExpect(status().isUnauthorized());
        var expired = JwtClaimsSet.builder().issuer("todo-list-api").subject("1")
                .issuedAt(Instant.now().minusSeconds(3600)).expiresAt(Instant.now().minusSeconds(120)).build();
        String expiredToken = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), expired)).getTokenValue();
        mvc.perform(get("/test/authenticated").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
