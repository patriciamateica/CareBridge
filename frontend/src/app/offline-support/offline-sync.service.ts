import { Injectable, inject, OnInit } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { NetworkService } from './network.service';
import { OfflineStorageService } from './offline-storage.service';

import { UserService } from '../cruds/services/userService';
import { PatientDetailsService } from '../cruds/services/patientDetailsService';
import { HealthStatusService } from '../cruds/services/healthStatusService';
import { CareNotesService } from '../cruds/services/careNotesService';
import { PrescriptionService } from '../cruds/services/prescriptionService';

@Injectable({
  providedIn: 'root'
})
export class OfflineSyncService implements OnInit {
  private readonly networkSvc = inject(NetworkService);
  private readonly offlineStorage = inject(OfflineStorageService);

  private readonly userSvc = inject(UserService);
  private readonly detailsSvc = inject(PatientDetailsService);
  private readonly healthSvc = inject(HealthStatusService);
  private readonly careNotesSvc = inject(CareNotesService);
  private readonly prescriptionSvc = inject(PrescriptionService);

  ngOnInit() {
    this.networkSvc.connectionRestored$.subscribe(() => {
      console.log('[OfflineSyncService] Auto-syncing after connection restored');
      this.syncNow();
    });
  }

  purgeStaleOperations(): void {
    const queue = this.offlineStorage.getQueue();
    const MAX_RETRIES = 10;
    const stale = queue.filter(op => {
      const patientId = op.payload['patientId'] as string;
      const isTmpId = typeof patientId === 'string' && patientId.startsWith('tmp-');
      const tooManyRetries = op.retryCount >= MAX_RETRIES;
      return isTmpId || tooManyRetries;
    });
    stale.forEach(op => {
      console.warn('[OfflineSyncService] Purging stale/unresolvable operation:', op.id, op.type);
      this.offlineStorage.removeFromQueue(op.id);
    });
  }

  async syncNow(): Promise<void> {
    this.purgeStaleOperations();
    const queue = this.offlineStorage.getQueue();
    if (queue.length === 0 || !this.networkSvc.isOnline) {
      return;
    }

    console.log(`[OfflineSyncService] Starting sync of ${queue.length} operations...`);

    for (const op of queue) {
      try {
        const payload = op.payload as any;
        switch (op.type) {
          case 'PATIENT_CREATE':
            await this.processPatientCreate(payload['dto']);
            break;
          case 'PATIENT_UPDATE':
            await this.processPatientUpdate(payload['patientId'], payload['dto']);
            break;
          case 'CHECKIN_UPSERT':
            const healthRes = await firstValueFrom(this.healthSvc.create(payload as any));
            console.log('[OfflineSyncService] Check-in synced:', healthRes);
            break;
          case 'CARE_NOTE_ADD':
            const noteRes = await firstValueFrom(this.careNotesSvc.create(payload as any));
            console.log('[OfflineSyncService] Care note synced:', noteRes);
            break;
          case 'CARE_NOTE_UPDATE':
            await firstValueFrom(this.careNotesSvc.update(payload['noteId'], payload as any));
            console.log('[OfflineSyncService] Care note updated');
            break;
          case 'CARE_NOTE_DELETE':
            await firstValueFrom(this.careNotesSvc.delete(payload['noteId']));
            console.log('[OfflineSyncService] Care note deleted');
            break;
          case 'MEDICATION_ADD':
            const medRes = await firstValueFrom(this.prescriptionSvc.create(payload as any));
            console.log('[OfflineSyncService] Medication synced:', medRes);
            break;
          case 'MEDICATION_DELETE':
            await firstValueFrom(this.prescriptionSvc.delete(payload['medId']));
            console.log('[OfflineSyncService] Medication deleted');
            break;
          default:
            console.warn('[OfflineSyncService] Unknown operation type:', op.type);
        }
        this.offlineStorage.removeFromQueue(op.id);
      } catch (err) {
        console.error('[OfflineSyncService] Sync failed for operation:', op, err);
        this.offlineStorage.markOperationFailed(op.id, err);
        break;
      }
    }
  }

  private async processPatientCreate(dto: any) {
    const email = `${dto.firstName.toLowerCase()}.${dto.lastName.toLowerCase()}.${Date.now()}@carebridge.com`;
    console.log('[OfflineSyncService] Sync: Registering patient...', { firstName: dto.firstName, lastName: dto.lastName, email });

    await firstValueFrom(this.userSvc.registerPatient({
      userRequest: {
        firstName: dto.firstName,
        lastName: dto.lastName,
        email: email,
        password: 'DefaultPassword123!',
        phoneNumber: dto.phone || '0'
      },
      primaryDiagnosis: dto.diagnosis || 'General Checkup',
      assignedNurseId: dto.assignedNurse || null,
      emergencyContact: dto.phone || 'N/A',
      status: dto.status || 'Active'
    }));

    console.log('[OfflineSyncService] Sync: Patient created successfully.');
  }

  private async processPatientUpdate(id: string, dto: any) {
    if (id.startsWith('tmp-')) {
      console.warn('[OfflineSyncService] Skipping update for temporary ID:', id);
      return;
    }
    await firstValueFrom(this.userSvc.update(id, {
        firstName: dto.firstName,
        lastName: dto.lastName,
        phoneNumber: dto.phone || '0',
        residentialAddress: dto.address,
        role: 'PATIENT',
        userStatus: dto.status === 'Inactive' ? 'INACTIVE' : 'ACTIVE'
    } as any));

    const detailsRes: any = await firstValueFrom(this.detailsSvc.getAll());
    const detail = detailsRes.content?.find((d: any) => d.userId === id);
    if (detail) {
      await firstValueFrom(this.detailsSvc.update(detail.id, {
        id: detail.id,
        userId: id,
        primaryDiagnosis: dto.diagnosis,
        assignedNurseId: dto.assignedNurse,
        assignedNurseName: null,
        status: dto.status,
        emergencyContact: dto.phone || 'N/A',
        diagnostics: detail.diagnostics || [],
        scans: detail.scans || []
      } as any));
    }
  }
}
