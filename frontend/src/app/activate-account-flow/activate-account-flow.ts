import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
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
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  private readonly cookiesService = inject(CookiesService);


  activationForm = new FormGroup({
    activationId: new FormControl('', [Validators.required]),
  });

  onSubmit() {
    if (!this.activationForm.valid) {
      this.activationForm.markAllAsTouched();
      return;
    }

    this.toastService.showSuccess('Activation step acknowledged. Please sign in.');
    this.router.navigate(['/user-login']);
    this.cookiesService.logActivity('activation_attempted', this.activationForm.value.activationId ?? '');
  }
}
