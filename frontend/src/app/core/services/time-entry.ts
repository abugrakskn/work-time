import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { API_BASE_URL } from '../config/api.config';
import {
  CreateManualTimeEntryRequest
} from '../models/create-manual-time-entry-request';
import {
  StartTimeEntryRequest
} from '../models/start-time-entry-request';
import { TimeEntry } from '../models/time-entry';

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
}