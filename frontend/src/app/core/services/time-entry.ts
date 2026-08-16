import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { HttpParams } from '@angular/common/http';

import { API_BASE_URL } from '../config/api.config';
import {
  CreateManualTimeEntryRequest
} from '../models/create-manual-time-entry-request';
import {
  StartTimeEntryRequest
} from '../models/start-time-entry-request';
import { TimeEntry } from '../models/time-entry';
import { TimeSummary } from '../models/time-summary';

@Injectable({
  providedIn: 'root'
})
export class TimeEntryService {

  private http = inject(HttpClient);

  private readonly apiUrl =
    `${API_BASE_URL}/time-entries`;

  getAll() {
    return this.http.get<TimeEntry[]>(
      this.apiUrl
    );
  }

  getActive() {
    return this.http.get<TimeEntry | null>(
      `${this.apiUrl}/active`
    );
  }

  start(request: StartTimeEntryRequest) {
    return this.http.post<TimeEntry>(
      `${this.apiUrl}/start`,
      request
    );
  }

  stop() {
    return this.http.post<TimeEntry>(
      `${this.apiUrl}/stop`,
      null
    );
  }

  createManual(
    request: CreateManualTimeEntryRequest
  ) {
    return this.http.post<TimeEntry>(
      `${this.apiUrl}/manual`,
      request
    );
  }

  getDailySummary(date: string) {
  const params = new HttpParams()
    .set('date', date);

  return this.http.get<TimeSummary>(
    `${this.apiUrl}/summary/daily`,
    { params }
  );
}

getWeeklySummary(date: string) {
  const params = new HttpParams()
    .set('date', date);

  return this.http.get<TimeSummary>(
    `${this.apiUrl}/summary/weekly`,
    { params }
  );
}
}