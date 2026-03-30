import { Component, inject, input, OnChanges, output, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { InputText } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { PatientService } from '../../patient-crud/patient-service';
import { Patient, PatientStatus, UpdatePatientDto } from '../../patient-crud/patient-model';
import {ToastService} from '../../../toast-service/toast-service';
import {CookiesService} from '../../../../cookies/cookieservice';

@Component({
  selector: 'app-update-patient',
  imports: [Dialog, FormsModule, Button, Message, InputText, SelectModule],
  templateUrl: './update-patient.html',
})
export class UpdatePatient implements OnChanges {
  private readonly patientService = inject(PatientService);
  private readonly toastService = inject(ToastService);
  private readonly cookiesService = inject(CookiesService);


  patientData = input.required<Patient>();
  refreshTable = output<void>();

  visible = signal(false);
  editPatient: UpdatePatientDto = {} as UpdatePatientDto;
  readonly statusOptions: PatientStatus[] = ['Active', 'Inactive', 'Critical'];

  ngOnChanges() { this.sync(); }

  openDialog() { this.sync(); this.visible.set(true); }

  onSubmit(form: NgForm) {
    if (!form.valid) return;
    this.patientService.update(this.patientData().id, this.editPatient);

    this.cookiesService.logActivity(
      'patient_updated',
      `${this.patientData().firstName} ${this.patientData().lastName}`
    );

    this.visible.set(false);
    this.refreshTable.emit();
    this.toastService.showSuccess('Patient updated successfully!');
  }

  private sync() {
    const p = this.patientData();
    this.editPatient = {
      ...p,
      firstName: p.firstName,
      lastName: p.lastName,
      diagnosis: p.diagnosis,
      status: p.status,
      assignedNurse: p.assignedNurse,
    };
  }
}
