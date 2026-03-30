import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatientManagement } from './patient-management';
import { PatientService } from '../../patient-crud/patient-service';
import { MessageService } from 'primeng/api';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { signal } from '@angular/core';
import { Patient } from '../../patient-crud/patient-model';
import { AuthService } from '../../../../auth-service/auth.service';
import { Subject } from 'rxjs'; // <-- Added to mock router events

const makePatient = (overrides: Partial<Patient> = {}): Patient => ({
  id: 'p1', firstName: 'Maria', lastName: 'Ionescu',
  dateOfBirth: new Date(),
  diagnosis: 'Hypertension', neurologicalStatus: 'Normal',
  associatedConditions: 'None', status: 'Active',
  phone: '0721123456', address: 'Str. Test',
  assignedNurse: 'Ana Pop', notes: '', createdAt: new Date(), ...overrides
});

const seedPatients: Patient[] = [
  makePatient({ id: 'p1', status: 'Active',   firstName: 'Maria',   assignedNurse: 'Ana Pop' }),
  makePatient({ id: 'p2', status: 'Active',   firstName: 'Ion',     assignedNurse: 'Ana Pop',    diagnosis: 'Diabetes' }),
  makePatient({ id: 'p3', status: 'Critical', firstName: 'Vasile',  assignedNurse: 'Elena Marin' }),
];

describe('PatientManagement', () => {
  let component: PatientManagement;
  let fixture: ComponentFixture<PatientManagement>;
  let patientService: jasmine.SpyObj<PatientService>;
  let authService: AuthService;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    patientService = jasmine.createSpyObj('PatientService', [
      'getLastVitalTime', 'getTodayCheckIn'
    ], {
      patients: signal(seedPatients).asReadonly(),
    });
    patientService.getLastVitalTime.and.returnValue(null);
    patientService.getTodayCheckIn.and.returnValue(null);

    router = jasmine.createSpyObj('Router', ['navigate'], {
      events: new Subject()
    });
    router.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [PatientManagement, NoopAnimationsModule],
      providers: [
        { provide: PatientService, useValue: patientService },
        { provide: Router, useValue: router },
        AuthService,
        MessageService
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientManagement);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('should default to table view', () => {
    expect(component.viewMode()).toBe('table');
  });

  it('should switch to charts view', () => {
    component.viewMode.set('charts');
    expect(component.viewMode()).toBe('charts');
  });

  describe('nurse view', () => {
    beforeEach(() => {
      authService.setRole('nurse', 'Ana Pop');
      fixture.detectChanges();
    });

    it('should only show patients assigned to the logged-in nurse', () => {
      expect(component.filteredPatients().length).toBe(2);
      component.filteredPatients().forEach(p =>
        expect(p.assignedNurse).toBe('Ana Pop')
      );
    });

    it('should not show patients of other nurses', () => {
      const names = component.filteredPatients().map(p => p.firstName);
      expect(names).not.toContain('Vasile');
    });
  });

  describe('admin view', () => {
    beforeEach(() => {
      authService.setRole('admin', 'Admin User');
      fixture.detectChanges();
    });

    it('should show all patients', () => {
      expect(component.filteredPatients().length).toBe(3);
    });
  });

  describe('stats for nurse Ana Pop', () => {
    beforeEach(() => authService.setRole('nurse', 'Ana Pop'));

    it('activeCount() counts only nurse\'s active patients', () => {
      expect(component.activeCount()).toBe(2);
    });

    it('criticalCount() counts only nurse\'s critical patients', () => {
      expect(component.criticalCount()).toBe(0);
    });
  });

  describe('stats for admin', () => {
    beforeEach(() => authService.setRole('admin', 'Admin User'));

    it('activeCount() counts all active patients', () => {
      expect(component.activeCount()).toBe(2);
    });

    it('criticalCount() counts all critical patients', () => {
      expect(component.criticalCount()).toBe(1);
    });
  });

  describe('donutData()', () => {
    it('should have 2 labels', () => {
      expect(component.donutData().labels.length).toBe(2);
    });

    it('should use green for Normal and red for Critical', () => {
      const colors = component.donutData().datasets[0].backgroundColor as string[];
      expect(colors[0]).toBe('#5f8d5f');
      expect(colors[1]).toBe('#b92b27');
    });
  });

  describe('symptomsData()', () => {
    it('should have 5 symptom labels', () => {
      expect(component.symptomsData().labels.length).toBe(5);
    });

    it('should count symptoms from today check-ins of visible patients', () => {
      authService.setRole('nurse', 'Ana Pop');
      patientService.getTodayCheckIn.and.callFake((id: string) => {
        if (id === 'p1') return {
          id: 'ci-1', patientId: 'p1', date: new Date(),
          painLevel: 5, mood: 'Calm', symptoms: ['Fatigue'],
          comments: '', lastModified: new Date()
        };
        return null;
      });
      const data = component.symptomsData();
      const fatigueIdx = data.labels.indexOf('Fatigue');
      expect(data.datasets[0].data[fatigueIdx]).toBe(1);
    });
  });

  describe('search (admin)', () => {
    beforeEach(() => authService.setRole('admin', 'Admin'));

    it('shows all when search is empty', () => {
      expect(component.filteredPatients().length).toBe(3);
    });

    it('filters by name', () => {
      component.searchQuery.set('maria');
      expect(component.filteredPatients().length).toBe(1);
    });

    it('filters by diagnosis', () => {
      component.searchQuery.set('diabetes');
      expect(component.filteredPatients().length).toBe(1);
    });

    it('returns empty for no match', () => {
      component.searchQuery.set('zzznomatch');
      expect(component.filteredPatients().length).toBe(0);
    });
  });

  describe('getSeverity()', () => {
    it('Active → success',  () => expect(component.getSeverity('Active')).toBe('success'));
    it('Inactive → warn',   () => expect(component.getSeverity('Inactive')).toBe('warn'));
    it('Critical → danger', () => expect(component.getSeverity('Critical')).toBe('danger'));
  });

  it('openDetail() navigates to patient detail route', () => {
    component.openDetail(seedPatients[0]);
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/patient-management', 'p1']);
  });
});
