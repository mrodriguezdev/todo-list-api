package tech.mrodriguez.todo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import tech.mrodriguez.todo.model.dto.CustomExceptionResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.mrodriguez.todo.model.dto.AuthResponse;
import tech.mrodriguez.todo.model.dto.LoginRequest;
import tech.mrodriguez.todo.model.dto.RegisterRequest;
import tech.mrodriguez.todo.service.AuthService;

@RestController
@Tag(name = "Autenticación")
@RequestMapping(value = "auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar un usuario", description = "Registra un usuario con nombre de usuario y correo únicos y devuelve un JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado y JWT emitido", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class))),
            @ApiResponse(responseCode = "409", description = "Correo o nombre de usuario ya registrados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @PostMapping(value = "register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.authService.register(request));
    }

    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y devuelve un JWT. Al vencer, se debe iniciar sesión de nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT emitido", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos de inicio de sesión inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomExceptionResponse.class)))
    })
    @PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(this.authService.login(request));
    }
}
