import { Component, inject, input, signal } from '@angular/core';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { firstValueFrom } from 'rxjs';

import { Patient } from '../../../cruds/models/patient-ui-models';
import { ToastService } from '../../../toast-service/toast-service';
import { CookiesService } from '../../../../cookies/cookieservice';

import { UserService } from '../../../cruds/services/userService';
import { PatientDetailsService } from '../../../cruds/services/patientDetailsService';
import { NetworkService } from '../../../offline-support/network.service';
import { OfflineStorageService } from '../../../offline-support/offline-storage.service';
import { AuthService } from '../../../../auth-service/auth.service';

@Component({
  selector: 'app-delete-patient',
  standalone: true,
  imports: [Button, Dialog],
  templateUrl: './delete-patient.html',
})
export class DeletePatient {
  private readonly userSvc = inject(UserService);
  private readonly detailsSvc = inject(PatientDetailsService);
  private readonly networkSvc = inject(NetworkService);
  private readonly offlineStorage = inject(OfflineStorageService);

  private readonly toastService = inject(ToastService);
  private readonly cookiesService = inject(CookiesService);
  protected readonly authService = inject(AuthService);

  patientData = input.required<Patient>();
  disabled = input(false);
  visible = signal(false);

  openDialog() { this.visible.set(true); }

  async confirmDelete() {
    const p = this.patientData();

    try {
      if (this.networkSvc.isOnline) {
        const detail = await firstValueFrom(this.detailsSvc.getByUserId(p.id));
        if (detail) {
          await firstValueFrom(this.detailsSvc.delete(detail.id));
        }

        await firstValueFrom(this.userSvc.delete(p.id));
      } else {
        this.offlineStorage.queueOperation({
          type: 'PATIENT_DELETE',
          payload: { patientId: p.id }
        });
      }
    } catch (error: any) {
      const message = error?.message ?? error?.error?.message ?? 'Could not delete patient from backend.';
      this.toastService.showError(message);
      return;
    }

    this.cookiesService.logActivity(
      'patient_deleted',
      `${p.firstName} ${p.lastName}`
    );

    this.visible.set(false);
    this.toastService.showSuccess(`${p.firstName} ${p.lastName} was removed from the roster.`);
  }
}
