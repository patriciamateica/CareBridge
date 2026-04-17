import { Injectable, WritableSignal, computed, inject, signal } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { firstValueFrom, Subscription } from 'rxjs';

import { GET_USERS, REGISTER_USER, UPDATE_USER_STATUS, USER_SUBSCRIPTION } from './user.graphql';
import { CREATE_ROSTER, DELETE_ROSTER, GET_ROSTERS, ROSTER_SUBSCRIPTION, UPDATE_ROSTER_STATUS } from './roster.graphql';
import { CREATE_PATIENT_DETAILS, GET_ALL_PATIENT_DETAILS, PATIENT_DETAILS_SUBSCRIPTION, UPDATE_PATIENT_DIAGNOSIS } from './patient-details.graphql';
import { GET_TASKS, TASK_SUBSCRIPTION } from './task.graphql';
import { GET_VITALS_BY_PATIENT, VITALS_SUBSCRIPTION } from './vitals.graphql';
import { GET_HEALTH_STATUSES, HEALTH_STATUS_SUBSCRIPTION, CREATE_HEALTH_STATUS } from './health-status.graphql';
import { GET_CARE_NOTES, CARE_NOTE_SUBSCRIPTION, CREATE_CARE_NOTE } from './care-notes.graphql';
import { GET_PRESCRIPTIONS, PRESCRIPTION_SUBSCRIPTION, CREATE_PRESCRIPTION } from './prescription.graphql';
import { CareNote, CreatePatientDto, DailyCheckIn, Medication, MoodStatus, Patient, PatientStatus, UpdatePatientDto, VitalReading } from '../patient/patient-crud/patient-model';

type GqlUser = {
  id: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  userStatus?: 'PENDING_ACTIVATION' | 'ACTIVE' | 'INACTIVE';
  phoneNumber?: string | number;
  dateOfBirth?: string;
  residentialAddress?: string;
};

type GqlRoster = {
  id: string;
  patientId: string;
  nurseId: string;
  status?: 'PENDING' | 'ACTIVE' | 'DECLINED' | 'INACTIVE';
};

type GqlPatientDetails = {
  id: string;
  userId: string;
  primaryDiagnosis?: string;
  diagnostics?: string[];
};

type GqlTask = {
  id: string;
  title?: string;
  taskType?: string;
  neededBy?: string;
  status?: string;
  patientId: string;
};

type GqlVitals = {
  id: string;
  heartRate?: number;
  bloodPressure?: number;
  respiratoryRate?: number;
  spO2?: number;
  timestamp?: string;
};

type GqlHealthStatus = {
  id: string;
  painScale?: number;
  mood?: MoodStatus;
  symptoms?: string[];
  notes?: string;
  timestamp?: string;
};

type GqlCareNote = {
  id: string;
  content?: string;
  timestamp?: string;
  nurseId?: string;
};

type GqlPrescription = {
  id: string;
  name?: string;
  dose?: string;
  timing?: string;
  nurseId?: string;
};

@Injectable({ providedIn: 'root' })
export class RealtimeDataService {
  private readonly apollo = inject(Apollo);

  private readonly _users = signal<GqlUser[]>([]);
  private readonly _rosters = signal<GqlRoster[]>([]);
  private readonly _patientDetails = signal<GqlPatientDetails[]>([]);
  private readonly _tasks = signal<GqlTask[]>([]);

  private readonly _vitalsByPatient = signal<Record<string, GqlVitals[]>>({});
  private readonly _healthByPatient = signal<Record<string, GqlHealthStatus[]>>({});
  private readonly _notesByPatient = signal<Record<string, GqlCareNote[]>>({});
  private readonly _prescriptionsByPatient = signal<Record<string, GqlPrescription[]>>({});

  private readonly patientLiveStreams = new Map<string, Subscription[]>();

