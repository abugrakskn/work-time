import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Task } from '../models/task';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/tasks';

  getAll() {
    return this.http.get<Task[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<Task>(
      `${this.apiUrl}/${id}`
    );
  }
}