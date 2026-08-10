import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { CreateProjectRequest } from '../models/create-project-request';
import { UpdateProjectRequest } from '../models/update-project-request';
import { Project } from '../models/project';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/projects`;

  getAll() {
    return this.http.get<Project[]>(this.apiUrl);
  }

  getById(id: number) {
  return this.http.get<Project>(
    `${this.apiUrl}/${id}`
  );
  }

  create(request: CreateProjectRequest) {
    return this.http.post<Project>(this.apiUrl, request);
  }

  update(id: number, request: UpdateProjectRequest) {
  return this.http.put<Project>(
    `${this.apiUrl}/${id}`,
    request
  );
}
}