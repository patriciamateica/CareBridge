import {Component, inject, signal} from '@angular/core';
import { CommonModule } from "@angular/common";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { ButtonModule } from "primeng/button";
import { CheckboxModule } from "primeng/checkbox";
import { InputTextModule } from "primeng/inputtext";
import { Password } from "primeng/password";
import { Message } from "primeng/message";
import {ToastService} from '../toast-service/toast-service';
import {AuthService} from '../../auth-service/auth.service';
import {CookiesService} from '../../cookies/cookieservice';

@Component({
  selector: 'app-login',
  templateUrl: './user-login.html',
  styleUrls: ['./user-login.css'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    CheckboxModule,
    InputTextModule,
    Password,
    Message,
  ]
})
export class UserLogin {
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cookiesService = inject(CookiesService);

  loginError = signal<string>('');
  isLoading  = signal(false);

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required]),
    rememberMe: new FormControl(false)
  });



  onSubmit() {
    this.loginError.set('');
    if (!this.loginForm.valid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const email = this.loginForm.value.email ?? '';
    const password = this.loginForm.value.password ?? '';

    this.authService.login({ email, password }).subscribe({
      next: (role) => {
        this.cookiesService.logActivity('login_success', email);
        const targetRoute = role === 'patient' ? '/dashboard/home-patient' : '/dashboard/home-nurse';
        this.toastService.showSuccess('Logged in successfully.');
        this.router.navigate([targetRoute]);
        this.isLoading.set(false);
      },
      error: (err) => {
        const message = err?.error ?? 'Login failed. Please check your credentials.';
        this.loginError.set(message);
        this.toastService.showError(message);
        this.cookiesService.logActivity('login_failed', email);
        this.isLoading.set(false);
      }
    });

  }

  onRegister(){
    this.router.navigate(['/user-registration']);
  }

  onForgotPassword(){
    this.router.navigate(['/forgot-password-flow']);
  }
}
