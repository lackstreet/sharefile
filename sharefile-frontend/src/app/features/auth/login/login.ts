import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Auth} from "../../../core/services/auth";
import {Router} from "@angular/router";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "../../../module/material/material-module";

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule,MaterialModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
      private formBuilder: FormBuilder,
      private authService: Auth,
      private router: Router
  ){
    this.loginForm = this.formBuilder.group({
      username: ['',[Validators.required, Validators.email]],
      password: ['',[Validators.required, Validators.minLength(4)]]
    });
  }

  onSubmit(){
    if(this.loginForm.invalid){
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const { username, password} = this.loginForm.value;

    this.authService.login(username, password).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage = 'Invalid email or password';
        this.isLoading = false;
      }
    });
  }

}
