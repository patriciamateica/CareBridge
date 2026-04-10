import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartModule } from 'primeng/chart';
import {PatientService} from '../patient/patient-crud/patient-service';

@Component({
  selector: 'app-home-nurse',
  standalone: true,
  imports: [CommonModule, ChartModule],
  templateUrl: './home-nurse.html',
  styleUrl: './home-nurse.css'
})
export class HomeNurse {
  private readonly patientService = inject(PatientService);

  nurseName = signal('Sara M.');
  currentDate = signal('Mar 12, 2026');

  tasks = signal([
    { id: 1, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 2, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 3, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 1, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 2, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 3, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 1, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 2, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 3, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' },
    { id: 4, time: '10:00 am', type: 'Check Up', patientName: 'Maria Vaida' }
  ]);

  criticalCount = computed(() =>
    this.patientService.patients().filter(p => p.status === 'Critical').length
  );

  totalCount = computed(() => this.patientService.patients().length);

  normalCount = computed(() => this.totalCount() - this.criticalCount());

  alerts = computed(() =>
    this.patientService.patients()
      .filter(p => p.status === 'Critical')
      .map(p => ({
        id: p.id,
        patientName: `${p.firstName} ${p.lastName}`,
        description: 'Low SpO2 (92%) - Vitals Alert'
      }))
  );

  chartData = computed(() => ({
    labels: ['Critical', 'Normal'],
    datasets: [
      {
        data: [this.criticalCount(), this.normalCount()],
        backgroundColor: ['#b32d2d', '#6a966a'],
        hoverBackgroundColor: ['#9e2424', '#5a825a'],
        borderWidth: 0,
      }
    ]
  }));

  readonly chartOptions = {
    cutout: '80%',
    plugins: {
      legend: { display: false },
      tooltip: { enabled: true }
    },
    maintainAspectRatio: false,
    responsive: true
  };

  criticalPercent = computed(() =>
    this.totalCount() ? Math.round((this.criticalCount() / this.totalCount()) * 100) : 0
  );

  normalPercent = computed(() => 100 - this.criticalPercent());
}
