import { Component, computed, inject, output, signal, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Message } from 'primeng/message';
import { InputTextModule } from 'primeng/inputtext';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { firstValueFrom } from 'rxjs';

import { PatientStatus } from '../../../cruds/models/patient-ui-models';
import { ToastService } from '../../../toast-service/toast-service';
import { CookiesService } from '../../../../cookies/cookieservice';

import { UserService } from '../../../cruds/services/userService';
import { PatientDetailsService } from '../../../cruds/services/patientDetailsService';
import { NurseDetailsService } from '../../../cruds/services/nurseDetailsService';
import { NetworkService } from '../../../offline-support/network.service';
import { OfflineStorageService } from '../../../offline-support/offline-storage.service';

@Component({
  selector: 'app-create-patient',
  standalone: true,
  imports: [Message, InputTextModule, FormsModule, Button, Dialog, SelectModule],
  templateUrl: './create-patient.html',
})
export class CreatePatient implements OnInit {
  private readonly userSvc = inject(UserService);
  private readonly detailsSvc = inject(PatientDetailsService);
  private readonly nurseSvc = inject(NurseDetailsService);
  private readonly networkSvc = inject(NetworkService);
  private readonly offlineStorage = inject(OfflineStorageService);

  private readonly toastService = inject(ToastService);
  private readonly cookiesService = inject(CookiesService);

  visible = signal(false);

  private readonly _nurses = signal<any[]>([]);
  private readonly _patients = signal<any[]>([]);

  readonly statusOptions: PatientStatus[] = ['Active', 'Inactive', 'Critical'];

  readonly nurseOptions = computed(() => this._nurses().map((nurse) => ({
    label: `${nurse.firstName} ${nurse.lastName}`.trim(),
    value: nurse.id,
  })));

  readonly patientEmailOptions = computed(() =>
    this._patients().map((patient) => ({
      label: patient.email,
      value: patient.email,
    }))
  );

  newPatient: any = this.empty();

  ngOnInit() {
    this.loadNurses();
    this.loadPatients();
  }

  private loadNurses() {
    this.userSvc.getByRole('NURSE').subscribe((res: any) => {
      const nurses = res.content || [];
      this._nurses.set(nurses);
    });
  }

  private loadPatients() {
    this.userSvc.getByRole('PATIENT').subscribe((res: any) => {
      const patients = res.content || [];
      this._patients.set(patients);
    });
  }

  open() {
    this.newPatient = this.empty();
    this.visible.set(true);
  }

  async onSubmit(form: NgForm) {
    if (!form.valid) return;

    try {
      if (this.networkSvc.isOnline) {
        await firstValueFrom(this.detailsSvc.create({
          userEmail: this.newPatient.userEmail,
          primaryDiagnosis: this.newPatient.diagnosis,
          assignedNurseId: this.newPatient.assignedNurse || null,
          emergencyContact: 'N/A', // Set defaults for now, could be added to form later
          status: this.newPatient.status || 'Active'
        } as any));
      } else {
        this.offlineStorage.queueOperation({
          type: 'PATIENT_CREATE',
          payload: { dto: this.newPatient }
        });
      }
    } catch (error: any) {
      console.error('Create patient error:', error);
      const message = error?.error?.message || error?.message || 'Could not create patient in backend.';
      this.toastService.showError(message);
      return;
    }

    this.cookiesService.logActivity(
      'patient_created',
      this.newPatient.userEmail
    );

    this.visible.set(false);
    form.resetForm();
    this.newPatient = this.empty();
    this.toastService.showSuccess(`Patient profile linked successfully.`);
  }

  private empty(): any {
    return {
      userEmail: '',
      diagnosis: '',
      status: 'Active',
      assignedNurse: ''
    };
  }
}
