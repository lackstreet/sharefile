import { HttpInterceptorFn } from '@angular/common/http';
import {Auth} from "../services/auth";
import {inject} from "@angular/core";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(Auth);
  const token = authService.getToken();

  if(token){
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next(req);
};
