package com.techatpark.workout.model;

import java.time.LocalDateTime;

public record Event(String id, String title, LocalDateTime createdAt,
                    LocalDateTime modifiedAt, String location,
                    LocalDateTime startsAt, LocalDateTime endsAt,
                    String description, String organizer,
                    Integer maxAttendees) {
}

