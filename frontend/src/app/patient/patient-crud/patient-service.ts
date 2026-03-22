import {Injectable, signal, computed} from '@angular/core';
import {
  Patient, CreatePatientDto, UpdatePatientDto,
  VitalReading, DailyCheckIn, Medication, CareNote, MoodStatus
} from '../patient-crud/patient-model';

let _id = 1;
const uid = () => `id-${_id++}`;

function daysAgo(n: number): Date {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d;
}

function hoursAgo(h: number): Date {
  return new Date(Date.now() - h * 3_600_000);
}

function seedVitals(id: string, baseHr: number, baseSys: number, baseSpo2: number): VitalReading[] {
  return Array.from({length: 8}, (_, i) => ({
    timestamp: daysAgo(7 - i),
    heartRate: baseHr + Math.round((Math.random() - 0.5) * 10),
    systolic: baseSys + Math.round((Math.random() - 0.5) * 12),
    diastolic: Math.round(baseSys * 0.63 + (Math.random() - 0.5) * 8),
    respiratoryRate: 14 + Math.round((Math.random() - 0.5) * 4),
    spo2: Math.min(100, baseSpo2 + Math.round((Math.random() - 0.5) * 3)),
  }));
}

@Injectable({providedIn: 'root'})
export class PatientService {
  private readonly _patients = signal<Patient[]>([]);
  private readonly _vitals = signal<Map<string, VitalReading[]>>(new Map());
  private readonly _checkIns = signal<DailyCheckIn[]>([]);
  private readonly _medications = signal<Medication[]>([]);
  private readonly _careNotes = signal<CareNote[]>([]);

  readonly patients = this._patients.asReadonly();
  readonly totalCount = computed(() => this._patients().length);

  constructor() {
    this.seed();
  }

  getById(id: string): Patient | undefined {
    return this._patients().find(p => p.id === id);
  }

  create(dto: CreatePatientDto): Patient {
    const p: Patient = {...dto, id: uid(), createdAt: new Date()};
    this._patients.update(l => [...l, p]);
    this._vitals.update(m => {
      m.set(p.id, []);
      return new Map(m);
    });
    return p;
  }

  update(id: string, dto: UpdatePatientDto): Patient | null {
    let result: Patient | null = null;
    this._patients.update(l => l.map(p => {
      if (p.id !== id) return p;
      result = {...p, ...dto};
      return result;
    }));
    return result;
  }

  delete(id: string): boolean {
    const before = this._patients().length;
    this._patients.update(l => l.filter(p => p.id !== id));
    this._vitals.update(m => {
      m.delete(id);
      return new Map(m);
    });
    this._checkIns.update(l => l.filter(c => c.patientId !== id));
    this._medications.update(l => l.filter(m => m.patientId !== id));
    this._careNotes.update(l => l.filter(n => n.patientId !== id));
    return this._patients().length < before;
  }

  getVitals(patientId: string): VitalReading[] {
    return this._vitals().get(patientId) ?? [];
  }

  addVital(patientId: string, v: Omit<VitalReading, 'timestamp'>): void {
    this._vitals.update(m => {
      m.set(patientId, [...(m.get(patientId) ?? []), {...v, timestamp: new Date()}]);
      return new Map(m);
    });
  }

  getLastVitalTime(patientId: string): Date | null {
    const v = this.getVitals(patientId);
    return v.length ? v[v.length - 1].timestamp : null;
  }

  getTodayCheckIn(patientId: string): DailyCheckIn | null {
    const today = new Date().toDateString();
    return this._checkIns().find(c => c.patientId === patientId && new Date(c.date).toDateString()
      === today) ?? null;
  }

  getCheckIns(patientId: string): DailyCheckIn[] {
    return this._checkIns()
      .filter(c => c.patientId === patientId)
      .sort((a, b) => b.date.getTime() - a.date.getTime());
  }

