import { Component, inject, output, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Message } from 'primeng/message';
import { InputTextModule } from 'primeng/inputtext';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { SelectModule } from 'primeng/select';
import { PatientService } from '../../patient-crud/patient-service';
import { CreatePatientDto, PatientStatus } from '../../patient-crud/patient-model';

@Component({
  selector: 'app-create-patient',
  standalone: true,
  imports: [Message, InputTextModule, FormsModule, Button, Dialog, SelectModule],
  templateUrl: './create-patient.html',
})
export class CreatePatient {
  private readonly patientService = inject(PatientService);
  private readonly messageService = inject(MessageService);

  visible = signal(false);
  refreshTable = output<void>();

  readonly statusOptions: PatientStatus[] = ['Active', 'Inactive', 'Critical'];
  newPatient: CreatePatientDto = this.empty();

  open() {
    this.newPatient = this.empty();
    this.visible.set(true);
  }

  onSubmit(form: NgForm) {
    if (!form.valid) return;
    this.patientService.create(this.newPatient);
    this.visible.set(false);
    this.refreshTable.emit();
    form.resetForm();
    this.newPatient = this.empty();
    this.messageService.add({
      severity: 'success',
      summary: 'Patient Added',
      detail: `${this.newPatient.firstName} ${this.newPatient.lastName} added to roster.`
    });
  }

  private empty(): CreatePatientDto {
    return {
      firstName: '',
      lastName: '',
      dateOfBirth: null as any,
      diagnosis: '',
      neurologicalStatus: '',
      associatedConditions: '',
      status: 'Active',
      phone: '',
      address: '',
      assignedNurse: '',
      notes: ''
    };
  }
}
