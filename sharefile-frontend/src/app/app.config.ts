import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {provideHttpClient, withFetch, withInterceptors, withInterceptorsFromDi} from "@angular/common/http";
import {RefreshInterceptor} from "./core/interceptors/refresh-interceptor";
import {CsrfInterceptor} from "./core/interceptors/csrf-interceptor";

// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
        withInterceptors([CsrfInterceptor, RefreshInterceptor]),
        withInterceptorsFromDi(), // se usi interceptor class-based
        withFetch() // opzionale, usa Fetch API invece di XMLHttpRequest
    )
  ]
};
