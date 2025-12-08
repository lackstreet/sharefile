import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Auth} from "./core/services/auth";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('sharefile-frontend');

  constructor(private auth: Auth) {}

  ngOnInit() {
    this.auth.initCsrf().subscribe(() => {
    });
  }
}
