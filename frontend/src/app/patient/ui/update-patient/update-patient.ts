import { Component, computed, inject, input, OnChanges, signal, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { InputText } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { Patient, PatientStatus, UpdatePatientDto } from '../../../cruds/models/patient-ui-models';
import { ToastService } from '../../../toast-service/toast-service';
import { CookiesService } from '../../../../cookies/cookieservice';

import { UserService } from '../../../cruds/services/userService';
import { PatientDetailsService } from '../../../cruds/services/patientDetailsService';
import { NurseDetailsService } from '../../../cruds/services/nurseDetailsService';
import { NetworkService } from '../../../offline-support/network.service';
import { OfflineStorageService } from '../../../offline-support/offline-storage.service';

@Component({
  selector: 'app-update-patient',
  standalone: true,
  imports: [Dialog, FormsModule, Button, Message, InputText, SelectModule],
  templateUrl: './update-patient.html',
})
export class UpdatePatient implements OnChanges, OnInit {
  private readonly userSvc = inject(UserService);
  private readonly detailsSvc = inject(PatientDetailsService);
  private readonly nurseSvc = inject(NurseDetailsService);
  private readonly networkSvc = inject(NetworkService);
  private readonly offlineStorage = inject(OfflineStorageService);
  private readonly http = inject(HttpClient);

  private readonly toastService = inject(ToastService);
  private readonly cookiesService = inject(CookiesService);

  patientData = input.required<Patient>();

  visible = signal(false);
  editPatient: UpdatePatientDto = {} as UpdatePatientDto;

  private readonly _nurses = signal<any[]>([]);

  readonly statusOptions: PatientStatus[] = ['Active', 'Inactive', 'Critical'];
  readonly nurseOptions = computed(() =>
    this._nurses().map((nurse) => ({
    label: `${nurse.firstName} ${nurse.lastName}`.trim(),
    value: nurse.id,
  })));

  ngOnInit() {
    this.loadNurses();
  }

  ngOnChanges() { this.sync(); }

  private loadNurses() {
    this.userSvc.getAll().subscribe((res: any) => {
      const users = res.content || [];
      const nurses = users.filter((u: any) => u.roles?.includes('NURSE'));
      this._nurses.set(nurses);
    });
  }

  openDialog() { this.sync(); this.visible.set(true); }

  async onSubmit(form: NgForm) {
    if (!form.valid) return;
    const name = `${this.patientData().firstName} ${this.patientData().lastName}`.trim();

    try {
      if (this.networkSvc.isOnline) {
        const userPayload: any = {
          firstName: this.editPatient.firstName,
          lastName: this.editPatient.lastName,
        };
        if (this.editPatient.phone) userPayload.phoneNumber = this.editPatient.phone;
        if (this.editPatient.address) userPayload.residentialAddress = this.editPatient.address;
        if (this.editPatient.dateOfBirth) {
          userPayload.dateOfBirth = this.editPatient.dateOfBirth instanceof Date
            ? this.editPatient.dateOfBirth.toISOString().split('T')[0]
            : this.editPatient.dateOfBirth;
        }
        userPayload.userStatus = this.editPatient.status === 'Inactive' ? 'INACTIVE' : 'ACTIVE';

        await firstValueFrom(this.userSvc.update(this.patientData().id, userPayload));

        const detailsRes: any = await firstValueFrom(
          this.http.get('http://localhost:8080/api/patient-details?size=200')
        );
        const detail = detailsRes.content?.find((d: any) => d.userId === this.patientData().id);

        if (detail) {
          await firstValueFrom(this.detailsSvc.update(detail.id, {
            id: detail.id,
            userId: this.patientData().id,
            primaryDiagnosis: this.editPatient.diagnosis,
            assignedNurseId: this.editPatient.assignedNurse || null,
            assignedNurseName: null,
            status: this.editPatient.status,
            emergencyContact: this.editPatient.phone || detail.emergencyContact || 'N/A',
            diagnostics: detail.diagnostics || [],
            scans: detail.scans || []
          } as any));
        }
      } else {
        this.offlineStorage.queueOperation({
          type: 'PATIENT_UPDATE',
          payload: { patientId: this.patientData().id, dto: this.editPatient }
        });
      }
    } catch (error: any) {
      const message = error?.message ?? error?.error?.message ?? 'Could not update patient in backend.';
      this.toastService.showError(message);
      return;
    }

    this.cookiesService.logActivity(
      'patient_updated',
      name
    );

    this.visible.set(false);
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
      assignedNurse: p.assignedNurseId || '',
    };
  }
}
