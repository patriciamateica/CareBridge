// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { signal } from '@angular/core';
// import { ActivatedRoute, Router } from '@angular/router';
// import { NoopAnimationsModule } from '@angular/platform-browser/animations';
// import { Subject } from 'rxjs';
//
// import { PatientDetail } from './patient-detail';
// import { DailyCheckIn, Patient, VitalReading } from '../../cruds/models/patient-ui-models';
// import { RealtimeDataService } from '../../services/realtime-data.service';
// import { RealtimeMutationOfflineService } from '../../offline-support/realtime-mutation-offline.service';
// import { ToastService } from '../../toast-service/toast-service';
//
// const mockPatient: Patient = {
//   id: 'p-test',
//   firstName: 'Maria',
//   lastName: 'Vaida',
//   dateOfBirth: new Date('1945-03-12'),
//   diagnosis: 'Test Diagnosis',
//   neurologicalStatus: 'Normal',
//   associatedConditions: 'None',
//   status: 'Critical',
//   phone: '0721123456',
//   address: 'Str. Test 1',
//   assignedNurse: 'Ana Pop',
//   notes: '',
//   createdAt: new Date(),
// };
//
// const mockVital: VitalReading = {
//   timestamp: new Date(),
//   heartRate: 103,
//   systolic: 111,
//   diastolic: 80,
//   respiratoryRate: 11,
//   spo2: 92,
// };
//
// const mockVital2: VitalReading = {
//   timestamp: new Date(Date.now() + 1000),
//   heartRate: 95,
//   systolic: 120,
//   diastolic: 78,
//   respiratoryRate: 14,
//   spo2: 97,
// };
//
// class FakeRealtimeDataService {
//   readonly patientRows = signal<Record<string, Patient>>({});
//   readonly vitalsRows = signal<Record<string, VitalReading[]>>({});
//   readonly checkInsRows = signal<Record<string, DailyCheckIn | null>>({});
//   readonly careNotesRows = signal<Record<string, any[]>>({});
//   readonly medicationsRows = signal<Record<string, any[]>>({});
//
//   ensurePatientLiveData(_: string): void {}
//
//   getById(id: string): Patient | undefined {
//     return this.patientRows()[id];
//   }
//
//   getVitals(id: string): VitalReading[] {
//     return this.vitalsRows()[id] ?? [];
//   }
//
//   getTodayCheckIn(id: string): DailyCheckIn | null {
//     return this.checkInsRows()[id] ?? null;
//   }
//
//   getCareNotes(id: string): any[] {
//     return this.careNotesRows()[id] ?? [];
//   }
//
//   getMedications(id: string): any[] {
//     return this.medicationsRows()[id] ?? [];
//   }
// }
//
// describe('PatientDetail', () => {
//   let component: PatientDetail;
//   let fixture: ComponentFixture<PatientDetail>;
//   let router: jasmine.SpyObj<Router>;
//   let realtimeData: ReturnType<typeof createRealtimeDataMock>;
//   let realtimeMutations: jasmine.SpyObj<RealtimeMutationOfflineService>;
//   let toastService: jasmine.SpyObj<ToastService>;
//
//   const createRealtimeDataMock = () => {
//     const patientRows = signal<Record<string, Patient>>({});
//     const vitalsRows = signal<Record<string, VitalReading[]>>({});
//     const checkInsRows = signal<Record<string, DailyCheckIn | null>>({});
//     const careNotesRows = signal<Record<string, any[]>>({});
//     const medicationsRows = signal<Record<string, any[]>>({});
//
//     return {
//       patientRows,
//       vitalsRows,
//       checkInsRows,
//       careNotesRows,
//       medicationsRows,
//       ensurePatientLiveData: (_: string) => undefined,
//       getById: (id: string) => patientRows()[id],
//       getVitals: (id: string) => vitalsRows()[id] ?? [],
//       getTodayCheckIn: (id: string) => checkInsRows()[id] ?? null,
//       getCareNotes: (id: string) => careNotesRows()[id] ?? [],
//       getMedications: (id: string) => medicationsRows()[id] ?? [],
//     };
//   };
//
//   beforeEach(async () => {
//     router = jasmine.createSpyObj('Router', ['navigate'], { events: new Subject() });
//     router.navigate.and.returnValue(Promise.resolve(true));
//
//     realtimeData = createRealtimeDataMock();
//     realtimeData.patientRows.set({ 'p-test': mockPatient });
//     realtimeData.vitalsRows.set({ 'p-test': [mockVital, mockVital2] });
//     realtimeData.checkInsRows.set({ 'p-test': null });
//     realtimeData.careNotesRows.set({ 'p-test': [] });
//     realtimeData.medicationsRows.set({ 'p-test': [] });
//
//     realtimeMutations = jasmine.createSpyObj('RealtimeMutationOfflineService', [
//       'upsertCheckIn',
//       'addCareNote',
//       'addMedication',
//     ]);
//     realtimeMutations.upsertCheckIn.and.resolveTo();
//     realtimeMutations.addCareNote.and.resolveTo();
//     realtimeMutations.addMedication.and.resolveTo();
//
//     toastService = jasmine.createSpyObj('ToastService', ['showSuccess', 'showError']);
//
//     await TestBed.configureTestingModule({
//       imports: [PatientDetail, NoopAnimationsModule],
//       providers: [
//         { provide: RealtimeDataService, useValue: realtimeData },
//         { provide: RealtimeMutationOfflineService, useValue: realtimeMutations },
//         { provide: ToastService, useValue: toastService },
//         { provide: Router, useValue: router },
//         {
//           provide: ActivatedRoute,
//           useValue: {
//             snapshot: { paramMap: { get: (_: string) => 'p-test' } },
//           },
//         },
//       ],
//     }).compileComponents();
//
//     fixture = TestBed.createComponent(PatientDetail);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });
//
//   it('should create', () => expect(component).toBeTruthy());
//
//   it('should load patient by route id on ngOnInit', () => {
//     expect(component.patient()?.firstName).toBe('Maria');
//   });
//
//   it('should pre-fill check-in when today entry exists', () => {
//     const existing: DailyCheckIn = {
//       id: 'ci-1',
//       patientId: 'p-test',
//       date: new Date(),
//       painLevel: 7,
//       mood: 'Anxious',
//       symptoms: ['Fatigue'],
//       comments: 'Some pain',
//       lastModified: new Date(),
//     };
//     realtimeData.checkInsRows.set({ 'p-test': existing });
//     fixture.detectChanges();
//     expect(component.checkInPainLevel()).toBe(7);
//     expect(component.checkInMood()).toBe('Anxious');
//     expect(component.checkInSymptoms()).toContain('Fatigue');
//     expect(component.checkInComments()).toBe('Some pain');
//   });
//
//   it('should navigate away when patient not found', () => {
//     realtimeData.patientRows.set({});
//     component.ngOnInit();
//     fixture.detectChanges();
//     expect(router.navigate).toHaveBeenCalledWith(['/dashboard/patient-management']);
//   });
//
//   it('vitals() should return readings from service', () => {
//     expect(component.vitals().length).toBe(2);
//     expect(component.vitals()[0].heartRate).toBe(103);
//   });
//
//   it('latestVital() should return the last reading', () => {
//     expect(component.latestVital()).not.toBeNull();
//     expect(component.latestVital()!.heartRate).toBe(95);
//   });
//
//   it('latestVital() should return null when no vitals', () => {
//     realtimeData.vitalsRows.set({ 'p-test': [] });
//     fixture.detectChanges();
//     expect(component.latestVital()).toBeNull();
//   });
//
//   describe('sparklinePath()', () => {
//     it('returns a valid SVG path string when vitals exist', () => {
//       const path = component.sparklinePath();
//       expect(path).not.toBeNull();
//       expect(path).toContain('M ');
//       expect(path).toContain('C ');
//     });
//
//     it('returns null when fewer than 2 vitals', () => {
//       realtimeData.vitalsRows.set({ 'p-test': [mockVital] });
//       fixture.detectChanges();
//       expect(component.sparklinePath()).toBeNull();
//     });
//
//     it('returns null when no vitals', () => {
//       realtimeData.vitalsRows.set({ 'p-test': [] });
//       fixture.detectChanges();
//       expect(component.sparklinePath()).toBeNull();
//     });
//
//     it('path starts at x=0', () => {
//       const path = component.sparklinePath();
//       expect(path).toMatch(/^M 0,/);
//     });
//   });
//
//   it('recentVitalsTable() should return last 5 entries in reverse order', () => {
//     const many: VitalReading[] = Array.from({ length: 8 }, (_, i) => ({
//       ...mockVital,
//       heartRate: 70 + i,
//       timestamp: new Date(Date.now() + i * 1000),
//     }));
//     realtimeData.vitalsRows.set({ 'p-test': many });
//     fixture.detectChanges();
//     expect(component.recentVitalsTable().length).toBe(5);
//     expect(component.recentVitalsTable()[0].heartRate).toBe(77);
//   });
//
//   describe('isAnyCritical()', () => {
//     it('returns true when patient status is Critical', () => {
//       realtimeData.patientRows.set({ 'p-test': { ...mockPatient, status: 'Critical' } });
//       fixture.detectChanges();
//       expect(component.isAnyCritical()).toBeTrue();
//     });
//
//     it('returns false when patient status is Active', () => {
//       realtimeData.patientRows.set({ 'p-test': { ...mockPatient, status: 'Active' } });
//       fixture.detectChanges();
//       expect(component.isAnyCritical()).toBeFalse();
//     });
//
//     it('returns false when patient status is Inactive', () => {
//       realtimeData.patientRows.set({ 'p-test': { ...mockPatient, status: 'Inactive' } });
//       fixture.detectChanges();
//       expect(component.isAnyCritical()).toBeFalse();
//     });
//
//     it('returns false when patient is null', () => {
//       realtimeData.patientRows.set({});
//       fixture.detectChanges();
//       expect(component.isAnyCritical()).toBeFalse();
//     });
//   });
//
//   describe('getSeverity()', () => {
//     it('Active → success', () => expect(component.getSeverity('Active')).toBe('success'));
//     it('Inactive → warn', () => expect(component.getSeverity('Inactive')).toBe('warn'));
//     it('Critical → danger', () => expect(component.getSeverity('Critical')).toBe('danger'));
//   });
//
//   describe('getPainColor()', () => {
//     it('green for low pain', () => expect(component.getPainColor(2)).toBe('#4ade80'));
//     it('yellow for medium pain', () => expect(component.getPainColor(5)).toBe('#facc15'));
//     it('red for high pain', () => expect(component.getPainColor(9)).toBe('#ef4444'));
//     it('boundary 3 → green', () => expect(component.getPainColor(3)).toBe('#4ade80'));
//     it('boundary 6 → yellow', () => expect(component.getPainColor(6)).toBe('#facc15'));
//     it('boundary 7 → red', () => expect(component.getPainColor(7)).toBe('#ef4444'));
//   });
//
//   describe('toggleSymptom()', () => {
//     it('adds symptom when not present', () => {
//       component.checkInSymptoms.set([]);
//       component.toggleSymptom('Fatigue');
//       expect(component.checkInSymptoms()).toContain('Fatigue');
//     });
//
//     it('removes symptom when already present', () => {
//       component.checkInSymptoms.set(['Fatigue']);
//       component.toggleSymptom('Fatigue');
//       expect(component.checkInSymptoms()).not.toContain('Fatigue');
//     });
//
//     it('keeps other symptoms when removing one', () => {
//       component.checkInSymptoms.set(['Fatigue', 'Nausea']);
//       component.toggleSymptom('Fatigue');
//       expect(component.checkInSymptoms()).toContain('Nausea');
//       expect(component.checkInSymptoms()).not.toContain('Fatigue');
//     });
//   });
//
//   describe('submitCheckIn()', () => {
//     it('calls upsertCheckIn with correct data', async () => {
//       component.checkInPainLevel.set(8);
//       component.checkInMood.set('Anxious');
//       component.checkInSymptoms.set(['Nausea']);
//       component.checkInComments.set('Feeling rough');
//
//       await component.submitCheckIn();
//
//       expect(realtimeMutations.upsertCheckIn).toHaveBeenCalledWith(
//         'p-test',
//         jasmine.objectContaining({
//           painLevel: 8,
//           mood: 'Anxious',
//           symptoms: ['Nausea'],
//           comments: 'Feeling rough',
//         })
//       );
//     });
//
//     it('shows success toast', async () => {
//       await component.submitCheckIn();
//       expect(toastService.showSuccess).toHaveBeenCalledWith('Daily Check-In message saved.');
//     });
//
//     it('does nothing when patient is null', async () => {
//       realtimeData.patientRows.set({});
//       fixture.detectChanges();
//       await component.submitCheckIn();
//       expect(realtimeMutations.upsertCheckIn).not.toHaveBeenCalled();
//     });
//   });
//
//   describe('submitNote()', () => {
//     it('does nothing when content is empty', async () => {
//       component.newNoteContent.set('');
//       await component.submitNote();
//       expect(realtimeMutations.addCareNote).not.toHaveBeenCalled();
//     });
//
//     it('does nothing when content is only whitespace', async () => {
//       component.newNoteContent.set('   ');
//       await component.submitNote();
//       expect(realtimeMutations.addCareNote).not.toHaveBeenCalled();
//     });
//
//     it('adds note, clears input and shows toast', async () => {
//       component.newNoteContent.set('Patient doing well');
//       await component.submitNote();
//       expect(realtimeMutations.addCareNote).toHaveBeenCalledWith('p-test', 'Patient doing well');
//       expect(component.newNoteContent()).toBe('');
//       expect(toastService.showSuccess).toHaveBeenCalledWith('Care Note saved.');
//     });
//
//     it('does nothing when patient is null', async () => {
//       realtimeData.patientRows.set({});
//       fixture.detectChanges();
//       component.newNoteContent.set('Some note');
//       await component.submitNote();
//       expect(realtimeMutations.addCareNote).not.toHaveBeenCalled();
//     });
//   });
//
//   describe('submitMedication()', () => {
//     it('does nothing when name is empty', async () => {
//       component.newMedName.set('');
//       component.newMedDose.set('100mg');
//       component.newMedTiming.set('OD');
//       await component.submitMedication();
//       expect(realtimeMutations.addMedication).not.toHaveBeenCalled();
//     });
//
//     it('does nothing when dose is empty', async () => {
//       component.newMedName.set('Aspirin');
//       component.newMedDose.set('');
//       component.newMedTiming.set('OD');
//       await component.submitMedication();
//       expect(realtimeMutations.addMedication).not.toHaveBeenCalled();
//     });
//
//     it('does nothing when timing is empty', async () => {
//       component.newMedName.set('Aspirin');
//       component.newMedDose.set('100mg');
//       component.newMedTiming.set('');
//       await component.submitMedication();
//       expect(realtimeMutations.addMedication).not.toHaveBeenCalled();
//     });
//
//     it('adds medication, closes dialog and shows toast', async () => {
//       component.newMedName.set('Aspirin');
//       component.newMedDose.set('100mg');
//       component.newMedTiming.set('OD');
//       component.showMedDialog.set(true);
//       await component.submitMedication();
//       expect(realtimeMutations.addMedication).toHaveBeenCalledWith('p-test', 'Aspirin', '100mg', 'OD');
//       expect(component.showMedDialog()).toBeFalse();
//       expect(toastService.showSuccess).toHaveBeenCalledWith('Medication Added.');
//     });
//
//     it('does nothing when patient is null', async () => {
//       realtimeData.patientRows.set({});
//       fixture.detectChanges();
//       component.newMedName.set('Aspirin');
//       component.newMedDose.set('100mg');
//       component.newMedTiming.set('OD');
//       await component.submitMedication();
//       expect(realtimeMutations.addMedication).not.toHaveBeenCalled();
//     });
//   });
//
//   describe('deleteNote()', () => {
//     it('shows the delete note message', () => {
//       component.deleteNote('n-1');
//       expect(toastService.showError).toHaveBeenCalledWith('Delete note is not wired to backend yet.');
//     });
//   });
//
//   describe('deleteMedication()', () => {
//     it('shows the delete medication message', () => {
//       component.deleteMedication('m-1');
//       expect(toastService.showError).toHaveBeenCalledWith('Delete medication is not wired to backend yet.');
//     });
//   });
//
//   it('reflects realtime patient updates', () => {
//     realtimeData.patientRows.set({ 'p-test': { ...mockPatient, firstName: 'Updated' } });
//     fixture.detectChanges();
//     expect(component.patient()?.firstName).toBe('Updated');
//   });
//
//
//   describe('openMedDialog()', () => {
//     it('clears all fields and opens dialog', () => {
//       component.newMedName.set('Old');
//       component.newMedDose.set('Old');
//       component.newMedTiming.set('Old');
//       component.openMedDialog();
//       expect(component.showMedDialog()).toBeTrue();
//       expect(component.newMedName()).toBe('');
//       expect(component.newMedDose()).toBe('');
//       expect(component.newMedTiming()).toBe('');
//     });
//   });
//
//   describe('goBack()', () => {
//     it('navigates back to patient management', () => {
//       component.goBack();
//       expect(router.navigate).toHaveBeenCalledWith(['/dashboard/patient-management']);
//     });
//   });
// });
