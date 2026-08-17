package com.worktime.repository;

import com.worktime.entity.Task;
import com.worktime.entity.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskStatusHistoryRepository
        extends JpaRepository<TaskStatusHistory, Long> {

    List<TaskStatusHistory>
    findByTaskOrderByChangedAtDesc(Task task);
}