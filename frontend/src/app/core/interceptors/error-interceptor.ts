import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification';

interface ApiErrorResponse {
  message?: string;
  validationErrors?: Record<string, string>;
}

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const notificationService = inject(NotificationService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = getErrorMessage(error);

      notificationService.error(message);

      return throwError(() => error);
    })
  );
};

function getErrorMessage(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'The server could not be reached. Please try again later.';
  }

  const response = error.error as ApiErrorResponse | null;

  if (response?.validationErrors) {
    const validationMessages = Object.values(
      response.validationErrors
    );

    if (validationMessages.length > 0) {
      return validationMessages.join(' ');
    }
  }

  if (response?.message) {
    return response.message;
  }

  switch (error.status) {
    case 400:
      return 'The submitted information is invalid.';
    case 401:
      return 'Authentication is required.';
    case 403:
      return 'You do not have permission to perform this operation.';
    case 404:
      return 'The requested resource could not be found.';
    case 409:
      return 'This record already exists.';
    default:
      return 'An unexpected error occurred.';
  }
}