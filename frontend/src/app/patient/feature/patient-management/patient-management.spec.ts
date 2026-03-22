import {ComponentFixture, TestBed} from '@angular/core/testing';
import {PatientManagement} from './patient-management';
import {PatientService} from '../../patient-crud/patient-service';
import {MessageService} from 'primeng/api';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {provideRouter, Router} from '@angular/router';
import {signal} from '@angular/core';
import {Patient} from '../../patient-crud/patient-model';

const makePatient = (overrides: Partial<Patient> = {}): Patient => ({
  id: 'p1', firstName: 'Maria', lastName: 'Ionescu',
  dateOfBirth: new Date(),
  diagnosis: 'Hypertension', neurologicalStatus: 'Normal',
  associatedConditions: 'None', status: 'Active',
  phone: '0721123456', address: 'Str. Test', assignedNurse: 'Ana Pop',
  notes: '', createdAt: new Date(), ...overrides
});

const seedPatients: Patient[] = [
  makePatient({id: 'p1', status: 'Active', firstName: 'Maria'}),
  makePatient({id: 'p2', status: 'Active', firstName: 'Ion', lastName: 'Popa', diagnosis: 'Diabetes'}),
  makePatient({id: 'p3', status: 'Critical', firstName: 'Vasile', assignedNurse: 'Elena Marin'}),
];

describe('PatientManagement', () => {
  let component: PatientManagement;
  let fixture: ComponentFixture<PatientManagement>;
  let patientService: jasmine.SpyObj<PatientService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    patientService = jasmine.createSpyObj('PatientService', [
      'getLastVitalTime', 'getTodayCheckIn'
    ], {
      patients: signal(seedPatients).asReadonly(),
    });
    patientService.getLastVitalTime.and.returnValue(null);
    patientService.getTodayCheckIn.and.returnValue(null);

    router = jasmine.createSpyObj('Router', ['navigate']);
    router.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [PatientManagement, NoopAnimationsModule],
      providers: [
        {provide: PatientService, useValue: patientService},
        {provide: Router, useValue: router},
        MessageService,
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('should default to table view', () => {
    expect(component.viewMode()).toBe('table');
  });

  it('should switch to table view', () => {
    component.viewMode.set('table');
    expect(component.viewMode()).toBe('table');
  });

  it('should switch back to charts view', () => {
    component.viewMode.set('table');
    component.viewMode.set('charts');
    expect(component.viewMode()).toBe('charts');
  });

  it('activeCount() should count Active patients', () => {
    expect(component.activeCount()).toBe(2);
  });

  it('criticalCount() should count Critical patients', () => {
    expect(component.criticalCount()).toBe(1);
  });

  describe('donutData()', () => {
    it('should have 2 labels', () => {
      expect(component.donutData().labels.length).toBe(2);
    });

    it('should compute correct normal and critical percentages', () => {
      expect(component.donutData().normalPct).toBe(67);
      expect(component.donutData().criticalPct).toBe(33);
    });

    it('should use green for Normal and red for Critical', () => {
      const colors = component.donutData().datasets[0].backgroundColor as string[];
      expect(colors[0]).toBe('#5f8d5f');
      expect(colors[1]).toBe('#b92b27');
    });

    it('should handle zero patients gracefully', () => {
      (patientService as any).patients = signal([]).asReadonly();
      expect(() => component.donutData()).not.toThrow();
    });
  });

  describe('symptomsData()', () => {
    it('should have 5 symptom labels', () => {
      expect(component.symptomsData().labels.length).toBe(5);
    });

    it('should include Fatigue as first label', () => {
      expect(component.symptomsData().labels[0]).toBe('Fatigue');
    });

    it('should count symptoms from today check-ins', () => {
      patientService.getTodayCheckIn.and.callFake((id: string) => {
        if (id === 'p1') return {
          id: 'ci-1', patientId: 'p1', date: new Date(),
          painLevel: 5, mood: 'Calm', symptoms: ['Fatigue', 'Nausea'],
          comments: '', lastModified: new Date()
        };
        return null;
      });
      const data = component.symptomsData();
      const fatigueIdx = data.labels.indexOf('Fatigue');
      const nauseaIdx = data.labels.indexOf('Nausea');
      expect(data.datasets[0].data[fatigueIdx]).toBe(1);
      expect(data.datasets[0].data[nauseaIdx]).toBe(1);
    });
  });

  it('filteredPatients() shows all when search is empty', () => {
    expect(component.filteredPatients().length).toBe(3);
  });

  it('filteredPatients() filters by name', () => {
    component.searchQuery.set('maria');
    expect(component.filteredPatients().length).toBe(1);
    expect(component.filteredPatients()[0].firstName).toBe('Maria');
  });

  it('filteredPatients() filters by diagnosis', () => {
    component.searchQuery.set('diabetes');
    expect(component.filteredPatients().length).toBe(1);
  });

  it('filteredPatients() filters by nurse', () => {
    component.searchQuery.set('elena marin');
    expect(component.filteredPatients().length).toBe(1);
  });

  it('filteredPatients() returns empty for no match', () => {
    component.searchQuery.set('zzznomatch');
    expect(component.filteredPatients().length).toBe(0);
  });

  describe('getSeverity()', () => {
    it('Active → success', () => expect(component.getSeverity('Active')).toBe('success'));
    it('Inactive → warn', () => expect(component.getSeverity('Inactive')).toBe('warn'));
    it('Critical → danger', () => expect(component.getSeverity('Critical')).toBe('danger'));
  });

  it('openDetail() navigates to patient detail route', () => {
    component.openDetail(seedPatients[0]);
    expect(router.navigate).toHaveBeenCalledWith(
      ['/dashboard/patient-management', 'p1']
    );
  });
});
