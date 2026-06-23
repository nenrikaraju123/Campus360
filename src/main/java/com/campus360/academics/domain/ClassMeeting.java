package com.campus360.academics.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** An individual class session for a section. */
@Entity
@Table(name = "class_meetings")
@Getter
@Setter
public class ClassMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(length = 300)
    private String topic;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 120)
    private String createdBy;
}
