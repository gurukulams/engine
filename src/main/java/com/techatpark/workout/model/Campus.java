package com.techatpark.workout.model;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record Campus(UUID id, String title, String description,
                     @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                        LocalDateTime createdAt,
                     @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                        String createdBy,
                     @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                        LocalDateTime modifiedAt,
                     @Schema(accessMode = Schema.AccessMode.READ_ONLY)
                        String modifiedBy) {


}
