import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { Task } from '../../core/models/task';
import { TaskService } from '../../core/services/task';

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  private taskService = inject(TaskService);

  protected readonly overdueTasks =
    signal<Task[]>([]);

  protected readonly isLoading = signal(true);

  ngOnInit(): void {
    this.loadOverdueTasks();
  }

  protected overdueDays(dueDate: string): number {
    const [year, month, day] =
      dueDate.split('-').map(Number);

    const dueDateUtc = Date.UTC(
      year,
      month - 1,
      day
    );

    const today = new Date();

    const todayUtc = Date.UTC(
      today.getFullYear(),
      today.getMonth(),
      today.getDate()
    );

    return Math.max(
      0,
      Math.round(
        (todayUtc - dueDateUtc)
        / (1000 * 60 * 60 * 24)
      )
    );
  }

  protected formatDate(dueDate: string): string {
    return new Date(
      `${dueDate}T00:00:00`
    ).toLocaleDateString();
  }

  protected priorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  private loadOverdueTasks(): void {
    this.isLoading.set(true);

    this.taskService.getOverdue()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        })
      )
      .subscribe({
        next: (tasks) => {
          this.overdueTasks.set(tasks);
        },
        error: (err) => {
          console.error(
            'Overdue tasks could not be loaded.',
            err
          );
        }
      });
  }
}