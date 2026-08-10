package com.worktime.repository;

import com.worktime.entity.Project;
import com.worktime.entity.Task;
import com.worktime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedUser(User assignedUser);

    boolean existsByProjectAndDueDateBefore(
            Project project,
            LocalDate startDate
    );

    boolean existsByProjectAndDueDateAfter(
            Project project,
            LocalDate endDate
    );
}
