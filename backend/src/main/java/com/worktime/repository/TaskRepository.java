package com.worktime.repository;

import com.worktime.entity.Project;
import com.worktime.entity.Task;
import com.worktime.entity.TaskStatus;
import com.worktime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
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

    List<Task> findByDueDateBeforeAndStatusInOrderByDueDateAsc(
            LocalDate dueDate,
            Collection<TaskStatus> statuses
    );

    List<Task>
    findByAssignedUserAndDueDateBeforeAndStatusInOrderByDueDateAsc(
            User assignedUser,
            LocalDate dueDate,
            Collection<TaskStatus> statuses
    );
}
