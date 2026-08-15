import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import {
  CreateManualTimeEntryRequest
} from '../../core/models/create-manual-time-entry-request';
import {
  StartTimeEntryRequest
} from '../../core/models/start-time-entry-request';
import { Task } from '../../core/models/task';
import { TimeEntry } from '../../core/models/time-entry';

import {
  NotificationService
} from '../../core/services/notification';
import { TaskService } from '../../core/services/task';
import {
  TimeEntryService
} from '../../core/services/time-entry';

@Component({
  selector: 'app-time-entries',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './time-entries.html',
  styleUrl: './time-entries.scss'
})
export class TimeEntries implements OnInit, OnDestroy {

  private taskService = inject(TaskService);
  private timeEntryService = inject(TimeEntryService);
  private notificationService =
    inject(NotificationService);

  protected readonly tasks = signal<Task[]>([]);
  protected readonly timeEntries =
    signal<TimeEntry[]>([]);
  protected readonly activeTimeEntry =
    signal<TimeEntry | null>(null);

  protected readonly elapsedSeconds = signal(0);

  protected readonly isStarting = signal(false);
  protected readonly isStopping = signal(false);
  protected readonly isCreatingManual = signal(false);

  protected readonly trackableTasks = computed(() =>
    this.tasks().filter((task) =>
      task.status === 'TODO'
      || task.status === 'IN_PROGRESS'
    )
  );

  protected selectedTaskId: number | null = null;
  protected timerDescription = '';

  protected manualTaskId: number | null = null;
  protected manualStartTime = '';
  protected manualEndTime = '';
  protected manualDescription = '';

  private timerIntervalId: number | null = null;

  ngOnInit(): void {
    this.loadTasks();
    this.loadTimeEntries();
    this.loadActiveTimeEntry();
  }

  ngOnDestroy(): void {
    this.stopClock();
  }

  protected startTimer(): void {
    if (
      this.selectedTaskId === null
      || this.isStarting()
    ) {
      return;
    }

    const request: StartTimeEntryRequest = {
      taskId: this.selectedTaskId,
      description: this.timerDescription.trim()
    };

    this.isStarting.set(true);

    this.timeEntryService.start(request)
      .pipe(
        finalize(() => {
          this.isStarting.set(false);
        })
      )
      .subscribe({
        next: (timeEntry) => {
          this.activeTimeEntry.set(timeEntry);
          this.selectedTaskId = null;
          this.timerDescription = '';

          this.startClock();
          this.loadTimeEntries();

          this.notificationService.success(
            'Timer started successfully.'
          );
        },
        error: (err) => {
          console.error('Timer could not be started.', err);
        }
      });
  }

  protected stopTimer(): void {
    if (
      !this.activeTimeEntry()
      || this.isStopping()
    ) {
      return;
    }

    this.isStopping.set(true);

    this.timeEntryService.stop()
      .pipe(
        finalize(() => {
          this.isStopping.set(false);
        })
      )
      .subscribe({
        next: () => {
          this.activeTimeEntry.set(null);
          this.stopClock();
          this.elapsedSeconds.set(0);
          this.loadTimeEntries();

          this.notificationService.success(
            'Timer stopped successfully.'
          );
        },
        error: (err) => {
          console.error('Timer could not be stopped.', err);
        }
      });
  }

  protected createManualTimeEntry(): void {
    if (
      this.manualTaskId === null
      || !this.manualStartTime
      || !this.manualEndTime
      || this.isCreatingManual()
    ) {
      return;
    }

    const request: CreateManualTimeEntryRequest = {
      taskId: this.manualTaskId,
      startTime: this.manualStartTime,
      endTime: this.manualEndTime,
      description: this.manualDescription.trim()
    };

    this.isCreatingManual.set(true);

    this.timeEntryService.createManual(request)
      .pipe(
        finalize(() => {
          this.isCreatingManual.set(false);
        })
      )
      .subscribe({
        next: () => {
          this.manualTaskId = null;
          this.manualStartTime = '';
          this.manualEndTime = '';
          this.manualDescription = '';

          this.loadTimeEntries();

          this.notificationService.success(
            'Manual time entry created successfully.'
          );
        },
        error: (err) => {
          console.error(
            'Manual time entry could not be created.',
            err
          );
        }
      });
  }

  protected elapsedTime(): string {
    const totalSeconds = this.elapsedSeconds();

    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor(
      (totalSeconds % 3600) / 60
    );
    const seconds = totalSeconds % 60;

    return [
      hours,
      minutes,
      seconds
    ]
      .map((value) =>
        value.toString().padStart(2, '0')
      )
      .join(':');
  }

  protected formatDuration(
    durationMinutes: number | null
  ): string {
    if (durationMinutes === null) {
      return 'Running';
    }

    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    if (hours === 0) {
      return `${minutes} min`;
    }

    return `${hours} h ${minutes} min`;
  }

  protected formatDate(value: string | null): string {
    if (!value) {
      return '—';
    }

    return new Date(value).toLocaleString();
  }

  private loadTasks(): void {
    this.taskService.getAll().subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
      },
      error: (err) => {
        console.error('Tasks could not be loaded.', err);
      }
    });
  }

  private loadTimeEntries(): void {
    this.timeEntryService.getAll().subscribe({
      next: (timeEntries) => {
        this.timeEntries.set(timeEntries);
      },
      error: (err) => {
        console.error(
          'Time entries could not be loaded.',
          err
        );
      }
    });
  }

  private loadActiveTimeEntry(): void {
    this.timeEntryService.getActive().subscribe({
      next: (timeEntry) => {
        this.activeTimeEntry.set(timeEntry);

        if (timeEntry) {
          this.startClock();
          return;
        }

        this.stopClock();
        this.elapsedSeconds.set(0);
      },
      error: (err) => {
        console.error(
          'Active time entry could not be loaded.',
          err
        );
      }
    });
  }

  private startClock(): void {
    this.stopClock();
    this.updateElapsedTime();

    this.timerIntervalId = window.setInterval(
      () => this.updateElapsedTime(),
      1000
    );
  }

  private stopClock(): void {
    if (this.timerIntervalId === null) {
      return;
    }

    window.clearInterval(this.timerIntervalId);
    this.timerIntervalId = null;
  }

  private updateElapsedTime(): void {
    const timeEntry = this.activeTimeEntry();

    if (!timeEntry) {
      this.elapsedSeconds.set(0);
      return;
    }

    const startTime =
      new Date(timeEntry.startTime).getTime();

    const elapsedMilliseconds =
      Date.now() - startTime;

    this.elapsedSeconds.set(
      Math.max(
        0,
        Math.floor(elapsedMilliseconds / 1000)
      )
    );
  }
}