package tech.mrodriguez.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.mrodriguez.todo.config.JwtConfig;
import tech.mrodriguez.todo.config.OpenApiConfig;
import tech.mrodriguez.todo.config.SecurityConfig;
import tech.mrodriguez.todo.controller.AuthController;
import tech.mrodriguez.todo.controller.TaskController;
import tech.mrodriguez.todo.service.AuthService;
import tech.mrodriguez.todo.service.TaskService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = OpenApiTests.Config.class, properties = {
        "spring.profiles.active=test",
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=todo-list-api"})
@AutoConfigureMockMvc
class OpenApiTests {
    @Configuration
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class, DataJpaRepositoriesAutoConfiguration.class})
    @Import({SecurityConfig.class, JwtConfig.class, OpenApiConfig.class,
            AuthController.class, TaskController.class})
    static class Config {
    }

    @Autowired MockMvc mvc;
    @MockitoBean AuthService authService;
    @MockitoBean TaskService taskService;

    @Test
    void documentsJwtForTasksAndLeavesAuthenticationPublic() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Todo List API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/tasks'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/auth/register'].post.security").doesNotExist());
    }

    @Test
    void documentsResponsesFiltersAndRequestModels() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/tasks'].post.responses['201'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/TaskResponse"))
                .andExpect(jsonPath("$.paths['/tasks'].get.responses['429'].headers['Retry-After']").exists())
                .andExpect(jsonPath("$.paths['/tasks/{taskId}'].delete.responses['204'].content").doesNotExist())
                .andExpect(jsonPath("$.paths['/tasks'].get.parameters[?(@.name == 'page')].schema.default").value(org.hamcrest.Matchers.hasItem(1)))
                .andExpect(jsonPath("$.paths['/tasks'].get.parameters[?(@.name == 'title')].in").value(org.hamcrest.Matchers.hasItem("query")))
                .andExpect(jsonPath("$.components.schemas.TaskRequest.properties.title.example").value("Comprar comida"))
                .andExpect(jsonPath("$.components.schemas.RegisterRequest.properties.password.writeOnly").value(true))
                .andExpect(jsonPath("$.components.schemas.RegisterRequest.properties.passwordWithinBcryptLimit").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.AuthResponse.properties.token.description").exists());
    }

    @Test
    void servesSwaggerWithoutJwtAndKeepsTasksProtected() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
        mvc.perform(get("/tasks")).andExpect(status().isUnauthorized());
    }
}
