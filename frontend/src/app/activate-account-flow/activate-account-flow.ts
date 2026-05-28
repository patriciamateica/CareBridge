import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../toast-service/toast-service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-activation',
  templateUrl: './activate-account-flow.html',
  styleUrls: ['./activate-account-flow.css'],
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule, Message],
})
export class ActivateAccountFlow {
  private readonly router      = inject(Router);
  private readonly route       = inject(ActivatedRoute);
  private readonly http        = inject(HttpClient);
  private readonly toastService = inject(ToastService);

  isLoading    = signal(false);
  errorMessage = signal('');
  email        = signal('');

  activationForm = new FormGroup({
    email: new FormControl('', [Validators.email]),
    activationCode: new FormControl('', [Validators.required]),
  });


  constructor() {
    this.route.queryParamMap.subscribe(params => {
      const email = params.get('email');
      if (email) this.email.set(email);
    });
  }

  onSubmit() {
    if (this.activationForm.invalid) {
      this.activationForm.markAllAsTouched();
      return;
    }
    const emailToUse = this.email() || this.activationForm.value.email || '';

    this.isLoading.set(true);
    this.errorMessage.set('');

     this.http.post(`${environment.apiBaseUrl}/api/auth/activate`, {
      email: this.email(),
      activationNumber: this.activationForm.value.activationCode
    }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toastService.showSuccess('Account activated! You can now sign in.');
        this.router.navigate(['/user-login']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err?.error?.error ?? 'Invalid activation code. Please try again.');
      }
    });
  }
}
