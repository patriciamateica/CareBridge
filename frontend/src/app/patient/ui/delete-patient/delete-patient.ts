import { Component, inject, input, output, signal } from '@angular/core';
import { Button } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { Dialog } from 'primeng/dialog';
import { PatientService } from '../../patient-crud/patient-service';
import { Patient } from '../../patient-crud/patient-model';

@Component({
  selector: 'app-delete-patient',
  imports: [Button, Dialog],
  templateUrl: './delete-patient.html',
})
export class DeletePatient {
  private readonly patientService = inject(PatientService);
  private readonly messageService = inject(MessageService);

  patientData = input.required<Patient>();
  refreshTable = output<void>();
  visible = signal(false);

  openDialog() { this.visible.set(true); }

  confirmDelete() {
    const p = this.patientData();
    this.patientService.delete(p.id);
    this.visible.set(false);
    this.refreshTable.emit();
    this.messageService.add({ severity: 'success', summary: 'Removed', detail: `${p.firstName} ${p.lastName}
    was removed from the roster.` });
  }
}
