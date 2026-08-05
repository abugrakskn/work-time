package com.worktime.repository;

import com.worktime.entity.Task;
import com.worktime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedUser(User assignedUser);
}
