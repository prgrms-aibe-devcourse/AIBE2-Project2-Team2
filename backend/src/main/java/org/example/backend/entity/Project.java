package org.example.backend.entity;

import lombok.Getter;
import org.example.backend.constant.Status;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Table(name = "projects")
public class Project extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

}