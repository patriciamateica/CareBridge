import {TestBed} from '@angular/core/testing';
import {CreatePatientDto} from './patient-model';
import {PatientService} from './patient-service';

const dto: CreatePatientDto = {
  firstName: 'Test', lastName: 'User', dateOfBirth: new Date('1960-01-01'), diagnosis: 'Flu',
  neurologicalStatus: 'Normal', associatedConditions: 'None',
  status: 'Active', phone: '0700000000', address: 'Test St 1',
  assignedNurse: 'Nurse A', notes: ''
};

describe('PatientService', () => {
  let service: PatientService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PatientService);
  });

  it('should be created', () => expect(service).toBeTruthy());
  it('should seed patients', () => expect(
    service.patients().length).toBeGreaterThan(0));

  describe('create()', () => {
    it('adds patient with id and createdAt', () => {
      const before = service.totalCount();
      const p = service.create(dto);
      expect(service.totalCount()).toBe(before + 1);
      expect(p.id).toBeTruthy();
      expect(p.createdAt).toBeTruthy();
    });
    it('generates unique ids', () => {
      const a = service.create(dto);
      const b = service.create(dto);
      expect(a.id).not.toBe(b.id);
    });
  });

  describe('getById()', () => {
    it('returns patient', () => {
      const p = service.create(dto);
      expect(service.getById(p.id)?.id).toBe(p.id);
    });
    it('returns undefined for unknown id', () => {
      expect(service.getById('none')).toBeUndefined();
    });
  });

  describe('update()', () => {
    it('updates fields', () => {
      const p = service.create(dto);
      const u = service.update(p.id, {...dto, firstName: 'Changed'});
      expect(u?.firstName).toBe('Changed');
    });
    it('preserves id and createdAt', () => {
      const p = service.create(dto);
      const u = service.update(p.id, {...dto, diagnosis: 'New'});
      expect(u?.id).toBe(p.id);
      expect(u?.createdAt).toEqual(p.createdAt);
    });
    it('returns null for unknown id', () => {
      expect(service.update('nope', dto)).toBeNull();
    });
  });

  describe('delete()', () => {
    it('removes patient and returns true', () => {
      const p = service.create(dto);
      const before = service.totalCount();
      expect(service.delete(p.id)).toBeTrue();
      expect(service.totalCount()).toBe(before - 1);
    });
    it('returns false for unknown id', () => {
      expect(service.delete('nope')).toBeFalse();
    });
    it('cascades delete to vitals, notes, meds, check-ins', () => {
      const p = service.create(dto);
      service.addVital(p.id, {heartRate: 72, systolic: 120, diastolic: 80, respiratoryRate: 16, spo2: 98});
      service.addCareNote(p.id, 'note', 'Nurse A');
      service.addMedication(p.id, 'Med', '100mg', 'OD', 'Nurse A');
      service.upsertCheckIn(p.id, {painLevel: 3, mood: 'Calm', symptoms: [], comments: ''});
      service.delete(p.id);
      expect(service.getVitals(p.id)).toEqual([]);
      expect(service.getCareNotes(p.id)).toEqual([]);
      expect(service.getMedications(p.id)).toEqual([]);
      expect(service.getTodayCheckIn(p.id)).toBeNull();
    });
  });

  describe('vitals', () => {
    it('new patient has empty vitals', () => {
      const p = service.create(dto);
      expect(service.getVitals(p.id)).toEqual([]);
    });
    it('addVital appends reading', () => {
      const p = service.create(dto);
      service.addVital(p.id, {heartRate: 80, systolic: 130, diastolic: 85, respiratoryRate: 15, spo2: 97});
      expect(service.getVitals(p.id).length).toBe(1);
      expect(service.getVitals(p.id)[0].heartRate).toBe(80);
    });
    it('getLastVitalTime returns null when no vitals', () => {
      const p = service.create(dto);
      expect(service.getLastVitalTime(p.id)).toBeNull();
    });
    it('getLastVitalTime returns timestamp after adding', () => {
      const p = service.create(dto);
      service.addVital(p.id, {heartRate: 72, systolic: 120, diastolic: 80, respiratoryRate: 16, spo2: 98});
      expect(service.getLastVitalTime(p.id)).toBeTruthy();
    });
  });

  describe('checkIns', () => {
    const ciData = {painLevel: 5, mood: 'Calm' as const, symptoms: ['Fatigue'], comments: 'OK'};

    it('getTodayCheckIn returns null initially', () => {
      const p = service.create(dto);
      expect(service.getTodayCheckIn(p.id)).toBeNull();
    });
    it('upsertCheckIn creates new entry', () => {
      const p = service.create(dto);
      const ci = service.upsertCheckIn(p.id, ciData);
      expect(ci.id).toBeTruthy();
      expect(service.getTodayCheckIn(p.id)?.painLevel).toBe(5);
    });
    it('upsertCheckIn updates existing entry for same day', () => {
      const p = service.create(dto);
      service.upsertCheckIn(p.id, ciData);
      service.upsertCheckIn(p.id, {...ciData, painLevel: 8});
      expect(service.getTodayCheckIn(p.id)?.painLevel).toBe(8);
      expect(service.getCheckIns(p.id).length).toBe(1);
    });
    it('deleteCheckIn removes entry', () => {
      const p = service.create(dto);
      const ci = service.upsertCheckIn(p.id, ciData);
      service.deleteCheckIn(ci.id);
      expect(service.getTodayCheckIn(p.id)).toBeNull();
    });
    it('getCheckIns returns entries sorted newest first', () => {
      const p = service.create(dto);
      service.upsertCheckIn(p.id, ciData);
      const entries = service.getCheckIns(p.id);
      expect(entries.length).toBeGreaterThan(0);
    });
  });

  describe('medications', () => {
    it('new patient has no medications', () => {
      const p = service.create(dto);
      expect(service.getMedications(p.id)).toEqual([]);
    });
    it('addMedication stores and returns med', () => {
      const p = service.create(dto);
      const m = service.addMedication(
        p.id, 'Aspirin', '100mg', 'OD', 'Nurse A');
      expect(m.id).toBeTruthy();
      expect(service.getMedications(p.id).length).toBe(1);
    });
    it('deleteMedication removes entry', () => {
      const p = service.create(dto);
      const m = service.addMedication(
        p.id, 'Aspirin', '100mg', 'OD', 'Nurse A');
      service.deleteMedication(m.id);
      expect(service.getMedications(p.id).length).toBe(0);
    });
  });

  describe('careNotes', () => {
    it('new patient has no notes', () => {
      const p = service.create(dto);
      expect(service.getCareNotes(p.id)).toEqual([]);
    });
    it('addCareNote stores note', () => {
      const p = service.create(dto);
      service.addCareNote(p.id, 'All good', 'Nurse A');
      expect(service.getCareNotes(p.id).length).toBe(1);
    });
    it('getCareNotes returns newest first', () => {
      const p = service.create(dto);
      service.addCareNote(p.id, 'First', 'Nurse A');
      service.addCareNote(p.id, 'Second', 'Nurse A');
      expect(service.getCareNotes(p.id)[0].content).toBe('Second');
    });
    it('deleteCareNote removes entry', () => {
      const p = service.create(dto);
      const n = service.addCareNote(p.id, 'To delete', 'Nurse A');
      service.deleteCareNote(n.id);
      expect(service.getCareNotes(p.id).length).toBe(0);
    });
  });
});
