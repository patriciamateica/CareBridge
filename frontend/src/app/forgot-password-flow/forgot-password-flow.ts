import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { Message } from 'primeng/message';
import { AuthService } from '../../auth-service/auth.service';
import { ToastService } from '../toast-service/toast-service';

const passwordValidators = [
  Validators.required,
  Validators.minLength(8),
  Validators.pattern(/^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*])/)
];

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password-flow.html',
  styleUrls: ['./forgot-password-flow.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    PasswordModule,
    MessageModule,
    Message
  ]
})
export class ForgotPassword {
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  step = signal<'email' | 'reset'>('email');
  isLoading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  emailForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  passwordMatchValidator = (control: AbstractControl): ValidationErrors | null => {
    const pw = control.get('password');
    const cpw = control.get('confirmPassword');
    if (pw && cpw && pw.value !== cpw.value) return { passwordMismatch: true };
    return null;
  };

  resetForm = new FormGroup({
    token: new FormControl('', [Validators.required]),
    password: new FormControl('', passwordValidators),
    confirmPassword: new FormControl('', passwordValidators)
  }, { validators: this.passwordMatchValidator });

  constructor() {
    this.route.queryParamMap.subscribe(params => {
      const token = params.get('token');
      if (token) {
        this.resetForm.patchValue({ token });
        this.step.set('reset');
      }
    });
  }

  onSubmitEmail() {
    this.errorMessage.set('');
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const email = this.emailForm.value.email!;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set(
          'If that email is registered, you will receive a reset link shortly.'
        );
        this.step.set('reset');
      },
      error: () => {
        this.isLoading.set(false);
        this.step.set('reset');
      }
    });
  }

  onSubmitReset() {
    this.errorMessage.set('');
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const { token, password } = this.resetForm.value;

    this.authService.resetPassword(token!, password!).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toastService.showSuccess('Password updated successfully. Please sign in.');
        this.router.navigate(['/user-login']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err?.error ?? 'Reset failed. The link may have expired.');
      }
    });
  }

  onBackToLogin() {
    this.router.navigate(['/user-login']);
  }
}
