import {
  Component,
  computed,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

import { CreateTaskRequest } from '../../../core/models/create-task-request';
import { Project } from '../../../core/models/project';
import { TaskStatus } from '../../../core/models/task-status';
import { UpdateTaskRequest } from '../../../core/models/update-task-request';
import { User } from '../../../core/models/user';

import { AuthService } from '../../../core/services/auth';
import { NotificationService } from '../../../core/services/notification';
import { ProjectService } from '../../../core/services/project';
import { TaskService } from '../../../core/services/task';
import { UserService } from '../../../core/services/user';

import { toLocalDateString } from '../../../core/utils/date.utils';

@Component({
  selector: 'app-task-form',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatSelectModule
  ],
  templateUrl: './task-form.html',
  styleUrl: './task-form.scss'
})
export class TaskForm implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);

  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private projectService = inject(ProjectService);
  private taskService = inject(TaskService);
  private userService = inject(UserService);

  protected readonly isAdmin = this.authService.isAdmin;

  isEditMode = signal(false);

  protected readonly isStatusOnlyMode = computed(() => {
    return this.isEditMode() && !this.isAdmin();
  });

  taskId: number | null = null;

  projects = signal<Project[]>([]);
  users = signal<User[]>([]);

  title = '';
  description = '';
  dueDate: Date | null = null;
  estimatedDurationMinutes: number | null = null;
  priority = 'MEDIUM';
  status: TaskStatus = 'TODO';
  projectId: number | null = null;
  assignedUserId: number | null = null;
  assignedUserName: string | null = null;

  minDate = new Date();

  ngOnInit() {
    this.loadProjects();

    if (this.isAdmin()) {
      this.loadUsers();
    }

    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.isEditMode.set(true);
      this.taskId = Number(idParam);

      this.loadTask(this.taskId);
    }
  }

  loadProjects() {
    this.projectService.getAll().subscribe({
      next: (projects) => {
        this.projects.set(projects);
      },
      error: (err) => {
        console.error('Projects could not be loaded.', err);
      }
    });
  }

  loadUsers() {
  this.userService.getAll().subscribe({
      next: (users) => {
        this.users.set(users);
      },
      error: (err) => {
        console.error('Users could not be loaded.', err);
      }
    });
  }

  loadTask(id: number) {
    this.taskService.getById(id).subscribe({
      next: (task) => {
        this.title = task.title;
        this.description = task.description;

        this.dueDate = task.dueDate
          ? new Date(task.dueDate)
          : null;

        this.estimatedDurationMinutes =
          task.estimatedDurationMinutes;

        this.priority = task.priority;
        this.status = task.status;
        this.projectId = task.projectId;
        this.assignedUserId = task.assignedUserId;
        this.assignedUserName = task.assignedUserName;
      },
      error: (err) => {
        console.error('Task could not be loaded.', err);
      }
    });
  }

  saveTask() {
    if (this.isEditMode() && this.taskId) {
      if (this.isAdmin()) {
        if (!this.projectId) {
          return;
        }

        this.updateTask();
      } else {
        this.updateTaskStatus();
      }

      return;
    }

    if (!this.projectId) {
      return;
    }

    this.createTask();
  }

  createTask() {
    const request: CreateTaskRequest = {
      title: this.title,
      description: this.description,
      dueDate: this.dueDate
        ? toLocalDateString(this.dueDate)
        : null,
      estimatedDurationMinutes: this.estimatedDurationMinutes,
      priority: this.priority,
      projectId: this.projectId!,
      assignedUserId: this.assignedUserId
    };

    this.taskService.create(request).subscribe({
      next: (task) => {
        this.notificationService.success(
          'Task created successfully.'
        );

        this.router.navigate(['/tasks', task.id]);
      },
      error: (err) => {
        console.error('Task could not be created.', err);
      }
    });
  }

  updateTask() {
    const request: UpdateTaskRequest = {
      title: this.title,
      description: this.description,
      dueDate: this.dueDate
        ? toLocalDateString(this.dueDate)
        : null,
      estimatedDurationMinutes: this.estimatedDurationMinutes,
      priority: this.priority,
      status: this.status,
      projectId: this.projectId!,
      assignedUserId: this.assignedUserId
    };

    this.taskService.update(this.taskId!, request).subscribe({
      next: () => {
        this.notificationService.success(
          'Task updated successfully.'
        );

        this.router.navigate(['/tasks', this.taskId]);
      },
      error: (err) => {
        console.error('Task could not be updated.', err);
      }
    });
  }

  updateTaskStatus() {
    this.taskService
      .updateStatus(this.taskId!, this.status)
      .subscribe({
        next: () => {
          this.notificationService.success(
            'Task status updated successfully.'
          );

          this.router.navigate(['/tasks', this.taskId]);
        },
        error: (err) => {
          console.error(
            'Task status could not be updated.',
            err
          );
        }
      });
  }

  cancel() {
    if (this.isEditMode() && this.taskId) {
      this.router.navigate(['/tasks', this.taskId]);
      return;
    }

    this.router.navigate(['/tasks']);
  }
}