import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Task } from '../models/task';

import { CreateTaskRequest } from '../models/create-task-request';
import { UpdateTaskRequest } from '../models/update-task-request';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/tasks`;

  getAll() {
    return this.http.get<Task[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<Task>(
      `${this.apiUrl}/${id}`
    );
  }

  create(request: CreateTaskRequest) {
    return this.http.post<Task>(
      this.apiUrl,
      request
    );
  }  

  update(id: number, request: UpdateTaskRequest) {
    return this.http.put<Task>(
      `${this.apiUrl}/${id}`,
      request
    );
  }
}