  upsertCheckIn(patientId: string, data: Omit<DailyCheckIn, 'id' | 'patientId' | 'date' | 'lastModified'>):
    DailyCheckIn {
    const existing = this.getTodayCheckIn(patientId);
    if (existing) {
      const updated = {...existing, ...data, lastModified: new Date()};
      this._checkIns.update(l => l.map(c => c.id === existing.id ? updated : c));
      return updated;
    }
    const newEntry: DailyCheckIn = {...data, id: uid(), patientId, date: new Date(), lastModified: new Date()};
    this._checkIns.update(l => [newEntry, ...l]);
    return newEntry;
  }

  deleteCheckIn(id: string): void {
    this._checkIns.update(l => l.filter(c => c.id !== id));
  }

  getMedications(patientId: string): Medication[] {
    return this._medications().filter(m => m.patientId === patientId);
  }

  addMedication(patientId: string, name: string, dose: string, timing: string, nurseName: string): Medication {
    const m: Medication = {id: uid(), patientId, name, dose, timing, addedBy: nurseName, addedAt: new Date()};
    this._medications.update(l => [...l, m]);
    return m;
  }

  deleteMedication(id: string): void {
    this._medications.update(l => l.filter(m => m.id !== id));
  }

  getCareNotes(patientId: string): CareNote[] {
    return this._careNotes()
      .filter(n => n.patientId === patientId)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
  }

  addCareNote(patientId: string, content: string, nurseName: string): CareNote {
    const n: CareNote = {id: uid(), patientId, content, nurseName, createdAt: new Date()};
    this._careNotes.update(l => [n, ...l]);
    return n;
  }

  deleteCareNote(id: string): void {
    this._careNotes.update(l => l.filter(n => n.id !== id));
  }

