package tech.mrodriguez.todo.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado paginado")
public record PageResponse<T>(
        @Schema(description = "Elementos de la página")
        T data,
        @Schema(description = "Página actual, comenzando en 1", example = "1")
        Integer page,
        @Schema(description = "Cantidad máxima de elementos por página", example = "5")
        Integer limit,
        @Schema(description = "Total de elementos que coinciden con el filtro", example = "12")
        Long total
) {}
