import {Component, inject} from '@angular/core';
import {CheckboxModule} from "primeng/checkbox";
import {CommonModule} from "@angular/common";
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from "@angular/forms";
import {ButtonModule} from "primeng/button";
import {InputTextModule} from "primeng/inputtext";
import {Password} from "primeng/password";
import {Message} from "primeng/message";
import {Router} from "@angular/router";
import {ToastService} from "../toast-service/toast-service";
import {AuthService, RegisterRequest} from '../../auth-service/auth.service';
import {CookiesService} from '../../cookies/cookieservice';

const passwordValidators = [
  Validators.required,
  Validators.minLength(8),
  Validators.pattern(/^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])/)
];

@Component({
  selector: 'app-registration',
  templateUrl: './user-registration.html',
  styleUrls: ['./user-registration.css'],
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CheckboxModule,
    InputTextModule,
    Password,
    ReactiveFormsModule,
    Message,
  ],
})

export class UserRegistration {
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly cookiesService = inject(CookiesService);
  private readonly router = inject(Router);

  passwordMatchValidator = (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return {passwordMismatch: true};
    }
    return null;
  }

  registerForm = new FormGroup({
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    phoneNumber: new FormControl('', [Validators.required]),
    password: new FormControl('', passwordValidators),
    confirmPassword: new FormControl('', passwordValidators),
  }, {
    validators: this.passwordMatchValidator
  });

  onSubmit() {
    if (!this.registerForm.valid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const requestPayload: RegisterRequest = {
      firstName: this.registerForm.value.firstName!,
      lastName: this.registerForm.value.lastName!,
      email: this.registerForm.value.email!,
      phoneNumber: this.registerForm.value.phoneNumber!,
      password: this.registerForm.value.password!
    };

    this.authService.registerNewUser(requestPayload).subscribe({
      next: () => {
        this.toastService.showSuccess('User registered successfully!');
        this.cookiesService.logActivity('registration_success', requestPayload.email);
        this.registerForm.reset();
        this.router.navigate(['/user-login']);
      },
      error: (err) => {
        const backendError = typeof err?.error === 'string' ? err.error : (err?.error?.message || err?.error || 'Could not register user.');
        this.toastService.showError(backendError);
        this.cookiesService.logActivity('registration_failed', requestPayload.email);
      }
    });
  }

  onSignIn(){
    this.router.navigate(['/user-login']);
  }
}
