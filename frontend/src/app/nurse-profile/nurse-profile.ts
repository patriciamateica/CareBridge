import { Component, input, model } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';

@Component({
  selector: 'app-nurse-profile',
  standalone: true,
  imports: [CommonModule, DialogModule],
  templateUrl: './nurse-profile.html',
  styleUrl: './nurse-profile.css'
})
export class NurseProfile {
  nurseDetails = input<any>();
  nurseName = input<string>('');

  visible = model<boolean>(false);

  onHide() {
    this.visible.set(false);
  }
}
