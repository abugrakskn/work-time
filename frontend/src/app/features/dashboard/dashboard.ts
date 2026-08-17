import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { downloadCsv } from '../../core/utils/csv.utils';

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

interface ChartDataPoint {
  date: string;
  label: string;
  durationMinutes: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [
    FormsModule,
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

  protected readonly timeEntries =
    signal<TimeEntry[]>([]);

  protected readonly dailySummary =
    signal<TimeSummary | null>(null);

  protected readonly weeklySummary =
    signal<TimeSummary | null>(null);

  protected readonly activeTimeEntry =
    signal<TimeEntry | null>(null);

  protected readonly isLoading = signal(true);

  protected readonly chartEndDate = signal(
    toLocalDateString(new Date())
  );

  protected readonly chartStartDate = signal(
    this.getDateDaysAgo(6)
  );

  protected readonly isChartRangeInvalid =
    computed(() =>
      !this.chartStartDate()
      || !this.chartEndDate()
      || this.chartStartDate() > this.chartEndDate()
    );

  protected readonly chartData = computed<
    ChartDataPoint[]
  >(() => {
    if (this.isChartRangeInvalid()) {
      return [];
    }

    const totalsByDate = new Map<string, number>();

    for (const timeEntry of this.timeEntries()) {
      if (timeEntry.durationMinutes === null) {
        continue;
      }

      const entryDate =
        timeEntry.startTime.slice(0, 10);

      if (
        entryDate < this.chartStartDate()
        || entryDate > this.chartEndDate()
      ) {
        continue;
      }

      const currentTotal =
        totalsByDate.get(entryDate) ?? 0;

      totalsByDate.set(
        entryDate,
        currentTotal + timeEntry.durationMinutes
      );
    }

    const dataPoints: ChartDataPoint[] = [];

    const currentDate = new Date(
      `${this.chartStartDate()}T00:00:00`
    );

    const endDate = new Date(
      `${this.chartEndDate()}T00:00:00`
    );

    while (currentDate <= endDate) {
      const date = toLocalDateString(currentDate);

      dataPoints.push({
        date,
        label: currentDate.toLocaleDateString(
          undefined,
          {
            day: '2-digit',
            month: 'short'
          }
        ),
        durationMinutes:
          totalsByDate.get(date) ?? 0
      });

      currentDate.setDate(
        currentDate.getDate() + 1
      );
    }

    return dataPoints;
  });

  protected readonly chartTotalMinutes =
    computed(() =>
      this.chartData().reduce(
        (total, dataPoint) =>
          total + dataPoint.durationMinutes,
        0
      )
    );

  protected readonly chartMaximumMinutes =
    computed(() =>
      Math.max(
        1,
        ...this.chartData().map(
          (dataPoint) =>
            dataPoint.durationMinutes
        )
      )
    );

  ngOnInit(): void {
    this.loadDashboardData();
  }

  protected exportChartCsv(): void {
  if (
    this.isChartRangeInvalid()
    || this.isLoading()
  ) {
    return;
  }

  const rows: Array<
    Array<string | number>
  > = [
    [
      'Date',
      'Duration Minutes'
    ],
    ...this.chartData().map((dataPoint) => [
      dataPoint.date,
      dataPoint.durationMinutes
    ]),
    [
      'Total',
      this.chartTotalMinutes()
    ]
  ];

  const fileName =
    `time-report_${this.chartStartDate()}`
    + `_to_${this.chartEndDate()}.csv`;

  downloadCsv(fileName, rows);
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

  protected chartBarHeight(
    durationMinutes: number
  ): number {
    if (durationMinutes === 0) {
      return 3;
    }

    return Math.max(
      8,
      durationMinutes
      / this.chartMaximumMinutes()
      * 100
    );
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

  private getDateDaysAgo(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() - days);

    return toLocalDateString(date);
  }

  private loadDashboardData(): void {
    const today = toLocalDateString(new Date());

    this.isLoading.set(true);

    forkJoin({
      overdueTasks:
        this.taskService.getOverdue(),

      timeEntries:
        this.timeEntryService.getAll(),

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

          this.timeEntries.set(
            response.timeEntries
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