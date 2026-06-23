package com.campus360.timetable.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank @Size(max = 30) String roomCode,
        @Size(max = 100) String name,
        @Size(max = 100) String building,
        @Size(max = 20) String floor,
        Integer capacity,
        String roomType
) {}