  private seed() {
    const patients: Patient[] = [
      {
        id: uid(),
        firstName: 'Maria',
        lastName: 'Vaida',
        dateOfBirth: new Date('1945-03-12'),
        diagnosis: 'Trauma-Induced Paraplegia (Spinal Cord Injury, Oct 2025)',
        neurologicalStatus: 'No sensation or motor function below the waist.',
        associatedConditions: 'Chronic neuropathic pain, neurogenic bladder, and neurogenic bowel.',
        status: 'Critical',
        phone: '0721123456',
        address: 'Str. Mihai Eminescu 14, Cluj-Napoca',
        assignedNurse: 'Ana Pop',
        notes: '',
        createdAt: daysAgo(30)
      },
      {
        id: uid(),
        firstName: 'Ion',
        lastName: 'Popa',
        dateOfBirth: new Date('1938-07-22'),
        diagnosis: 'Type 2 Diabetes Mellitus',
        neurologicalStatus: 'Peripheral neuropathy in lower limbs.',
        associatedConditions: 'Hypertension, Chronic Kidney Disease Stage 2.',
        status: 'Active',
        phone: '0733987654',
        address: 'Bd. Unirii 5, Bucharest',
        assignedNurse: 'Elena Marin',
        notes: 'Insulin injections twice daily.',
        createdAt: daysAgo(20)
      },
      {
        id: uid(),
        firstName: 'Gheorghe',
        lastName: 'Stan',
        dateOfBirth: new Date('1950-11-01'),
        diagnosis: 'COPD Stage III',
        neurologicalStatus: 'No neurological deficits.',
        associatedConditions: 'Chronic bronchitis, Pulmonary hypertension.',
        status: 'Inactive',
        phone: '0744321321',
        address: 'Str. Libertății 3, Timișoara',
        assignedNurse: 'Maria Dumitrescu',
        notes: 'Oxygen therapy at home.',
        createdAt: daysAgo(45)
      },
      {
        id: uid(),
        firstName: 'Elena',
        lastName: 'Dumitru',
        dateOfBirth: new Date('1942-05-30'),
        diagnosis: 'Osteoporosis with vertebral fractures',
        neurologicalStatus: 'Mild lumbar radiculopathy.',
        associatedConditions: 'Vitamin D deficiency, Mild depression.',
        status: 'Active',
        phone: '0755234567',
        address: 'Calea Victoriei 22, Bucharest',
        assignedNurse: 'Ana Pop',
        notes: 'Weekly physiotherapy sessions.',
        createdAt: daysAgo(15)
      },
      {
        id: uid(),
        firstName: 'Vasile',
        lastName: 'Munteanu',
        dateOfBirth: new Date('1955-09-15'),
        diagnosis: 'Congestive Heart Failure (EF 35%)',
        neurologicalStatus: 'Alert and oriented, no focal deficits.',
        associatedConditions: 'Atrial fibrillation, Hypertension, CKD Stage 3.',
        status: 'Critical',
        phone: '0766345678',
        address: 'Str. Republicii 10, Iași',
        assignedNurse: 'Elena Marin',
        notes: 'Daily weight monitoring. Fluid restriction 1.5L/day.',
        createdAt: daysAgo(10)
      },
      {
        id: uid(),
        firstName: 'Adriana',
        lastName: 'Florescu',
        dateOfBirth: new Date('1960-02-18'),
        diagnosis: 'Rheumatoid Arthritis',
        neurologicalStatus: 'No neurological involvement.',
        associatedConditions: 'Anemia, Osteopenia.',
        status: 'Active',
        phone: '0777456789',
        address: 'Str. Câmpului 7, Brașov',
        assignedNurse: 'Ana Pop',
        notes: 'Anti-inflammatory medication in the morning.',
        createdAt: daysAgo(8)
      },
    ];

    const vitalsMap = new Map<string, VitalReading[]>();
    const hrBases = [103, 72, 68, 76, 92, 70];
    const sysBases = [111, 145, 125, 118, 158, 118];
    const spo2Bases = [92, 97, 89, 98, 94, 98];
    patients.forEach((p, i) => vitalsMap.set(
      p.id, seedVitals(p.id, hrBases[i], sysBases[i], spo2Bases[i])));

    const meds: Medication[] = [
      {
        id: uid(),
        patientId: patients[0].id,
        name: 'Medicine 1 (500g, BID)',
        dose: '500g',
        timing: 'BID',
        addedBy: 'Ana Pop',
        addedAt: daysAgo(10)
      },
      {
        id: uid(),
        patientId: patients[0].id,
        name: 'Medicine 2 (500g, BID)',
        dose: '500g',
        timing: 'BID',
        addedBy: 'Ana Pop',
        addedAt: daysAgo(10)
      },
      {
        id: uid(),
        patientId: patients[0].id,
        name: 'Medicine 3 (500g, BID)',
        dose: '500g',
        timing: 'BID',
        addedBy: 'Ana Pop',
        addedAt: daysAgo(5)
      },
      {
        id: uid(),
        patientId: patients[1].id,
        name: 'Metformin (1000mg, BID)',
        dose: '1000mg',
        timing: 'BID',
        addedBy: 'Elena Marin',
        addedAt: daysAgo(20)
      },
      {
        id: uid(),
        patientId: patients[4].id,
        name: 'Furosemide (40mg, OD)',
        dose: '40mg',
        timing: 'OD',
        addedBy: 'Elena Marin',
        addedAt: daysAgo(8)
      },
    ];

    const notes: CareNote[] = [
      {
        id: uid(),
        patientId: patients[0].id,
        content: 'Patient reports consistent BP readings',
        nurseName: 'Ana Pop',
        createdAt: daysAgo(1)
      },
      {
        id: uid(),
        patientId: patients[0].id,
        content: 'Patient reports consistent BP readings',
        nurseName: 'Ana Pop',
        createdAt: daysAgo(2)
      },
      {
        id: uid(),
        patientId: patients[0].id,
        content: 'Patient reports consistent BP readings',
        nurseName: 'Ana Pop',
        createdAt: daysAgo(3)
      },
      {
        id: uid(),
        patientId: patients[1].id,
        content: 'Blood glucose stable at 120 mg/dL',
        nurseName: 'Elena Marin',
        createdAt: hoursAgo(5)
      },
    ];

    const checkIns: DailyCheckIn[] = [
      {
        id: uid(),
        patientId: patients[0].id,
        date: new Date(),
        painLevel: 9,
        mood: 'Anxious',
        symptoms: ['Fatigue', 'Shortness of Breath'],
        comments: 'Experienced major pain...',
        lastModified: new Date()
      },
    ];

    this._patients.set(patients);
    this._vitals.set(vitalsMap);
    this._medications.set(meds);
    this._careNotes.set(notes);
    this._checkIns.set(checkIns);
  }
}
