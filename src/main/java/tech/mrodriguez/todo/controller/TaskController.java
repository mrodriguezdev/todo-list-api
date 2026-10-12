package tech.mrodriguez.todo.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import tech.mrodriguez.todo.model.dto.CustomExceptionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.mrodriguez.todo.model.dto.PageResponse;
import tech.mrodriguez.todo.model.dto.TaskFilterRequest;
import tech.mrodriguez.todo.model.dto.TaskRequest;
import tech.mrodriguez.todo.model.dto.TaskResponse;
import tech.mrodriguez.todo.service.TaskService;

import java.util.List;

@RestController
@Tag(name = "Tareas")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "JWT ausente, inválido o vencido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario o tarea no encontrados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class))),
        @ApiResponse(responseCode = "429", description = "Cuota de peticiones agotada", headers = @Header(name = "Retry-After", description = "Segundos de espera antes de reintentar", schema = @Schema(type = "integer")), content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
})
@RequestMapping(value = "tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Listar mis tareas", description = "Devuelve las tareas del usuario autenticado con paginación y filtro opcional por título.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de tareas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<List<TaskResponse>>> list(@Parameter(hidden = true) Authentication authentication,
                                                                 @ParameterObject @Valid @ModelAttribute TaskFilterRequest filter) {
        return ResponseEntity.ok()
                .body(this.taskService.list(Long.parseLong(authentication.getName()), filter));
    }

    @Operation(summary = "Consultar una tarea", description = "Consulta una tarea que pertenece al usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "403", description = "La tarea pertenece a otro usuario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @GetMapping(value = "{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> findById(@Parameter(hidden = true) Authentication authentication, @Parameter(description = "ID de la tarea", example = "1") @PathVariable Long taskId) {
        return ResponseEntity.ok()
                .body(this.taskService.findById(Long.parseLong(authentication.getName()), taskId));
    }

    @Operation(summary = "Crear una tarea", description = "Crea una tarea para el usuario autenticado. La descripción es opcional.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos de la tarea inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> create(@Parameter(hidden = true) Authentication authentication, @RequestBody @Valid TaskRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.taskService.create(Long.parseLong(authentication.getName()), request));
    }

    @Operation(summary = "Actualizar una tarea", description = "Reemplaza el título y la descripción de una tarea propia. Omitir la descripción la elimina.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea actualizada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos de la tarea inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class))),
            @ApiResponse(responseCode = "403", description = "La tarea pertenece a otro usuario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @PutMapping(value = "{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> update(@Parameter(hidden = true) Authentication authentication, @Parameter(description = "ID de la tarea", example = "1") @PathVariable Long taskId,
                                               @RequestBody @Valid TaskRequest request) {
        return ResponseEntity
                .ok()
                .body(this.taskService.update(Long.parseLong(authentication.getName()), taskId, request));
    }

    @Operation(summary = "Eliminar una tarea", description = "Elimina una tarea propia. No devuelve un cuerpo de respuesta.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarea eliminada", content = @Content),
            @ApiResponse(responseCode = "403", description = "La tarea pertenece a otro usuario", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @DeleteMapping(value = "{taskId}")
    public ResponseEntity<Object> delete(@Parameter(hidden = true) Authentication authentication, @Parameter(description = "ID de la tarea", example = "1") @PathVariable Long taskId) {
        this.taskService.delete(Long.parseLong(authentication.getName()), taskId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
