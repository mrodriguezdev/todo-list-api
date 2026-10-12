package tech.mrodriguez.todo;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tech.mrodriguez.todo.config.JwtConfig;
import tech.mrodriguez.todo.config.SecurityConfig;
import tech.mrodriguez.todo.controller.TaskController;
import tech.mrodriguez.todo.exception.GlobalExceptionHandler;
import tech.mrodriguez.todo.mapper.TaskMapper;
import tech.mrodriguez.todo.model.entity.Task;
import tech.mrodriguez.todo.model.entity.User;
import tech.mrodriguez.todo.repository.TaskRepository;
import tech.mrodriguez.todo.repository.UserRepository;
import tech.mrodriguez.todo.service.JwtService;
import tech.mrodriguez.todo.service.TaskService;
import tech.mrodriguez.todo.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(TaskControllerTests.Config.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.issuer=todo-list-api", "app.jwt.ttl=PT10M"})
class TaskControllerTests {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, JwtConfig.class, JwtService.class, TaskController.class,
            TaskService.class, UserService.class, TaskMapper.class, GlobalExceptionHandler.class})
    static class Config {
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean TaskRepository taskRepository() { return mock(TaskRepository.class); }
    }

    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired TaskRepository tasks;
    @Autowired JwtService jwtService;

    private MockMvc mvc;
    private User owner;
    private Task task;
    private String token;

    @BeforeEach
    void setUp() {
        reset(users, tasks);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("mario");
        owner.setFirstName("Mario");
        owner.setLastName("Rodriguez");
        task = new Task();
        task.setId(10L);
        task.setTitle("Terminar API");
        task.setDescription("Agregar pruebas");
        task.setUser(owner);
        when(users.findById(1L)).thenReturn(Optional.of(owner));
        when(tasks.findById(10L)).thenReturn(Optional.of(task));
        token = jwtService.issue(owner);
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
    }

    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder operation(String method) {
        return switch (method) {
            case "GET" -> get("/tasks/10");
            case "PUT" -> put("/tasks/10").contentType("application/json")
                    .content("{\"title\":\"Actualizada\",\"description\":\"Nueva descripción\"}");
            case "DELETE" -> delete("/tasks/10");
            default -> throw new IllegalArgumentException(method);
        };
    }

    @Test
    void listsOnlyTasksOfAuthenticatedUser() throws Exception {
        when(tasks.findByUserId(eq(1L), any(Pageable.class))).thenAnswer(invocation ->
                new PageImpl<>(List.of(task), invocation.getArgument(1, Pageable.class), 11));
        mvc.perform(authenticated(get("/tasks")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].title").value("Terminar API"))
                .andExpect(jsonPath("$.data[0].description").value("Agregar pruebas"))
                .andExpect(jsonPath("$.data[0].user").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.total").value(11));
        verify(tasks).findByUserId(eq(1L), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 5));
        verify(tasks, never()).findAll();
    }

    @Test
    void filtersTitleWithinAuthenticatedUsersTasksAndPaginates() throws Exception {
        when(tasks.findByUserIdAndTitleLike(eq(1L), eq("%API%"), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(task),
                        invocation.getArgument(2, Pageable.class), 6));
        mvc.perform(authenticated(get("/tasks").param("title", "API")
                        .param("page", "2").param("limit", "5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Terminar API"))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.total").value(6));
        verify(tasks).findByUserIdAndTitleLike(eq(1L), eq("%API%"),
                argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 5));
        verify(tasks, never()).findByUserId(anyLong(), any(Pageable.class));
    }

    @Test
    void returnsEmptyPageWhenTitleDoesNotMatch() throws Exception {
        when(tasks.findByUserIdAndTitleLike(eq(1L), eq("%missing%"), any(Pageable.class)))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(2, Pageable.class)));
        mvc.perform(authenticated(get("/tasks").param("title", "missing")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void ignoresBlankTitleFilter(String title) throws Exception {
        when(tasks.findByUserId(eq(1L), any(Pageable.class)))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1, Pageable.class)));
        mvc.perform(authenticated(get("/tasks").param("title", title)))
                .andExpect(status().isOk());
        verify(tasks).findByUserId(eq(1L), any(Pageable.class));
        verify(tasks, never()).findByUserIdAndTitleLike(anyLong(), anyString(), any(Pageable.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"page", "limit"})
    void rejectsInvalidPagination(String parameter) throws Exception {
        for (String value : List.of("0", "-1", "abc")) {
            mvc.perform(authenticated(get("/tasks").param(parameter, value)))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(users, tasks);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page", "limit"})
    void returnsPaginationValidationMessage(String parameter) throws Exception {
        String name = parameter.equals("page") ? "Page" : "Limit";
        mvc.perform(authenticated(get("/tasks").param(parameter, "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(parameter + ": " + name + " must be greater than or equal to 1"));
        verifyNoInteractions(users, tasks);
    }

    @Test
    void returnsEmptyListWhenUserHasNoTasks() throws Exception {
        when(tasks.findByUserId(eq(1L), any(Pageable.class))).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(1, Pageable.class)));
        mvc.perform(authenticated(get("/tasks")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void findsOwnTask() throws Exception {
        mvc.perform(authenticated(get("/tasks/10")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
        verify(tasks).findById(10L);
    }

    @Test
    void createsTaskForAuthenticatedUserWithOptionalDescription() throws Exception {
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            assertSame(owner, saved.getUser());
            assertEquals("Nueva tarea", saved.getTitle());
            assertNull(saved.getDescription());
            saved.setId(11L);
            return saved;
        });
        mvc.perform(authenticated(post("/tasks")).contentType("application/json")
                .content("{\"title\":\"Nueva tarea\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.title").value("Nueva tarea"));
        verify(tasks).save(any(Task.class));
    }

    @Test
    void updatesOwnTaskAndPreservesOwner() throws Exception {
        mvc.perform(authenticated(operation("PUT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Actualizada"))
                .andExpect(jsonPath("$.description").value("Nueva descripción"));
        assertSame(owner, task.getUser());
        assertEquals("Actualizada", task.getTitle());
    }

    @Test
    void putClearsDescriptionWhenOmitted() throws Exception {
        mvc.perform(authenticated(put("/tasks/10")).contentType("application/json")
                .content("{\"title\":\"Actualizada\"}"))
                .andExpect(status().isOk());
        assertNull(task.getDescription());
        assertSame(owner, task.getUser());
    }

    @Test
    void deletesOwnTaskAndReturnsNoContent() throws Exception {
        mvc.perform(authenticated(delete("/tasks/10")))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(tasks).delete(task);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void rejectsAccessToAnotherUsersTask(String method) throws Exception {
        User other = new User();
        other.setId(2L);
        other.setUsername("otro");
        when(users.findById(2L)).thenReturn(Optional.of(other));
        token = jwtService.issue(other);
        mvc.perform(authenticated(operation(method)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this task"));
        verify(tasks).findById(10L);
        verify(tasks, never()).save(any(Task.class));
        verify(tasks, never()).delete(any(Task.class));
        assertEquals("Terminar API", task.getTitle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void returnsNotFoundForMissingTask(String method) throws Exception {
        when(tasks.findById(10L)).thenReturn(Optional.empty());
        mvc.perform(authenticated(operation(method)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
        verify(tasks, never()).save(any(Task.class));
        verify(tasks, never()).delete(any(Task.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE", "LIST", "POST"})
    void rejectsRequestsWithoutToken(String method) throws Exception {
        var request = switch (method) {
            case "LIST" -> get("/tasks");
            case "POST" -> post("/tasks").contentType("application/json").content("{\"title\":\"Nueva\"}");
            default -> operation(method);
        };
        mvc.perform(request).andExpect(status().isUnauthorized());
        verifyNoInteractions(users, tasks);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE", "LIST", "POST"})
    void rejectsDeletedUserWithValidToken(String method) throws Exception {
        when(users.findById(1L)).thenReturn(Optional.empty());
        var request = switch (method) {
            case "LIST" -> get("/tasks");
            case "POST" -> post("/tasks").contentType("application/json").content("{\"title\":\"Nueva\"}");
            default -> operation(method);
        };
        mvc.perform(authenticated(request)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
        verifyNoInteractions(tasks);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void rejectsMissingBlankAndOversizedTitles(String method) throws Exception {
        for (String body : List.of("{}", "{\"title\":null}", "{\"title\":\"\"}",
                "{\"title\":\"   \"}", "{\"title\":\"" + "a".repeat(201) + "\"}")) {
            var request = method.equals("POST") ? post("/tasks") : put("/tasks/10");
            mvc.perform(authenticated(request).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        }
        verifyNoInteractions(users, tasks);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void acceptsTitleAtMaximumLength(String method) throws Exception {
        String title = "a".repeat(200);
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });
        var request = method.equals("POST") ? post("/tasks") : put("/tasks/10");
        mvc.perform(authenticated(request).contentType("application/json")
                .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().is(method.equals("POST") ? 201 : 200))
                .andExpect(jsonPath("$.title").value(title));
    }
}
