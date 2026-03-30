import { Component, inject, input, output, signal } from '@angular/core';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { PatientService } from '../../patient-crud/patient-service';
import { Patient } from '../../patient-crud/patient-model';
import {ToastService} from '../../../toast-service/toast-service';
import {CookiesService} from '../../../../cookies/cookieservice';

@Component({
  selector: 'app-delete-patient',
  imports: [Button, Dialog],
  templateUrl: './delete-patient.html',
})
export class DeletePatient {
  private readonly patientService = inject(PatientService);
  private readonly toastService = inject(ToastService);
  private readonly cookiesService = inject(CookiesService);


  patientData = input.required<Patient>();
  refreshTable = output<void>();
  visible = signal(false);

  openDialog() { this.visible.set(true); }

  confirmDelete() {
    const p = this.patientData();
    this.patientService.delete(p.id);

    this.cookiesService.logActivity(
      'patient_deleted',
      `${p.firstName} ${p.lastName}`
    );

    this.visible.set(false);
    this.refreshTable.emit();
    this.toastService.showSuccess(`${p.firstName} ${p.lastName} was removed from the roster.`);
  }
}
