import { Component, inject } from '@angular/core';
import { CommonModule } from "@angular/common";
import { AbstractControl, FormControl, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from "@angular/forms";
import { Router } from "@angular/router";

import { ButtonModule } from "primeng/button";
import { PasswordModule } from "primeng/password";
import { MessageModule } from "primeng/message";

// import { ToastService } from "../toast-service/toast-service";
// import { AuthService } from '../../auth-service/auth.service';

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
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    PasswordModule,
    MessageModule
  ]
})
export class ForgotPassword {
  // toastService = inject(ToastService);
  // authService = inject(AuthService);
  router = inject(Router);

  passwordMatchValidator = (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  forgotPasswordForm = new FormGroup({
    password: new FormControl('', passwordValidators),
    confirmPassword: new FormControl('', passwordValidators),
  }, {
    validators: this.passwordMatchValidator
  });

  onSubmit() {
    // if (!this.forgotPasswordForm.valid) {
    //   this.forgotPasswordForm.markAllAsTouched();
    // } else {}
    //   console.log('New Password Data:', this.forgotPasswordForm.value);
      this.router.navigate(['/user-login']);
  }
}
