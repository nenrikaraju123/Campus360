package com.campus360.timetable.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "room_code", nullable = false, length = 30)
    private String roomCode;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String building;

    @Column(length = 20)
    private String floor;

    @Column(nullable = false)
    private Integer capacity = 0;

    /** CLASSROOM, LAB, AUDITORIUM, WORKSHOP */
    @Column(name = "room_type", length = 30)
    private String roomType = "CLASSROOM";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
