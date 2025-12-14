import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {provideHttpClient, withFetch, withInterceptors, withInterceptorsFromDi} from "@angular/common/http";
import {Configuration, ConfigurationParameters} from "./generated/api";
import {environment} from "../environments/environment";
import {authInterceptor} from "./core/interceptors/auth-interceptor";

export function apiConfigFactory(): Configuration{
  const params: ConfigurationParameters = {
    basePath: environment.apiUrl,
    withCredentials: true,
  };
  return new Configuration(params);
}

// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
        withInterceptors([authInterceptor]),
        withInterceptorsFromDi(), // se usi interceptor class-based
        withFetch() // opzionale, usa Fetch API invece di XMLHttpRequest
    ),
    {
      provide: Configuration,
      useFactory: apiConfigFactory
    }
  ]
};