  readonly patients = computed<Patient[]>(() => {
    const users = this._users();
    const usersById = new Map(users.map((u) => [u.id, u]));
    const detailByUserId = new Map(this._patientDetails().map((d) => [d.userId, d]));

    return users
      .filter((u) => (u.role ?? '').toUpperCase() === 'PATIENT' && (!u.userStatus || u.userStatus === 'ACTIVE'))
      .map((user) => {
        const detail = detailByUserId.get(user.id);
        const roster = this._rosters().find((r) => r.patientId === user.id);
        const nurse = usersById.get(roster?.nurseId ?? '');
        const latestCheckIn = this.getHealthRows(user.id)[0];

        return {
          id: user.id,
          firstName: user.firstName ?? 'Unknown',
          lastName: user.lastName ?? 'Patient',
          dateOfBirth: this.toDate(user.dateOfBirth),
          diagnosis: detail?.primaryDiagnosis ?? 'N/A',
          neurologicalStatus: detail?.diagnostics?.join(', ') ?? 'N/A',
          associatedConditions: detail?.diagnostics?.join(', ') ?? 'N/A',
          status: this.resolveStatus(roster?.status, latestCheckIn?.painScale),
          phone: String(user.phoneNumber ?? ''),
          address: user.residentialAddress ?? 'N/A',
          assignedNurse: nurse ? `${nurse.firstName ?? ''} ${nurse.lastName ?? ''}`.trim() : 'Unassigned',
          notes: latestCheckIn?.notes ?? '',
          createdAt: new Date(),
        };
      });
  });

