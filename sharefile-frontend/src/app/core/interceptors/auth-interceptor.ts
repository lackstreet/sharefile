import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {AuthService} from "../services/auth-service";
import {inject} from "@angular/core";
import {catchError} from "rxjs/operators";
import {throwError} from "rxjs";

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  return next(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if(error.status === 401){
          console.warn('session expired, redirecting to login');
          authService.login();
        }
        return throwError(() => error);
      })
  );
};
