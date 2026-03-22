import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatientDetail } from './patient-detail';
import { MessageService } from 'primeng/api';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { DailyCheckIn, Patient, VitalReading } from '../patient-crud/patient-model';
import { PatientService } from '../patient-crud/patient-service';

const mockPatient: Patient = {
  id: 'p-test', firstName: 'Maria', lastName: 'Vaida',
  dateOfBirth: new Date('1945-03-12'),
  diagnosis: 'Test Diagnosis', neurologicalStatus: 'Normal',
  associatedConditions: 'None', status: 'Critical',
  phone: '0721123456', address: 'Str. Test 1',
  assignedNurse: 'Ana Pop', notes: '', createdAt: new Date()
};

const mockVital: VitalReading = {
  timestamp: new Date(), heartRate: 103, systolic: 111,
  diastolic: 80, respiratoryRate: 11, spo2: 92
};

const mockVital2: VitalReading = {
  timestamp: new Date(Date.now() + 1000), heartRate: 95, systolic: 120,
  diastolic: 78, respiratoryRate: 14, spo2: 97
};

describe('PatientDetail', () => {
  let component: PatientDetail;
  let fixture: ComponentFixture<PatientDetail>;
  let patientService: jasmine.SpyObj<PatientService>;
  let messageService: MessageService;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    router = jasmine.createSpyObj('Router', ['navigate']);
    router.navigate.and.returnValue(Promise.resolve(true));

    patientService = jasmine.createSpyObj('PatientService', [
      'getById', 'getVitals', 'getCareNotes', 'getMedications',
      'getTodayCheckIn', 'getCheckIns', 'getLastVitalTime',
      'addCareNote', 'deleteCareNote', 'addMedication', 'deleteMedication',
      'upsertCheckIn', 'deleteCheckIn', 'update', 'delete'
    ]);

    patientService.getById.and.returnValue(mockPatient);
    patientService.getVitals.and.returnValue([mockVital, mockVital2]);
    patientService.getCareNotes.and.returnValue([]);
    patientService.getMedications.and.returnValue([]);
    patientService.getTodayCheckIn.and.returnValue(null);
    patientService.getCheckIns.and.returnValue([]);
    patientService.getLastVitalTime.and.returnValue(new Date());

    await TestBed.configureTestingModule({
      imports: [PatientDetail, NoopAnimationsModule],
      providers: [
        { provide: PatientService, useValue: patientService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: {
            snapshot: { paramMap: { get: (_: string) => 'p-test' } }
          }},
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientDetail);
    component = fixture.componentInstance;

    messageService = fixture.debugElement.injector.get(MessageService);
    spyOn(messageService, 'add');

    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('should load patient by route id on ngOnInit', () => {
    expect(patientService.getById).toHaveBeenCalledWith('p-test');
    expect(component.patient()?.firstName).toBe('Maria');
  });

  it('should pre-fill check-in when today entry exists', () => {
    const existing: DailyCheckIn = {
      id: 'ci-1', patientId: 'p-test', date: new Date(),
      painLevel: 7, mood: 'Anxious', symptoms: ['Fatigue'],
      comments: 'Some pain', lastModified: new Date()
    };
    patientService.getTodayCheckIn.and.returnValue(existing);
    component.ngOnInit();
    expect(component.checkInPainLevel()).toBe(7);
    expect(component.checkInMood()).toBe('Anxious');
    expect(component.checkInSymptoms()).toContain('Fatigue');
    expect(component.checkInComments()).toBe('Some pain');
  });

  it('should navigate away when patient not found', () => {
    patientService.getById.and.returnValue(undefined);
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/patient-management']);
  });

  it('vitals() should return readings from service', () => {
    expect(component.vitals().length).toBe(2);
    expect(component.vitals()[0].heartRate).toBe(103);
  });

  it('latestVital() should return the last reading', () => {
    expect(component.latestVital()).not.toBeNull();
    expect(component.latestVital()!.heartRate).toBe(95);
  });

  it('latestVital() should return null when no vitals', () => {
    patientService.getVitals.and.returnValue([]);
    component.patient.set(null);
    component.patient.set(mockPatient);
    expect(component.latestVital()).toBeNull();
  });

  describe('sparklinePath()', () => {
    it('returns a valid SVG path string when vitals exist', () => {
      const path = component.sparklinePath();
      expect(path).not.toBeNull();
      expect(path).toContain('M ');   // starts with move-to
      expect(path).toContain('C ');   // contains cubic bezier curves
    });

    it('returns null when fewer than 2 vitals', () => {
      patientService.getVitals.and.returnValue([mockVital]);
      component.patient.set(null);
      component.patient.set(mockPatient);
      expect(component.sparklinePath()).toBeNull();
    });

    it('returns null when no vitals', () => {
      patientService.getVitals.and.returnValue([]);
      component.patient.set(null);
      component.patient.set(mockPatient);
      expect(component.sparklinePath()).toBeNull();
    });

    it('path starts at x=0', () => {
      const path = component.sparklinePath();
      expect(path).toMatch(/^M 0,/);
    });
  });

  it('recentVitalsTable() should return last 5 entries in reverse order', () => {
    const many: VitalReading[] = Array.from({ length: 8 }, (_, i) => ({
      ...mockVital, heartRate: 70 + i,
      timestamp: new Date(Date.now() + i * 1000)
    }));
    patientService.getVitals.and.returnValue(many);
    component.patient.set(null);
    component.patient.set(mockPatient);
    expect(component.recentVitalsTable().length).toBe(5);
    expect(component.recentVitalsTable()[0].heartRate).toBe(77);
  });

  describe('isAnyCritical()', () => {
    it('returns true when patient status is Critical', () => {
      component.patient.set({ ...mockPatient, status: 'Critical' });
      expect(component.isAnyCritical()).toBeTrue();
    });

    it('returns false when patient status is Active', () => {
      component.patient.set({ ...mockPatient, status: 'Active' });
      expect(component.isAnyCritical()).toBeFalse();
    });

    it('returns false when patient status is Inactive', () => {
      component.patient.set({ ...mockPatient, status: 'Inactive' });
      expect(component.isAnyCritical()).toBeFalse();
    });

    it('returns false when patient is null', () => {
      component.patient.set(null);
      expect(component.isAnyCritical()).toBeFalse();
    });
  });

  describe('getSeverity()', () => {
    it('Active → success',  () => expect(component.getSeverity('Active')).toBe('success'));
    it('Inactive → warn',   () => expect(component.getSeverity('Inactive')).toBe('warn'));
    it('Critical → danger', () => expect(component.getSeverity('Critical')).toBe('danger'));
  });

  describe('getPainColor()', () => {
    it('green for low pain',     () => expect(component.getPainColor(2)).toBe('#4ade80'));
    it('yellow for medium pain', () => expect(component.getPainColor(5)).toBe('#facc15'));
    it('red for high pain',      () => expect(component.getPainColor(9)).toBe('#ef4444'));
    it('boundary 3 → green',    () => expect(component.getPainColor(3)).toBe('#4ade80'));
    it('boundary 6 → yellow',   () => expect(component.getPainColor(6)).toBe('#facc15'));
    it('boundary 7 → red',      () => expect(component.getPainColor(7)).toBe('#ef4444'));
  });

  describe('toggleSymptom()', () => {
    it('adds symptom when not present', () => {
      component.checkInSymptoms.set([]);
      component.toggleSymptom('Fatigue');
      expect(component.checkInSymptoms()).toContain('Fatigue');
    });
    it('removes symptom when already present', () => {
      component.checkInSymptoms.set(['Fatigue']);
      component.toggleSymptom('Fatigue');
      expect(component.checkInSymptoms()).not.toContain('Fatigue');
    });
    it('keeps other symptoms when removing one', () => {
      component.checkInSymptoms.set(['Fatigue', 'Nausea']);
      component.toggleSymptom('Fatigue');
      expect(component.checkInSymptoms()).toContain('Nausea');
      expect(component.checkInSymptoms()).not.toContain('Fatigue');
    });
  });

  describe('submitCheckIn()', () => {
    it('calls upsertCheckIn with correct data', () => {
      component.checkInPainLevel.set(8);
      component.checkInMood.set('Anxious');
      component.checkInSymptoms.set(['Nausea']);
      component.checkInComments.set('Feeling rough');
      component.submitCheckIn();
      expect(patientService.upsertCheckIn).toHaveBeenCalledWith('p-test',
        jasmine.objectContaining({
          painLevel: 8, mood: 'Anxious',
          symptoms: ['Nausea'], comments: 'Feeling rough'
        })
      );
    });

    it('shows success toast', () => {
      component.submitCheckIn();
      expect(messageService.add).toHaveBeenCalledWith(
        jasmine.objectContaining({ severity: 'success' })
      );
    });

    it('does nothing when patient is null', () => {
      component.patient.set(null);
      component.submitCheckIn();
      expect(patientService.upsertCheckIn).not.toHaveBeenCalled();
    });
  });

  describe('submitNote()', () => {
    it('does nothing when content is empty', () => {
      component.newNoteContent.set('');
      component.submitNote();
      expect(patientService.addCareNote).not.toHaveBeenCalled();
    });

    it('does nothing when content is only whitespace', () => {
      component.newNoteContent.set('   ');
      component.submitNote();
      expect(patientService.addCareNote).not.toHaveBeenCalled();
    });

    it('adds note, clears input and shows toast', () => {
      component.newNoteContent.set('Patient doing well');
      component.submitNote();
      expect(patientService.addCareNote).toHaveBeenCalledWith(
        'p-test', 'Patient doing well', 'Ana Pop'
      );
      expect(component.newNoteContent()).toBe('');
      expect(messageService.add).toHaveBeenCalledWith(
        jasmine.objectContaining({ severity: 'success' })
      );
    });

    it('does nothing when patient is null', () => {
      component.patient.set(null);
      component.newNoteContent.set('Some note');
      component.submitNote();
      expect(patientService.addCareNote).not.toHaveBeenCalled();
    });
  });

  describe('submitMedication()', () => {
    it('does nothing when name is empty', () => {
      component.newMedName.set('');
      component.newMedDose.set('100mg');
      component.newMedTiming.set('OD');
      component.submitMedication();
      expect(patientService.addMedication).not.toHaveBeenCalled();
    });

    it('does nothing when dose is empty', () => {
      component.newMedName.set('Aspirin');
      component.newMedDose.set('');
      component.newMedTiming.set('OD');
      component.submitMedication();
      expect(patientService.addMedication).not.toHaveBeenCalled();
    });

    it('does nothing when timing is empty', () => {
      component.newMedName.set('Aspirin');
      component.newMedDose.set('100mg');
      component.newMedTiming.set('');
      component.submitMedication();
      expect(patientService.addMedication).not.toHaveBeenCalled();
    });

    it('adds medication, closes dialog and shows toast', () => {
      component.newMedName.set('Aspirin');
      component.newMedDose.set('100mg');
      component.newMedTiming.set('OD');
      component.showMedDialog.set(true);
      component.submitMedication();
      expect(patientService.addMedication).toHaveBeenCalledWith(
        'p-test', 'Aspirin', '100mg', 'OD', 'Ana Pop'
      );
      expect(component.showMedDialog()).toBeFalse();
      expect(messageService.add).toHaveBeenCalledWith(
        jasmine.objectContaining({ severity: 'success' })
      );
    });

    it('does nothing when patient is null', () => {
      component.patient.set(null);
      component.newMedName.set('Aspirin');
      component.newMedDose.set('100mg');
      component.newMedTiming.set('OD');
      component.submitMedication();
      expect(patientService.addMedication).not.toHaveBeenCalled();
    });
  });

  describe('deleteNote()', () => {
    it('calls deleteCareNote with correct id', () => {
      component.deleteNote('n-1');
      expect(patientService.deleteCareNote).toHaveBeenCalledWith('n-1');
    });
  });

  describe('deleteMedication()', () => {
    it('calls deleteMedication with correct id', () => {
      component.deleteMedication('m-1');
      expect(patientService.deleteMedication).toHaveBeenCalledWith('m-1');
    });
  });

  describe('onPatientUpdated()', () => {
    it('refreshes patient signal from service', () => {
      const updated = { ...mockPatient, firstName: 'Updated' };
      patientService.getById.and.returnValue(updated);
      component.onPatientUpdated();
      expect(patientService.getById).toHaveBeenCalledWith('p-test');
      expect(component.patient()?.firstName).toBe('Updated');
    });

    it('sets patient to null if service returns undefined', () => {
      patientService.getById.and.returnValue(undefined);
      component.onPatientUpdated();
      expect(component.patient()).toBeNull();
    });
  });

  describe('openMedDialog()', () => {
    it('clears all fields and opens dialog', () => {
      component.newMedName.set('Old');
      component.newMedDose.set('Old');
      component.newMedTiming.set('Old');
      component.openMedDialog();
      expect(component.showMedDialog()).toBeTrue();
      expect(component.newMedName()).toBe('');
      expect(component.newMedDose()).toBe('');
      expect(component.newMedTiming()).toBe('');
    });
  });

  describe('goBack()', () => {
    it('navigates back to patient management', () => {
      component.goBack();
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard/patient-management']);
    });
  });

});
