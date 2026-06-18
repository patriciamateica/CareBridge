import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';

import { AuthService } from '../../auth-service/auth.service';
import { ClinicalLogService } from '../cruds/services/clinicalLogService';
import { UserService } from '../cruds/services/userService';
import { ClinicalLog } from '../cruds/models/clinicalLog';
import { User } from '../cruds/models/user';

@Component({
  selector: 'app-medical-records',
  standalone: true,
  imports: [DatePipe, NgClass, FormsModule, SelectModule],
  templateUrl: './medical-records.html',
  styleUrl: './medical-records.css',
})
export class MedicalRecordsComponent implements OnInit {
  private readonly clinicalLogSvc = inject(ClinicalLogService);
  private readonly userSvc = inject(UserService);
  private readonly authService = inject(AuthService);

  private readonly _logs = signal<ClinicalLog[]>([]);
  private readonly _nurses = signal<User[]>([]);
  private readonly _patients = signal<User[]>([]);

  searchQuery = signal('');
  activeFilter = signal<string>('ALL');
  readonly selectedPatientId = signal<string>('');

  readonly documentTypes = ['ALL', 'SCAN', 'REPORT', 'PRESCRIPTION', 'LAB_RESULT', 'REFERRAL', 'OTHER'];

  readonly isNurseMode = computed(() => ['Nurse', 'Admin'].includes(this.authService.currentRole()));
  readonly patientOptions = computed(() =>
    this._patients().map(p => ({ label: `${p.firstName} ${p.lastName}`, value: p.id }))
  );

  ngOnInit() {
    this.userSvc.getByRole('NURSE').subscribe({
      next: (res: any) => this._nurses.set(res.content || []),
      error: () => {}
    });
    if (this.isNurseMode()) {
      this.userSvc.getByRole('PATIENT').subscribe({
        next: (res: any) => this._patients.set(res.content || []),
        error: () => {}
      });
    } else {
      const userId = this.authService.currentUserId();
      this.loadLogsFor(userId);
    }
  }

  private loadLogsFor(patientId: string): void {
    this.clinicalLogSvc.getByPatientId(patientId).subscribe({
      next: (logs) => this._logs.set(logs || []),
      error: () => {}
    });
  }

  onPatientSelect(patientId: string): void {
    this.selectedPatientId.set(patientId);
    this._logs.set([]);
    this.loadLogsFor(patientId);
  }

  readonly filteredLogs = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const filter = this.activeFilter();
    return this._logs()
      .filter(l => filter === 'ALL' || l.documentType === filter)
      .filter(l => !q || (l.documentTitle || '').toLowerCase().includes(q) || (l.documentType || '').toLowerCase().includes(q))
      .sort((a, b) => new Date(b.datePerformed).getTime() - new Date(a.datePerformed).getTime());
  });

  getNurseName(nurseId: string): string {
    const u = this._nurses().find(x => x.id === nurseId);
    return u ? `${u.firstName} ${u.lastName}` : 'Unknown';
  }

  getDocTypeLabel(type: string): string {
    return type?.replace(/_/g, ' ') || 'Document';
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'ACTIVE': return 'status-active';
      case 'ARCHIVED': return 'status-archived';
      case 'DELETED': return 'status-deleted';
      default: return '';
    }
  }

  getDocTypeIcon(type: string): string {
    switch (type?.toUpperCase()) {
      case 'SCAN': return 'pi-image';
      case 'REPORT': return 'pi-file-pdf';
      case 'LAB_RESULT': return 'pi-chart-bar';
      case 'REFERRAL': return 'pi-send';
      case 'PRESCRIPTION': return 'pi-pills';
      default: return 'pi-file';
    }
  }
}
