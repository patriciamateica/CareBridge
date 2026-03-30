import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { ChartModule } from 'primeng/chart';
import { MessageService, SortEvent } from 'primeng/api';
import { PatientService } from '../../patient-crud/patient-service';
import { Patient, PatientStatus } from '../../patient-crud/patient-model';
import { CreatePatient } from '../../ui/create-patient/create-patient';
import { UpdatePatient } from '../../ui/update-patient/update-patient';
import { DeletePatient } from '../../ui/delete-patient/delete-patient';
import { AuthService } from '../../../../auth-service/auth.service';

const STATUS_PRIORITY: Record<string, number> = {
  'Critical': 1,
  'Active': 2,
  'Inactive': 3
};

@Component({
  selector: 'app-patient-management',
  standalone: true,
  imports: [
    DatePipe, FormsModule, TableModule, TagModule, InputTextModule,
    ChartModule,
    CreatePatient, UpdatePatient, DeletePatient
  ],
  templateUrl: './patient-management.html',
  styleUrl: './patient-management.css',
  providers: [MessageService]
})
export class PatientManagement {
  private readonly patientService = inject(PatientService);
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);

  readonly rowsPerPage = 10;
  searchQuery = signal('');
  viewMode = signal<'table' | 'charts'>('table');

  readonly filteredPatients = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const role = this.authService.currentRole();
    const nurseName = this.authService.currentUserName();

    let patients = [...this.patientService.patients()];

    if (role === 'nurse') {
      patients = patients.filter(p => p.assignedNurse === nurseName);
    }

    if (q) {
      patients = patients.filter(p =>
        `${p.firstName} ${p.lastName}`.toLowerCase().includes(q) ||
        p.diagnosis.toLowerCase().includes(q) ||
        p.assignedNurse.toLowerCase().includes(q)
      );
    }

    return patients.sort((a, b) => this.comparePatientsByStatus(a, b));
  });

  customSort(event: SortEvent) {
    if (!event.data) return;

    event.data.sort((data1, data2) => {
      let result: number;

      if (event.field === 'status') {
        result = this.comparePatientsByStatus(data1, data2);
      }
      else if (event.field === 'firstName' || event.field === 'lastName') {
        const name1 = `${data1.firstName} ${data1.lastName}`.toLowerCase();
        const name2 = `${data2.firstName} ${data2.lastName}`.toLowerCase();
        result = name1.localeCompare(name2);
      }
      else {
        const value1 = (data1[event.field!] || '').toString().toLowerCase();
        const value2 = (data2[event.field!] || '').toString().toLowerCase();
        result = value1.localeCompare(value2);
      }

      return (event.order || 1) * result;
    });
  }

  private comparePatientsByStatus(a: Patient, b: Patient): number {
    const p1 = STATUS_PRIORITY[a.status] || 99;
    const p2 = STATUS_PRIORITY[b.status] || 99;

    if (p1 !== p2) return p1 - p2;

    if (a.status === 'Critical') {
      const t1 = this.getLastVitalTime(a.id)?.getTime() ?? 0;
      const t2 = this.getLastVitalTime(b.id)?.getTime() ?? 0;
      return t1 - t2;
    }
    return 0;
  }

  readonly activeCount = computed(() =>
    this.filteredPatients().filter(p => p.status === 'Active').length
  );

  readonly criticalCount = computed(() =>
    this.filteredPatients().filter(p => p.status === 'Critical').length
  );

  readonly pendingTasksCount = computed(() =>
    this.filteredPatients().length
  );

  readonly donutData = computed(() => {
    const total = this.filteredPatients().length;
    const critical = this.criticalCount();
    const normal = total - critical;
    const normalPct = total ? Math.round((normal / total) * 100) : 0;
    const criticalPct = 100 - normalPct;

    return {
      labels: ['Normal', 'Critical'],
      datasets: [{
        data: [normal, critical],
        backgroundColor: ['#5f8d5f', '#b92b27'],
        hoverBackgroundColor: ['#4d7a4d', '#9e2420'],
        borderWidth: 0,
      }],
      normalPct,
      criticalPct,
    };
  });

  readonly donutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%',
    plugins: { legend: { display: false }, tooltip: { enabled: true } }
  };

  readonly symptomsData = computed(() => {
    const counts: Record<string, number> = {
      'Fatigue': 0, 'Shortness of Breath': 0,
      'Nausea': 0, 'Insomnia': 0, 'Constipation': 0,
    };

    this.filteredPatients().forEach(p => {
      const ci = this.patientService.getTodayCheckIn(p.id);
      if (ci) {
        ci.symptoms.forEach(s => {
          if (s in counts) counts[s]++;
        });
      }
    });

    return {
      labels: Object.keys(counts),
      datasets: [{
        data: Object.values(counts),
        backgroundColor: '#5f8d5f',
        hoverBackgroundColor: '#4d7a4d',
        borderRadius: 6,
        borderWidth: 0,
      }]
    };
  });

  readonly barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { stepSize: 1, color: '#6b7280' },
        grid: { color: 'rgba(0,0,0,0.06)' }
      },
      x: {
        ticks: { color: '#6b7280', font: { size: 11 } },
        grid: { display: false }
      }
    }
  };

  getSeverity(status: PatientStatus): 'success' | 'danger' | 'warn' {
    return ({ Active: 'success', Inactive: 'warn', Critical: 'danger' } as const)[status];
  }

  getLastVitalTime(id: string): Date | null {
    return this.patientService.getLastVitalTime(id);
  }

  openDetail(patient: Patient) {
    this.router.navigate(['/dashboard/patient-management', patient.id]);
  }

  triggerRefresh() {
    this.searchQuery.set(this.searchQuery());
  }
}
