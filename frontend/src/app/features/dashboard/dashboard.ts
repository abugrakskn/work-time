import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { Task } from '../../core/models/task';
import { TimeEntry } from '../../core/models/time-entry';
import { TimeSummary } from '../../core/models/time-summary';

import { TaskService } from '../../core/services/task';
import {
  TimeEntryService
} from '../../core/services/time-entry';

import {
  toLocalDateString
} from '../../core/utils/date.utils';

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
  private timeEntryService = inject(TimeEntryService);

  protected readonly overdueTasks =
    signal<Task[]>([]);

  protected readonly dailySummary =
    signal<TimeSummary | null>(null);

  protected readonly weeklySummary =
    signal<TimeSummary | null>(null);

  protected readonly activeTimeEntry =
    signal<TimeEntry | null>(null);

  protected readonly isLoading = signal(true);

  ngOnInit(): void {
    this.loadDashboardData();
  }

  protected formatDuration(
    durationMinutes: number
  ): string {
    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    if (hours === 0) {
      return `${minutes}m`;
    }

    if (minutes === 0) {
      return `${hours}h`;
    }

    return `${hours}h ${minutes}m`;
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

  private loadDashboardData(): void {
    const today = toLocalDateString(new Date());

    this.isLoading.set(true);

    forkJoin({
      overdueTasks:
        this.taskService.getOverdue(),

      dailySummary:
        this.timeEntryService.getDailySummary(today),

      weeklySummary:
        this.timeEntryService.getWeeklySummary(today),

      activeTimeEntry:
        this.timeEntryService.getActive()
    })
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        })
      )
      .subscribe({
        next: (response) => {
          this.overdueTasks.set(
            response.overdueTasks
          );

          this.dailySummary.set(
            response.dailySummary
          );

          this.weeklySummary.set(
            response.weeklySummary
          );

          this.activeTimeEntry.set(
            response.activeTimeEntry
          );
        },
        error: (err) => {
          console.error(
            'Dashboard data could not be loaded.',
            err
          );
        }
      });
  }
}