package com.techatpark.workout.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record Book(UUID id, String title, String path,
                   String description,
                   @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                   LocalDateTime created_at,
                   @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                   String created_by,
                   @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                   LocalDateTime modified_at,
                   @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                   String modified_by) {
}

