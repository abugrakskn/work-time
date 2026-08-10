package com.worktime.service;

import com.worktime.dto.project.CreateProjectRequest;
import com.worktime.dto.project.ProjectResponse;
import com.worktime.dto.project.UpdateProjectRequest;
import com.worktime.entity.Project;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService{

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        return toResponse(project);
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        validateProjectDates(
                request.getStartDate(),
                request.getEndDate()
        );

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .build();

        Project savedProject = projectRepository.save(project);

        return toResponse(savedProject);
    }

    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        validateProjectDates(
                request.getStartDate(),
                request.getEndDate()
        );
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateExistingTaskDates(
                project,
                request.getStartDate(),
                request.getEndDate()
        );

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());

        Project updatedProject = projectRepository.save(project);

        return toResponse(updatedProject);
    }



    private void validateProjectDates(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null) {
            throw new IllegalArgumentException("Project start date is required");
        }

        if (endDate == null) {
            throw new IllegalArgumentException("Project end date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Project end date cannot be before start date");
        }
    }

    private void validateExistingTaskDates(
            Project project,
            LocalDate startDate,
            LocalDate endDate
    ) {
        boolean hasTaskBeforeStart =
                taskRepository.existsByProjectAndDueDateBefore(
                        project,
                        startDate
                );

        if (hasTaskBeforeStart) {
            throw new IllegalArgumentException("Project start date cannot be after an existing task due date");
        }

        boolean hasTaskAfterEnd =
                taskRepository.existsByProjectAndDueDateAfter(
                        project,
                        endDate
                );

        if (hasTaskAfterEnd) {
            throw new IllegalArgumentException("Project end date cannot be before an existing task due date");
        }
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus()
        );
    }
}