import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { API_BASE_URL } from '../config/api.config';
import { CreateUserRequest } from '../models/create-user-request';
import { UpdateUserRequest } from '../models/update-user-request';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/users`;

  getAll() {
    return this.http.get<User[]>(this.apiUrl);
  }

  getById(id: number) {
    return this.http.get<User>(
      `${this.apiUrl}/${id}`
    );
  }

  create(request: CreateUserRequest) {
    return this.http.post<User>(
      this.apiUrl,
      request
    );
  }

  update(
    id: number,
    request: UpdateUserRequest
  ) {
    return this.http.patch<User>(
      `${this.apiUrl}/${id}`,
      request
    );
  }
}