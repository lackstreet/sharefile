import { Injectable } from '@angular/core';
import { environment } from "../../../environments/environment.development";
import { HttpClient, HttpResponse } from "@angular/common/http";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class Api {
  private apiUrl = environment.apiUrl;

  constructor() {}
  getEndpointUrl(endpoint: string): string {
    return `${this.apiUrl}/${endpoint}`;
  }
}