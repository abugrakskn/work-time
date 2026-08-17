import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { Task } from '../../../core/models/task';
import {
  TaskStatusHistory
} from '../../../core/models/task-status-history';

import { AuthService } from '../../../core/services/auth';
import { TaskService } from '../../../core/services/task';

@Component({
  selector: 'app-task-detail',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.scss'
})
export class TaskDetail implements OnInit {

  private route = inject(ActivatedRoute);
  private taskService = inject(TaskService);
  private authService = inject(AuthService);

  protected readonly currentUser =
    this.authService.currentUser;

  protected readonly isAdmin =
    this.authService.isAdmin;

  protected readonly task =
    signal<Task | null>(null);

  protected readonly statusHistory =
    signal<TaskStatusHistory[]>([]);

  protected readonly isHistoryLoading =
    signal(false);

  ngOnInit(): void {
    const id = Number(
      this.route.snapshot.paramMap.get('id')
    );

    this.loadTask(id);

    if (this.isAdmin()) {
      this.loadStatusHistory(id);
    }
  }

  protected canEditTask(task: Task): boolean {
    const user = this.currentUser();

    if (!user) {
      return false;
    }

    return user.role === 'ADMIN'
      || task.assignedUserId === user.id;
  }

  protected getStatusClass(status: string): string {
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

  protected getPriorityClass(
    priority: string
  ): string {
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

  protected formatStatus(status: string): string {
    return status.replaceAll('_', ' ');
  }

  protected formatDateTime(value: string): string {
    return new Date(value).toLocaleString();
  }

  private loadTask(id: number): void {
    this.taskService.getById(id).subscribe({
      next: (task) => {
        this.task.set(task);
      },
      error: (err) => {
        console.error(
          'Task could not be loaded.',
          err
        );
      }
    });
  }

  private loadStatusHistory(id: number): void {
    this.isHistoryLoading.set(true);

    this.taskService
      .getStatusHistory(id)
      .subscribe({
        next: (history) => {
          this.statusHistory.set(history);
          this.isHistoryLoading.set(false);
        },
        error: (err) => {
          this.isHistoryLoading.set(false);

          console.error(
            'Task status history could not be loaded.',
            err
          );
        }
      });
  }
}