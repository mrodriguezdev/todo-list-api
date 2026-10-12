package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Tarea del usuario autenticado")
public class TaskResponse {
    @Schema(description = "ID de la tarea", example = "1")
    private Long id;
    @Schema(description = "Título de la tarea", example = "Comprar comida")
    private String title;
    @Schema(description = "Descripción de la tarea", example = "Completar antes del viernes")
    private String description;
}
