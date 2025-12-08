import { HttpRequest, HttpHandlerFn, HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { Auth } from '../services/auth';

export const RefreshInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const authService = inject(Auth);
  let isRefreshing = false;

  return next(req).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401 && !isRefreshing) {
          isRefreshing = true;
          return authService.refreshToken().pipe(
              switchMap(() => {
                isRefreshing = false;
                return next(req); // retry request originale
              }),
              catchError(refreshErr => {
                isRefreshing = false;
                return throwError(() => refreshErr);
              })
          );
        }
        return throwError(() => err);
      })
  );
};
