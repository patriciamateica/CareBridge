import {Component, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartModule } from 'primeng/chart';
import { SliderModule } from 'primeng/slider';
import { CheckboxModule } from 'primeng/checkbox';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';

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
    TextareaModule
  ],
  templateUrl: './home-patient.html',
  styleUrl: './home-patient.css'
})
export class HomePatient {

  patientName = 'Maria Vaida';
  currentDate = 'Mar 12th 2026';

  painLevel: number = 9;

  symptoms = {
    nausea: false,
    insomnia: false,
    fatigue: false,
    shortnessOfBreath: false,
    constipation: false
  };

  moods = {
    calm: false,
    anxious: false,
    depressed: false,
    irritable: false
  };

  notes: string = '';

  chartData = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [
      {
        label: 'HR',
        data: [150, 160, 155, 165, 150, 160, 150],
        fill: false,
        borderColor: '#C06C66',
        tension: 0.4,
        borderWidth: 2
      },
      {
        label: 'BP',
        data: [100, 110, 105, 130, 95, 115, 120],
        fill: false,
        borderColor: '#78A17D',
        tension: 0.4,
        borderWidth: 2
      },
      {
        label: 'RR',
        data: [90, 85, 95, 80, 85, 90, 80],
        fill: false,
        borderColor: '#4A6B50',
        tension: 0.4,
        borderWidth: 2
      },
      {
        label: 'SpO2',
        data: [15, 12, 25, 10, 15, 25, 10],
        fill: false,
        borderColor: '#88B7B2',
        tension: 0.4,
        borderWidth: 2
      }
    ]
  };

  chartOptions = {
    maintainAspectRatio: false,
    aspectRatio: 1.5,
    plugins: {
      legend: {
        display: false
      }
    },
    scales: {
      x: {
        display: false
      },
      y: {
        min: 0,
        max: 200,
        ticks: {
          color: '#6c757d',
          stepSize: 50
        },
        grid: {
          color: '#dfe7ef',
          drawBorder: false
        }
      }
    }
  };
  profileDialogVisible = signal(false);
  submitWellnessCheck() {
    console.log('Submitting data...', {
      pain: this.painLevel,
      symptoms: this.symptoms,
      moods: this.moods,
      notes: this.notes
    });
  }
}
