package com.techatpark.workout.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Subject(UUID id, String title, String description,
                      LocalDateTime created_at, String created_by,
                      LocalDateTime modified_at, String modified_by) {
}

