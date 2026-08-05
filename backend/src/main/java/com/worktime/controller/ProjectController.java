package com.worktime.controller;

import com.worktime.dto.project.CreateProjectRequest;
import com.worktime.dto.project.ProjectResponse;
import com.worktime.dto.project.UpdateProjectRequest;
import com.worktime.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project Management",
    description = "Operations for managing projects.")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Get all projects",
                description = "Returns all projects in the system")
    @ApiResponse(responseCode = "200",
                description = "Projects retrieved successfully")
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID",
                description = "Returns the project with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                        description = "Project retrieved successfully"),
            @ApiResponse(responseCode = "404",
                        description = "Project not found")
    })
    public ProjectResponse getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project",
                description = "Creates a new project using the provided project details")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                        description = "Project created successfully"),
            @ApiResponse(responseCode = "400",
                        description = "Invalid project data")
    })
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.createProject(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project",
                description = "Updates the project with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Project updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid project data"),
            @ApiResponse(responseCode = "404",
                    description = "Project not found")
    })
    public ProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return projectService.updateProject(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a project",
                description = "Deletes the project with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404",
                    description = "Project not found")
    })
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }
}