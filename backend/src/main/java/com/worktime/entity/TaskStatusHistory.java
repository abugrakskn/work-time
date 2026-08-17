package com.worktime.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "task_id",
            nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status",
            nullable = false,
            length = 30)
    private TaskStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status",
            nullable = false,
            length = 30)
    private TaskStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "changed_by_user_id",
            nullable = false)
    private User changedBy;

    @Column(name = "changed_at",
            nullable = false)
    private LocalDateTime changedAt;
}