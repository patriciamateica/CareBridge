import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { ChartModule } from 'primeng/chart';
import { SortEvent } from 'primeng/api';

import { AuthService } from '../../../../auth-service/auth.service';
import { PatientManagementFacadeService } from '../../../cruds/services/patient-management.facade';
import { CreatePatient } from '../../ui/create-patient/create-patient';
import { UpdatePatient } from '../../ui/update-patient/update-patient';
import { DeletePatient } from '../../ui/delete-patient/delete-patient';
import { Patient } from '../../../cruds/models/patient-ui-models';

@Component({
  selector: 'app-patient-management',
  standalone: true,
  imports: [
    DatePipe, FormsModule, TableModule, TagModule, InputTextModule,
    ChartModule,
    CreatePatient, UpdatePatient, DeletePatient,
  ],
  templateUrl: './patient-management.html',
  styleUrl: './patient-management.css',
})
export class PatientManagement implements OnInit, OnDestroy {
  protected readonly facade      = inject(PatientManagementFacadeService);
  protected readonly authService = inject(AuthService);
  private readonly router        = inject(Router);

  readonly donutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '65%',
    plugins: { legend: { display: false }, tooltip: { enabled: true } },
  };

  readonly barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, ticks: { stepSize: 1, color: '#6b7280' }, grid: { color: 'rgba(0,0,0,0.06)' } },
      x: { ticks: { color: '#6b7280', font: { size: 11 } }, grid: { display: false } },
    },
  };

  ngOnInit(): void  { this.facade.init(); }
  ngOnDestroy(): void { this.facade.destroy(); }

  customSort(event: SortEvent): void {
    if (!event.data) return;
    event.data.sort((a: any, b: any) => {
      let result: number;
      if (event.field === 'status') {
        result = this.facade.compareByStatus(a as Patient, b as Patient);
      } else if (event.field === 'firstName' || event.field === 'lastName') {
        result = `${a.firstName} ${a.lastName}`.toLowerCase()
          .localeCompare(`${b.firstName} ${b.lastName}`.toLowerCase());
      } else {
        result = (a[event.field!] ?? '').toString().toLowerCase()
          .localeCompare((b[event.field!] ?? '').toString().toLowerCase());
      }
      return (event.order || 1) * result;
    });
  }

  openDetail(patient: Patient): void {
    this.router.navigate(['/dashboard/patient-detail', patient.id]);
  }
}