  readonly tasks = computed(() => {
    const patientNames = new Map(
      this._users()
        .filter((u) => (u.role ?? '').toUpperCase() === 'PATIENT')
        .map((u) => [u.id, `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim()])
    );

    return this._tasks().map((task) => ({
      id: task.id,
      time: this.toTime(task.neededBy),
      type: task.taskType ?? 'Task',
      patientId: task.patientId,
      patientName: patientNames.get(task.patientId) ?? 'Unknown patient',
      status: task.status ?? 'OPEN',
      title: task.title ?? 'Task',
    }));
  });

  constructor() {
    this.initRootStreams();
  }

  ensurePatientLiveData(patientId: string): void {
    if (!patientId || this.patientLiveStreams.has(patientId)) return;

    const streams: Subscription[] = [];

    streams.push(
      this.apollo.watchQuery<{ getVitalsByPatientId: GqlVitals[] }>({
        query: GET_VITALS_BY_PATIENT,
        variables: { patientId },
        fetchPolicy: 'network-only',
      }).valueChanges.subscribe(({ data }) => {
        const vitals = this.normalizeRows<GqlVitals>(data?.getVitalsByPatientId);
        this._vitalsByPatient.update((s) => ({ ...s, [patientId]: vitals }));
      })
    );

    streams.push(
      this.apollo.watchQuery<{ getHealthStatusesByPatientId: GqlHealthStatus[] }>({
        query: GET_HEALTH_STATUSES,
        variables: { patientId },
        fetchPolicy: 'network-only',
      }).valueChanges.subscribe(({ data }) => {
        const statuses = this.normalizeRows<GqlHealthStatus>(data?.getHealthStatusesByPatientId);
        this._healthByPatient.update((s) => ({ ...s, [patientId]: statuses }));
      })
    );

    streams.push(
      this.apollo.watchQuery<{ getCareNotesByPatientId: GqlCareNote[] }>({
        query: GET_CARE_NOTES,
        variables: { patientId },
        fetchPolicy: 'network-only',
      }).valueChanges.subscribe(({ data }) => {
        const notes = this.normalizeRows<GqlCareNote>(data?.getCareNotesByPatientId);
        this._notesByPatient.update((s) => ({ ...s, [patientId]: notes }));
      })
    );

    streams.push(
      this.apollo.watchQuery<{ getPrescriptionsByPatientId: GqlPrescription[] }>({
        query: GET_PRESCRIPTIONS,
        variables: { patientId },
        fetchPolicy: 'network-only',
      }).valueChanges.subscribe(({ data }) => {
        const prescriptions = this.normalizeRows<GqlPrescription>(data?.getPrescriptionsByPatientId);
        this._prescriptionsByPatient.update((s) => ({ ...s, [patientId]: prescriptions }));
      })
    );

    streams.push(
      this.apollo.subscribe<{ onVitalsAdded: GqlVitals }>({ query: VITALS_SUBSCRIPTION, variables: { patientId } })
        .subscribe(({ data }) => {
          if (!data?.onVitalsAdded?.id) return;
          this.upsertByPatient(this._vitalsByPatient, patientId, data.onVitalsAdded);
        })
    );

    streams.push(
      this.apollo.subscribe<{ onHealthStatusAdded: GqlHealthStatus }>({ query: HEALTH_STATUS_SUBSCRIPTION, variables: { patientId } })
        .subscribe(({ data }) => {
          if (!data?.onHealthStatusAdded?.id) return;
          this.upsertByPatient(this._healthByPatient, patientId, data.onHealthStatusAdded);
        })
    );

    streams.push(
      this.apollo.subscribe<{ onCareNoteAdded: GqlCareNote }>({ query: CARE_NOTE_SUBSCRIPTION, variables: { patientId } })
        .subscribe(({ data }) => {
          if (!data?.onCareNoteAdded?.id) return;
          this.upsertByPatient(this._notesByPatient, patientId, data.onCareNoteAdded);
        })
    );

    streams.push(
      this.apollo.subscribe<{ onPrescriptionCreated: GqlPrescription }>({ query: PRESCRIPTION_SUBSCRIPTION, variables: { patientId } })
        .subscribe(({ data }) => {
          if (!data?.onPrescriptionCreated?.id) return;
          this.upsertByPatient(this._prescriptionsByPatient, patientId, data.onPrescriptionCreated);
        })
    );

    this.patientLiveStreams.set(patientId, streams);
  }

  getById(id: string): Patient | undefined {
    this.ensurePatientLiveData(id);
    return this.patients().find((p) => p.id === id);
  }

  getVitals(patientId: string): VitalReading[] {
    this.ensurePatientLiveData(patientId);
    return [...(this._vitalsByPatient()[patientId] ?? [])]
      .sort((a, b) => this.toDate(a.timestamp).getTime() - this.toDate(b.timestamp).getTime())
      .map((row) => {
        const systolic = Number(row.bloodPressure ?? 0);
        return {
          timestamp: this.toDate(row.timestamp),
          heartRate: Number(row.heartRate ?? 0),
          systolic,
          diastolic: Math.max(40, Math.round(systolic * 0.67)),
          respiratoryRate: Number(row.respiratoryRate ?? 0),
          spo2: Number(row.spO2 ?? 0),
        };
      });
  }

  getLastVitalTime(patientId: string): Date | null {
    const vitals = this.getVitals(patientId);
    return vitals.length ? vitals[vitals.length - 1].timestamp : null;
  }

  getTodayCheckIn(patientId: string): DailyCheckIn | null {
    const today = new Date().toDateString();
    return this.getCheckIns(patientId).find((c) => c.date.toDateString() === today) ?? null;
  }

  getCheckIns(patientId: string): DailyCheckIn[] {
    this.ensurePatientLiveData(patientId);
    return this.getHealthRows(patientId)
      .map((row) => ({
        id: row.id,
        patientId,
        date: this.toDate(row.timestamp),
        painLevel: Number(row.painScale ?? 0),
        mood: (row.mood ?? 'Calm') as MoodStatus,
        symptoms: row.symptoms ?? [],
        comments: row.notes ?? '',
        lastModified: this.toDate(row.timestamp),
      }))
      .sort((a, b) => b.date.getTime() - a.date.getTime());
  }

  getMedications(patientId: string): Medication[] {
    this.ensurePatientLiveData(patientId);
    const usersById = new Map(this._users().map((u) => [u.id, `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim()]));
    return [...(this._prescriptionsByPatient()[patientId] ?? [])].map((row) => ({
      id: row.id,
      patientId,
      name: row.name ?? 'Medication',
      dose: row.dose ?? '-',
      timing: row.timing ?? '-',
      addedBy: usersById.get(row.nurseId ?? '') ?? 'Nurse',
      addedAt: new Date(),
    }));
  }

  getCareNotes(patientId: string): CareNote[] {
    this.ensurePatientLiveData(patientId);
    const usersById = new Map(this._users().map((u) => [u.id, `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim()]));
    return [...(this._notesByPatient()[patientId] ?? [])]
      .map((row) => ({
        id: row.id,
        patientId,
        content: row.content ?? '',
        createdAt: this.toDate(row.timestamp),
        nurseName: usersById.get(row.nurseId ?? '') ?? 'Nurse',
      }))
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
  }

  async upsertCheckIn(patientId: string, data: Omit<DailyCheckIn, 'id' | 'patientId' | 'date' | 'lastModified'>): Promise<void> {
    await firstValueFrom(this.apollo.mutate({
      mutation: CREATE_HEALTH_STATUS,
      variables: {
        patientId,
        painScale: data.painLevel,
        mood: data.mood,
        symptoms: data.symptoms,
        notes: data.comments,
        timestamp: new Date().toISOString().slice(0, 10),
      },
    }));
  }

  async addCareNote(patientId: string, content: string): Promise<void> {
    const nurseId = this.resolveNurseId(patientId);
    if (!nurseId) throw new Error('No nurseId available for this patient');
    await firstValueFrom(this.apollo.mutate({
      mutation: CREATE_CARE_NOTE,
      variables: { patientId, nurseId, content, timestamp: new Date().toISOString() },
    }));
  }

  async addMedication(patientId: string, name: string, dose: string, timing: string): Promise<void> {
    const nurseId = this.resolveNurseId(patientId);
    if (!nurseId) throw new Error('No nurseId available for this patient');
    await firstValueFrom(this.apollo.mutate({
      mutation: CREATE_PRESCRIPTION,
      variables: { patientId, nurseId, name, dose, timing },
    }));
  }

  async createPatient(dto: CreatePatientDto): Promise<void> {
    const nurseId = this.findNurseIdByName(dto.assignedNurse) ?? this.resolveNurseId('');
    if (!nurseId) {
      throw new Error('No nurse user exists in backend. Seed nurse users first.');
    }

    const email = this.generatePatientEmail(dto.firstName, dto.lastName);
    const registerResult = await firstValueFrom(this.apollo.mutate<{ registerUser: GqlUser }>({
      mutation: REGISTER_USER,
      variables: {
        input: {
          firstName: dto.firstName,
          lastName: dto.lastName,
          email,
          password: 'Carebridge123!',
          phoneNumber: this.normalizePhone(dto.phone),
        }
      },
    }));

    const createdUser = registerResult.data?.registerUser;
    if (!createdUser?.id) {
      throw new Error('Backend did not return the created patient user.');
    }
    this._users.update((rows) => this.upsertById(rows, createdUser));

    const diagnostics = [dto.neurologicalStatus, dto.associatedConditions].filter((v) => !!v?.trim());
    const detailsResult = await firstValueFrom(this.apollo.mutate<{ createPatientDetails: GqlPatientDetails }>({
      mutation: CREATE_PATIENT_DETAILS,
      variables: {
        userId: createdUser.id,
        primaryDiagnosis: dto.diagnosis,
        diagnostics,
        scans: [],
        emergencyContact: dto.phone,
        assignedNurseId: nurseId,
      },
    }));
    const details = detailsResult.data?.createPatientDetails;
    if (details?.id) {
      this._patientDetails.update((rows) => this.upsertById(rows, details));
    }

    const rosterResult = await firstValueFrom(this.apollo.mutate<{ createRoster: GqlRoster }>({
      mutation: CREATE_ROSTER,
      variables: {
        patientId: createdUser.id,
        nurseId,
        status: this.toRosterStatus(dto.status),
      },
    }));
    const roster = rosterResult.data?.createRoster;
    if (roster?.id) {
      this._rosters.update((rows) => this.upsertById(rows, roster));
    }

    if (dto.status === 'Critical') {
      await this.upsertCheckIn(createdUser.id, {
        painLevel: 8,
        mood: 'Anxious',
        symptoms: ['Fatigue'],
        comments: dto.notes || 'Marked as critical from roster form.',
      });
    }
  }

  async updatePatient(patientId: string, dto: UpdatePatientDto): Promise<void> {
    const details = this._patientDetails().find((row) => row.userId === patientId);
    if (details?.id && dto.diagnosis) {
      const diagnosisResult = await firstValueFrom(this.apollo.mutate<{ updatePatientDiagnosis: GqlPatientDetails }>({
        mutation: UPDATE_PATIENT_DIAGNOSIS,
        variables: { id: details.id, primaryDiagnosis: dto.diagnosis },
      }));
      const updatedDetails = diagnosisResult.data?.updatePatientDiagnosis;
      if (updatedDetails?.id) {
        this._patientDetails.update((rows) => this.upsertById(rows, { ...details, ...updatedDetails }));
      }
    }

    const roster = this._rosters().find((row) => row.patientId === patientId);
    const nextNurseId = this.findNurseIdByName(dto.assignedNurse) ?? roster?.nurseId ?? this.resolveNurseId(patientId);
    const targetStatus = this.toRosterStatus(dto.status);

    if (roster && nextNurseId && roster.nurseId !== nextNurseId) {
      await firstValueFrom(this.apollo.mutate<{ deleteRoster: boolean }>({
        mutation: DELETE_ROSTER,
        variables: { id: roster.id },
      }));
      this._rosters.update((rows) => rows.filter((row) => row.id !== roster.id));

      const recreatedRoster = await firstValueFrom(this.apollo.mutate<{ createRoster: GqlRoster }>({
        mutation: CREATE_ROSTER,
        variables: { patientId, nurseId: nextNurseId, status: targetStatus },
      }));
      const created = recreatedRoster.data?.createRoster;
      if (created?.id) {
        this._rosters.update((rows) => this.upsertById(rows, created));
      }
    } else if (roster && roster.status !== targetStatus) {
      const rosterUpdate = await firstValueFrom(this.apollo.mutate<{ updateRosterStatus: GqlRoster }>({
        mutation: UPDATE_ROSTER_STATUS,
        variables: { id: roster.id, status: targetStatus },
      }));
      const updatedRoster = rosterUpdate.data?.updateRosterStatus;
      if (updatedRoster?.id) {
        this._rosters.update((rows) => this.upsertById(rows, updatedRoster));
      }
    }

    const userStatus = dto.status === 'Inactive' ? 'INACTIVE' : 'ACTIVE';
    await firstValueFrom(this.apollo.mutate<{ updateUserStatus: GqlUser }>({
      mutation: UPDATE_USER_STATUS,
      variables: { id: patientId, status: userStatus },
    }));
    this._users.update((rows) => rows.map((row) => row.id === patientId ? { ...row, userStatus } : row));

    if (dto.status === 'Critical') {
      await this.upsertCheckIn(patientId, {
        painLevel: 8,
        mood: 'Anxious',
        symptoms: ['Fatigue'],
        comments: dto.notes || 'Marked as critical from roster form.',
      });
    }
  }

  async deletePatient(patientId: string): Promise<void> {
    const roster = this._rosters().find((row) => row.patientId === patientId);
    if (roster?.id) {
      await firstValueFrom(this.apollo.mutate<{ deleteRoster: boolean }>({
        mutation: DELETE_ROSTER,
        variables: { id: roster.id },
      }));
      this._rosters.update((rows) => rows.filter((row) => row.id !== roster.id));
    }

    await firstValueFrom(this.apollo.mutate<{ updateUserStatus: GqlUser }>({
      mutation: UPDATE_USER_STATUS,
      variables: { id: patientId, status: 'INACTIVE' },
    }));
    this._users.update((rows) => rows.map((row) => row.id === patientId ? { ...row, userStatus: 'INACTIVE' } : row));
  }

  private initRootStreams(): void {
    this.apollo.watchQuery<{ getUsers: GqlUser[] }>({
      query: GET_USERS,
      variables: { page: 0, size: 500 },
      fetchPolicy: 'network-only',
    }).valueChanges.subscribe(({ data }) => {
      this._users.set(this.normalizeRows<GqlUser>(data?.getUsers));
    });

    this.apollo.watchQuery<{ getRosters: GqlRoster[] }>({
      query: GET_ROSTERS,
      variables: { page: 0, size: 500 },
      fetchPolicy: 'network-only',
    }).valueChanges.subscribe(({ data }) => {
      this._rosters.set(this.normalizeRows<GqlRoster>(data?.getRosters));
    });

    this.apollo.watchQuery<{ getAllPatientDetails: GqlPatientDetails[] }>({
      query: GET_ALL_PATIENT_DETAILS,
      variables: { page: 0, size: 500 },
      fetchPolicy: 'network-only',
    }).valueChanges.subscribe(({ data }) => {
      this._patientDetails.set(this.normalizeRows<GqlPatientDetails>(data?.getAllPatientDetails));
    });

    this.apollo.watchQuery<{ getTasks: GqlTask[] }>({
      query: GET_TASKS,
      variables: { page: 0, size: 500 },
      fetchPolicy: 'network-only',
    }).valueChanges.subscribe(({ data }) => {
      this._tasks.set(this.normalizeRows<GqlTask>(data?.getTasks));
    });

    this.apollo.subscribe<{ onUserRegistered: GqlUser }>({ query: USER_SUBSCRIPTION }).subscribe(({ data }) => {
      if (!data?.onUserRegistered?.id) return;
      this._users.update((rows) => this.upsertById(rows, data.onUserRegistered));
    });

    this.apollo.subscribe<{ onRosterUpdated: GqlRoster }>({ query: ROSTER_SUBSCRIPTION }).subscribe(({ data }) => {
      if (!data?.onRosterUpdated?.id) return;
      this._rosters.update((rows) => this.upsertById(rows, data.onRosterUpdated));
    });

    this.apollo.subscribe<{ onPatientDetailsCreated: GqlPatientDetails }>({ query: PATIENT_DETAILS_SUBSCRIPTION }).subscribe(({ data }) => {
      if (!data?.onPatientDetailsCreated?.id) return;
      this._patientDetails.update((rows) => this.upsertById(rows, data.onPatientDetailsCreated));
    });

    this.apollo.subscribe<{ onTaskCreated: GqlTask }>({ query: TASK_SUBSCRIPTION }).subscribe(({ data }) => {
      if (!data?.onTaskCreated?.id) return;
      this._tasks.update((rows) => this.upsertById(rows, data.onTaskCreated));
    });
  }

  private getHealthRows(patientId: string): GqlHealthStatus[] {
    return [...(this._healthByPatient()[patientId] ?? [])]
      .sort((a, b) => this.toDate(b.timestamp).getTime() - this.toDate(a.timestamp).getTime());
  }

  private resolveStatus(rosterStatus: GqlRoster['status'], painScale?: number): PatientStatus {
    if (Number(painScale ?? 0) >= 7) return 'Critical';
    if (rosterStatus === 'INACTIVE' || rosterStatus === 'DECLINED') return 'Inactive';
    return 'Active';
  }

  private resolveNurseId(patientId: string): string | null {
    const rosterNurseId = this._rosters().find((row) => row.patientId === patientId)?.nurseId;
    if (rosterNurseId) return rosterNurseId;

    const fallback = this._users().find((row) => (row.role ?? '').toUpperCase() === 'NURSE')?.id;
    return fallback ?? null;
  }

  private findNurseIdByName(nurseName: string): string | null {
    const normalized = (nurseName || '').trim().toLowerCase();
    if (!normalized) return null;
    const nurse = this._users().find((row) => {
      if ((row.role ?? '').toUpperCase() !== 'NURSE') return false;
      const fullName = `${row.firstName ?? ''} ${row.lastName ?? ''}`.trim().toLowerCase();
      return fullName === normalized;
    });
    return nurse?.id ?? null;
  }

  private toRosterStatus(status: PatientStatus): GqlRoster['status'] {
    if (status === 'Inactive') return 'INACTIVE';
    return 'ACTIVE';
  }

  private normalizePhone(phone: string): string {
    const digits = (phone || '').replace(/\D+/g, '');
    return digits || '0';
  }

  private generatePatientEmail(firstName: string, lastName: string): string {
    const first = (firstName || 'patient').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
    const last = (lastName || 'user').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
    return `${first}.${last}.${Date.now()}@carebridge.local`;
  }

  private toDate(value?: string): Date {
    if (!value) return new Date(0);
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? new Date(0) : parsed;
  }

  private toTime(value?: string): string {
    const date = this.toDate(value);
    if (date.getTime() === 0) return 'TBD';
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }).toLowerCase();
  }

  private upsertById<T extends { id: string }>(rows: T[], row: T): T[] {
    const index = rows.findIndex((entry) => entry.id === row.id);
    if (index === -1) return [row, ...rows];
    const next = [...rows];
    next[index] = { ...next[index], ...row };
    return next;
  }

  private upsertByPatient<T extends { id: string }>(state: WritableSignal<Record<string, T[]>>, patientId: string, row: T): void {
    state.update((current) => {
      const rows = [...(current[patientId] ?? [])];
      const index = rows.findIndex((entry) => entry.id === row.id);
      if (index === -1) {
        rows.push(row);
      } else {
        rows[index] = { ...rows[index], ...row };
      }
      return { ...current, [patientId]: rows };
    });
  }

  private normalizeRows<T extends { id: string }>(rows: ReadonlyArray<Partial<T> | null | undefined> | null | undefined): T[] {
    if (!rows?.length) return [];
    return rows
      .filter((row): row is Partial<T> & { id: string } => typeof row?.id === 'string' && row.id.length > 0)
      .map((row) => row as T);
  }
}


