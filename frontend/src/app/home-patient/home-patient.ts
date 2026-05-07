import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartModule } from 'primeng/chart';
import { SliderModule } from 'primeng/slider';
import { CheckboxModule } from 'primeng/checkbox';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { firstValueFrom } from 'rxjs';

import { AuthService } from '../../auth-service/auth.service';

import { UserService } from '../cruds/services/userService';
import { VitalsService } from '../cruds/services/vitalsService';
import { HealthStatusService } from '../cruds/services/healthStatusService';

import { User } from '../cruds/models/user';
import { Vitals } from '../cruds/models/vitals';

@Component({
  selector: 'app-home-patient',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ChartModule,
    SliderModule,
    CheckboxModule,
    ButtonModule,
    TextareaModule,
  ],
  templateUrl: './home-patient.html',
  styleUrl: './home-patient.css'
})
export class HomePatient implements OnInit {
  private readonly userSvc = inject(UserService);
  private readonly vitalsSvc = inject(VitalsService);
  private readonly healthSvc = inject(HealthStatusService);
  private readonly authService = inject(AuthService);

  readonly profileDialogVisible = signal(false);

  private readonly _currentUser = signal<User | null>(null);
  private readonly _vitals = signal<Vitals[]>([]);

  readonly patientName = computed(() => {
    const user = this._currentUser();
    return user ? `${user.firstName} ${user.lastName}` : 'Patient';
  });

  readonly currentDate = new Date();

  painLevel = 5;
  symptoms = { nausea: false, insomnia: false, fatigue: false, shortnessOfBreath: false, constipation: false };
  moods = { calm: false, anxious: false, depressed: false, irritable: false };
  notes = '';

  ngOnInit() {
    const id = this.authService.currentUserId();
    if (id) {
      this.userSvc.getById(id).subscribe(u => this._currentUser.set(u));
      this.vitalsSvc.getAll().subscribe((res: any) => {
        this._vitals.set(res.content?.filter((v: any) => v.patientId === id) || []);
      });

      this.vitalsSvc.listenToUpdates('/topic/vitals').subscribe((v: Vitals) => {
        if (v.patientId === id) this._vitals.update(list => [...list, v]);
      });
    }
  }

  readonly latestVital = computed(() => {
    const v = this._vitals();
    return v.length ? v[v.length - 1] : null;
  });

  isMetricAbnormal(metric: 'hr' | 'bp' | 'rr' | 'spo2'): boolean {
    const v = this.latestVital();
    if (!v) return false;
    switch (metric) {
      case 'hr': return v.heartRate < 60 || v.heartRate > 100;
      case 'bp': return v.bloodPressure < 90 || v.bloodPressure > 140;
      case 'rr': return v.respiratoryRate < 12 || v.respiratoryRate > 20;
      case 'spo2': return v.spO2 < 95;
    }
  }

  metricCardClass(metric: 'hr' | 'bp' | 'rr' | 'spo2'): string {
    return this.isMetricAbnormal(metric) ? 'bg-[#b92b27]' : 'bg-[#78A17D]';
  }

  metricLabel(metric: 'hr' | 'bp' | 'rr' | 'spo2'): string {
    return this.isMetricAbnormal(metric) ? 'Abnormal' : 'Normal';
  }

  readonly chartData = computed(() => {
    const vitals = this._vitals().slice(-7);
    return {
      labels: vitals.map((v) => new Date(v.timestamp).toLocaleDateString([], { weekday: 'short' })),
      datasets: [
        { label: 'HR', data: vitals.map((v) => v.heartRate), fill: false, borderColor: '#C06C66', tension: 0.4, borderWidth: 2 },
        { label: 'BP', data: vitals.map((v) => v.bloodPressure), fill: false, borderColor: '#78A17D', tension: 0.4, borderWidth: 2 },
        { label: 'RR', data: vitals.map((v) => v.respiratoryRate), fill: false, borderColor: '#4A6B50', tension: 0.4, borderWidth: 2 },
        { label: 'SpO2', data: vitals.map((v) => v.spO2), fill: false, borderColor: '#88B7B2', tension: 0.4, borderWidth: 2 }
      ]
    };
  });

  readonly chartOptions = {
    maintainAspectRatio: false,
    aspectRatio: 1.5,
    plugins: { legend: { display: false } },
    scales: { x: { display: false }, y: { min: 0, max: 200, ticks: { color: '#6c757d', stepSize: 50 }, grid: { color: '#dfe7ef', drawBorder: false } } }
  };

  async submitWellnessCheck() {
    const id = this.authService.currentUserId();
    if (!id) return;

    const selectedSymptoms = Object.entries(this.symptoms).filter(([_, v]) => v).map(([k, _]) => k);
    const selectedMood = Object.entries(this.moods).find(([_, v]) => v)?.[0] || 'calm';

    await firstValueFrom(this.healthSvc.create({
        patientId: id,
        painScale: this.painLevel,
        mood: selectedMood,
        symptoms: selectedSymptoms,
        notes: this.notes,
        timestamp: new Date().toISOString()
    } as any));

    // Reset form
    this.notes = '';
  }
}
