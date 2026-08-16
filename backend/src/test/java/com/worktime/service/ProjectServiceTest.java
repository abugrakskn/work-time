package com.worktime.service;

import com.worktime.dto.project.CreateProjectRequest;
import com.worktime.dto.project.ProjectResponse;
import com.worktime.dto.project.UpdateProjectRequest;
import com.worktime.entity.Project;
import com.worktime.entity.ProjectStatus;
import com.worktime.exception.ResourceNotFoundException;
import com.worktime.repository.ProjectRepository;
import com.worktime.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProjectShouldSaveProjectWithRequestValues() {
        // Arrange
        LocalDate startDate =
                LocalDate.of(2026, 8, 1);

        LocalDate endDate =
                LocalDate.of(2026, 8, 31);

        CreateProjectRequest request =
                org.mockito.Mockito.mock(
                        CreateProjectRequest.class
                );

        when(request.getName())
                .thenReturn("WorkTime");

        when(request.getDescription())
                .thenReturn("Time management project");

        when(request.getStartDate())
                .thenReturn(startDate);

        when(request.getEndDate())
                .thenReturn(endDate);

        when(request.getStatus())
                .thenReturn(ProjectStatus.ACTIVE);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // Act
        ProjectResponse response =
                projectService.createProject(request);

        // Assert
        assertThat(response).isNotNull();

        ArgumentCaptor<Project> projectCaptor =
                ArgumentCaptor.forClass(Project.class);

        verify(projectRepository).save(
                projectCaptor.capture()
        );

        Project savedProject =
                projectCaptor.getValue();

        assertThat(savedProject.getName())
                .isEqualTo("WorkTime");

        assertThat(savedProject.getDescription())
                .isEqualTo("Time management project");

        assertThat(savedProject.getStartDate())
                .isEqualTo(startDate);

        assertThat(savedProject.getEndDate())
                .isEqualTo(endDate);

        assertThat(savedProject.getStatus())
                .isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void createProjectShouldThrowWhenEndDateIsBeforeStartDate() {
        // Arrange
        CreateProjectRequest request =
                org.mockito.Mockito.mock(
                        CreateProjectRequest.class
                );

        when(request.getStartDate())
                .thenReturn(
                        LocalDate.of(2026, 8, 20)
                );

        when(request.getEndDate())
                .thenReturn(
                        LocalDate.of(2026, 8, 10)
                );

        // Act & Assert
        assertThatThrownBy(() ->
                projectService.createProject(request)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Project end date cannot be before start date"
                );

        verify(projectRepository, never())
                .save(any(Project.class));
    }

    @Test
    void getProjectByIdShouldThrowWhenProjectDoesNotExist() {
        // Arrange
        when(projectRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                projectService.getProjectById(99L)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage("Project not found");
    }

    @Test
    void updateProjectShouldUpdateExistingProject() {
        // Arrange
        Project project = Project.builder()
                .name("Old Project")
                .description("Old description")
                .startDate(
                        LocalDate.of(2026, 8, 1)
                )
                .endDate(
                        LocalDate.of(2026, 8, 31)
                )
                .status(ProjectStatus.PLANNED)
                .build();

        LocalDate newStartDate =
                LocalDate.of(2026, 8, 5);

        LocalDate newEndDate =
                LocalDate.of(2026, 9, 15);

        UpdateProjectRequest request =
                org.mockito.Mockito.mock(
                        UpdateProjectRequest.class
                );

        when(request.getName())
                .thenReturn("Updated Project");

        when(request.getDescription())
                .thenReturn("Updated description");

        when(request.getStartDate())
                .thenReturn(newStartDate);

        when(request.getEndDate())
                .thenReturn(newEndDate);

        when(request.getStatus())
                .thenReturn(ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(
                taskRepository
                        .existsByProjectAndDueDateBefore(
                                project,
                                newStartDate
                        )
        ).thenReturn(false);

        when(
                taskRepository
                        .existsByProjectAndDueDateAfter(
                                project,
                                newEndDate
                        )
        ).thenReturn(false);

        when(projectRepository.save(project))
                .thenReturn(project);

        // Act
        ProjectResponse response =
                projectService.updateProject(
                        1L,
                        request
                );

        // Assert
        assertThat(response).isNotNull();

        assertThat(project.getName())
                .isEqualTo("Updated Project");

        assertThat(project.getDescription())
                .isEqualTo("Updated description");

        assertThat(project.getStartDate())
                .isEqualTo(newStartDate);

        assertThat(project.getEndDate())
                .isEqualTo(newEndDate);

        assertThat(project.getStatus())
                .isEqualTo(ProjectStatus.ACTIVE);

        verify(projectRepository).save(project);
    }

    @Test
    void updateProjectShouldThrowWhenTaskExistsBeforeNewStartDate() {
        // Arrange
        Project project = Project.builder()
                .name("WorkTime")
                .startDate(
                        LocalDate.of(2026, 8, 1)
                )
                .endDate(
                        LocalDate.of(2026, 8, 31)
                )
                .status(ProjectStatus.ACTIVE)
                .build();

        LocalDate newStartDate =
                LocalDate.of(2026, 8, 10);

        LocalDate newEndDate =
                LocalDate.of(2026, 8, 31);

        UpdateProjectRequest request =
                org.mockito.Mockito.mock(
                        UpdateProjectRequest.class
                );

        when(request.getStartDate())
                .thenReturn(newStartDate);

        when(request.getEndDate())
                .thenReturn(newEndDate);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(
                taskRepository
                        .existsByProjectAndDueDateBefore(
                                project,
                                newStartDate
                        )
        ).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                projectService.updateProject(
                        1L,
                        request
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Project start date cannot be after an existing task due date"
                );

        verify(projectRepository, never())
                .save(any(Project.class));
    }
}