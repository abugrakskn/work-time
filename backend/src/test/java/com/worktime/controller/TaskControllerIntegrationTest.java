package com.worktime.controller;

import com.worktime.dto.task.TaskResponse;
import com.worktime.entity.TaskPriority;
import com.worktime.entity.TaskStatus;
import com.worktime.security.config.SecurityConfig;
import com.worktime.security.handler.SecurityErrorResponseWriter;
import com.worktime.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class,
        SecurityErrorResponseWriter.class})
class TaskControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(
            username = "employee@example.com",
            roles = "EMPLOYEE"
    )
    void getAllTasksShouldReturnAccessibleTasks()
            throws Exception {
        // Arrange
        TaskResponse task = new TaskResponse(
                1L,
                "Write controller tests",
                "Test the MVC layer",
                LocalDate.of(2026, 8, 20),
                120,
                TaskPriority.HIGH,
                TaskStatus.TODO,
                10L,
                "WorkTime",
                2L,
                "Test Employee"
        );

        when(
                taskService.getAllTasks(
                        "employee@example.com"
                )
        ).thenReturn(List.of(task));

        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value(
                                        "Write controller tests"
                                )
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("TODO")
                )
                .andExpect(
                        jsonPath("$[0].priority")
                                .value("HIGH")
                );

        verify(taskService).getAllTasks(
                "employee@example.com"
        );
    }

    @Test
    void getAllTasksShouldRejectUnauthenticatedRequest()
            throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(
                        status().isUnauthorized()
                );
    }
}