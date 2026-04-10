import { Component, input, model } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';

@Component({
  selector: 'app-patient-profile',
  standalone: true,
  imports: [CommonModule, DialogModule],
  templateUrl: './patient-profile.html',
  styleUrl: './patient-profile.css'
})
export class PatientProfile {
  patientDetails = input<any>();
  patientName = input<string>('');

  visible = model<boolean>(false);

  onHide() {
    this.visible.set(false);
  }
}
