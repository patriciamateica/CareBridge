import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { Toast } from 'primeng/toast';
import { ChartModule } from 'primeng/chart';
import { MessageService } from 'primeng/api';
import { PatientService } from '../../patient-crud/patient-service';
import { Patient, PatientStatus } from '../../patient-crud/patient-model';
import { CreatePatient } from '../../ui/create-patient/create-patient';
import { UpdatePatient } from '../../ui/update-patient/update-patient';
import { DeletePatient } from '../../ui/delete-patient/delete-patient';

@Component({
  selector: 'app-patient-management',
  standalone: true,
  imports: [
    DatePipe, FormsModule, TableModule, TagModule,
    IconField, InputIcon, InputTextModule,
    Toast, ChartModule,
    CreatePatient, UpdatePatient, DeletePatient
  ],
  templateUrl: './patient-management.html',
  styleUrl: './patient-management.css',
  providers: [MessageService]
})
export class PatientManagement {
  private readonly patientService = inject(PatientService);
  private readonly router = inject(Router);

  readonly rowsPerPage = 10;
  searchQuery = signal('');
  viewMode = signal<'table' | 'charts'>('table');

  readonly filteredPatients = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    if (!q) return this.patientService.patients();
    return this.patientService.patients().filter(p =>
      `${p.firstName} ${p.lastName}`.toLowerCase().includes(q) ||
      p.diagnosis.toLowerCase().includes(q) ||
      p.assignedNurse.toLowerCase().includes(q)
    );
  });

  readonly activeCount = computed(() =>
    this.patientService.patients().filter(p => p.status === 'Active').length
  );

  readonly criticalCount = computed(() =>
    this.patientService.patients().filter(p => p.status === 'Critical').length
  );

  readonly pendingTasksCount = computed(() =>
    this.patientService.patients().length
  );

  readonly donutData = computed(() => {
    const total = this.patientService.patients().length;
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
    plugins: {
      legend: { display: false },
      tooltip: { enabled: true }
    }
  };

  readonly symptomsData = computed(() => {
    const patients = this.patientService.patients();
    const symptomCounts: Record<string, number> = {
      'Fatigue': 0,
      'Shortness of Breath': 0,
      'Nausea': 0,
      'Insomnia': 0,
      'Constipation': 0,
    };

    patients.forEach(p => {
      const checkIn = this.patientService.getTodayCheckIn(p.id);
      if (checkIn) {
        checkIn.symptoms.forEach(s => {
          if (s in symptomCounts) symptomCounts[s]++;
        });
      }
    });

    return {
      labels: Object.keys(symptomCounts),
      datasets: [{
        data: Object.values(symptomCounts),
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
        grid: { color: 'rgba(0,0,0,0.06)' },
      },
      x: {
        ticks: { color: '#6b7280', font: { size: 11 } },
        grid: { display: false },
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
