import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { Task } from '../../core/models/task';
import { TaskService } from '../../core/services/task';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-tasks',
  imports: [
    RouterLink,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './tasks.html',
  styleUrl: './tasks.scss'
})
export class Tasks implements OnInit {

  private taskService = inject(TaskService);
  protected readonly isAdmin = inject(AuthService).isAdmin;

  allTasks = signal<Task[]>([]);
  tasks = signal<Task[]>([]);

  selectedPriority = '';
  selectedStatus = '';
  selectedProject = '';
  selectedAssignedUser = '';

  ngOnInit() {
    this.loadTasks();
  }

  loadTasks() {
    this.taskService.getAll().subscribe({
      next: (tasks) => {
        this.allTasks.set(tasks);
        this.applyFilters();
      },
      error: (err) => {
        console.error('Tasks could not be loaded.', err);
      }
    });
  }

  applyFilters() {

    let filteredTasks = [...this.allTasks()];

    if (this.selectedPriority) {
      filteredTasks = filteredTasks.filter(
        task => task.priority === this.selectedPriority
      );
    }

    if (this.selectedStatus) {
      filteredTasks = filteredTasks.filter(
        task => task.status === this.selectedStatus
      );
    }

    if (this.selectedProject) {
      filteredTasks = filteredTasks.filter(
        task => task.projectName === this.selectedProject
      );
    }

    if (this.selectedAssignedUser) {
      filteredTasks = filteredTasks.filter(
        task => task.assignedUserName === this.selectedAssignedUser
      );
    }

    const priorityOrder: Record<string, number> = {
      CRITICAL: 4,
      HIGH: 3,
      MEDIUM: 2,
      LOW: 1
    };

    filteredTasks.sort(
      (a, b) =>
        (priorityOrder[b.priority] ?? 0) -
        (priorityOrder[a.priority] ?? 0)
    );

    this.tasks.set(filteredTasks);
  }

  clearFilters() {
    this.selectedPriority = '';
    this.selectedStatus = '';
    this.selectedProject = '';
    this.selectedAssignedUser = '';

    this.applyFilters();
  }

  getProjects(): string[] {
    return [
      ...new Set(
        this.allTasks()
          .map(task => task.projectName)
          .filter(projectName => !!projectName)
      )
    ];
  }

  getAssignedUsers(): string[] {
    return [
      ...new Set(
        this.allTasks()
          .map(task => task.assignedUserName)
          .filter(
            (assignedUserName): assignedUserName is string =>
              assignedUserName !== null
          )
      )
    ];
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'TODO':
        return 'status-todo';

      case 'IN_PROGRESS':
        return 'status-progress';

      case 'COMPLETED':
        return 'status-completed';

      case 'CANCELLED':
        return 'status-cancelled';

      default:
        return 'status-default';
    }
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'LOW':
        return 'priority-low';

      case 'MEDIUM':
        return 'priority-medium';

      case 'HIGH':
        return 'priority-high';

      case 'CRITICAL':
        return 'priority-critical';

      default:
        return 'priority-default';
    }
  }
}