import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SliderModule } from 'primeng/slider';

import { AuthService } from '../../../auth-service/auth.service';
import { PatientDetailFacadeService } from '../../cruds/services/patient-detail.facade';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [DatePipe, FormsModule, SliderModule, RouterLink],
  templateUrl: './patient-detail.html',
  styleUrl: './patient-detail.css',
})
export class PatientDetail implements OnInit, OnDestroy {
  private readonly route  = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly auth    = inject(AuthService);
  protected readonly facade  = inject(PatientDetailFacadeService);

  readonly moodOptions    = ['Calm', 'Anxious', 'Depressed', 'Irritable'] as const;
  readonly symptomOptions = ['Nausea', 'Shortness of Breath', 'Fatigue', 'Insomnia', 'Constipation'];

  private patientId!: string;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/dashboard/patient-management']);
      return;
    }
    this.patientId = id;
    this.facade.init(id);
  }

  ngOnDestroy(): void {
    this.facade.destroy();
  }

  onMedicationListScroll(event: Event): void {
    const el = event.target as HTMLElement | null;
    if (!el) return;
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 24) {
      this.facade.loadMoreMedications(this.patientId);
    }
  }

  onCareNotesListScroll(event: Event): void {
    const el = event.target as HTMLElement | null;
    if (!el) return;
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 24) {
      this.facade.loadMoreCareNotes(this.patientId);
    }
  }

  submitCheckIn()                           { this.facade.submitCheckIn(this.patientId); }
  submitNote()                              { this.facade.submitNote(this.patientId); }
  startEditNote(id: string, c: string)      { this.facade.startEditNote(id, c); }
  cancelEditNote()                          { this.facade.cancelEditNote(); }
  deleteNote(id: string)                    { this.facade.deleteNote(id); }
  openMedDialog()                           { this.facade.openMedDialog(); }
  submitMedication()                        { this.facade.submitMedication(this.patientId); }
  deleteMedication(id: string)              { this.facade.deleteMedication(id); }
  toggleSymptom(s: string)                  { this.facade.toggleSymptom(s); }
  goBack()                                  { this.router.navigate(['/dashboard/patient-management']); }
}
