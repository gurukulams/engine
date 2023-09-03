package com.techatpark.workout.model;

import java.time.LocalDateTime;

public record Category(String id, String title,
                       LocalDateTime created_at, String created_by,
                       LocalDateTime modified_at, String modified_by) {
}

