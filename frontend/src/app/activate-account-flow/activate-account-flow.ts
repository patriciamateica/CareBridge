import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { AuthService } from '../../auth-service/auth.service';
import { ToastService } from '../toast-service/toast-service';
import {Message} from 'primeng/message';
import {CookiesService} from '../../cookies/cookieservice';

@Component({
  selector: 'app-activation',
  templateUrl: './activate-account-flow.html',
  styleUrls: ['./activate-account-flow.css'],
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule, Message],
})
export class ActivateAccountFlow {
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  private readonly cookiesService = inject(CookiesService);


  activationForm = new FormGroup({
    activationId: new FormControl('', [Validators.required]),
  });

  onSubmit() {
    // if (!this.activationForm.valid) {
    //   this.activationForm.markAllAsTouched();
    //   return;
    // }
    //
    // const email = localStorage.getItem('pending_activation_email') ?? '';
    // const activationId = this.activationForm.value.activationId!;
    //
    // this.authService.activateUser(email, activationId).subscribe({
    //   next: (role) => {
    //     this.toastService.showSuccess(`Account activated successfully!`);
    //     localStorage.removeItem('pending_activation_email');
    //     setTimeout(() => {
    //       this.router.navigate([ROLE_ROUTES[role]]);
    //     }, 1500);
    //   },
    //   error: (err) => {
    //     this.toastService.showError(err?.error ?? 'Invalid activation ID');
    //   }
    // });
    this.router.navigate(['/dashboard/home-nurse']);
    this.cookiesService.logActivity('activation_attempted', this.activationForm.value.activationId ?? '');
  }
}
