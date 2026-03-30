import { Component, inject, output, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Message } from 'primeng/message';
import { InputTextModule } from 'primeng/inputtext';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { PatientService } from '../../patient-crud/patient-service';
import { CreatePatientDto, PatientStatus } from '../../patient-crud/patient-model';
import {ToastService} from '../../../toast-service/toast-service';
import {CookiesService} from '../../../../cookies/cookieservice';

@Component({
  selector: 'app-create-patient',
  standalone: true,
  imports: [Message, InputTextModule, FormsModule, Button, Dialog, SelectModule],
  templateUrl: './create-patient.html',
})
export class CreatePatient {
  private readonly patientService = inject(PatientService);
  private readonly toastService = inject(ToastService);
  private readonly cookiesService = inject(CookiesService);


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

    this.cookiesService.logActivity(
      'patient_created',
      `${this.newPatient.firstName} ${this.newPatient.lastName}`
    );

    this.visible.set(false);
    this.refreshTable.emit();
    form.resetForm();
    this.newPatient = this.empty();
    this.toastService.showSuccess(`Patient ${this.newPatient.firstName} ${this.newPatient.lastName} added to roster.`);
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
