// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { CreatePatient } from './create-patient';
// import { PatientService } from '../../patient-crud/patient-service';
// import { MessageService } from 'primeng/api';
// import { NoopAnimationsModule } from '@angular/platform-browser/animations';
// import { signal } from '@angular/core';
// import { RealtimeMutationOfflineService } from '../../../offline-support/realtime-mutation-offline.service';
// import { RealtimeDataService } from '../../../services/realtime-data.service';
//
// describe('CreatePatient', () => {
//   let component: CreatePatient;
//   let fixture: ComponentFixture<CreatePatient>;
//   let patientService: jasmine.SpyObj<PatientService>;
//   let messageService: jasmine.SpyObj<MessageService>;
//   let realtimeService: jasmine.SpyObj<RealtimeMutationOfflineService>;
//   const realtimeDataMock = {
//     nurses: signal([
//       { id: 'n1', firstName: 'Ana', lastName: 'Pop' },
//       { id: 'n2', firstName: 'Mara', lastName: 'Ionescu' },
//     ]),
//   };
//
//   beforeEach(async () => {
//     patientService = jasmine.createSpyObj('PatientService', ['create']);
//     messageService = jasmine.createSpyObj('MessageService', ['add']);
//     realtimeService = jasmine.createSpyObj('RealtimeMutationOfflineService', ['createPatient']);
//     realtimeService.createPatient.and.resolveTo();
//
//     await TestBed.configureTestingModule({
//       imports: [CreatePatient, NoopAnimationsModule],
//       providers: [
//         { provide: PatientService, useValue: patientService },
//         { provide: MessageService, useValue: messageService },
//         { provide: RealtimeMutationOfflineService, useValue: realtimeService },
//         { provide: RealtimeDataService, useValue: realtimeDataMock },
//       ],
//     }).compileComponents();
//
//     fixture = TestBed.createComponent(CreatePatient);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });
//
//   it('should create', () => expect(component).toBeTruthy());
//
//   it('should start with dialog hidden', () => {
//     expect(component.visible()).toBeFalse();
//   });
//
//   it('open() should show dialog and reset form', () => {
//     component.newPatient.firstName = 'dirty';
//     component.open();
//     expect(component.visible()).toBeTrue();
//     expect(component.newPatient.firstName).toBe('');
//   });
//
//   it('should have correct status options', () => {
//     expect(component.statusOptions).toEqual(['Active', 'Inactive', 'Critical']);
//   });
//
//   it('should default status to Active', () => {
//     expect(component.newPatient.status).toBe('Active');
//   });
//
//   it('should not create patient when form is invalid', () => {
//     component.onSubmit({ valid: false } as any);
//     expect(patientService.create).not.toHaveBeenCalled();
//   });
//
//   it('should create patient and emit refresh on valid submit', () => {
//     const refreshSpy = jasmine.createSpy();
//     component.refreshTable.subscribe(refreshSpy);
//
//     component.newPatient.firstName = 'Maria';
//     component.newPatient.lastName = 'Ionescu';
//     component.newPatient.diagnosis = 'Hypertension';
//     component.newPatient.assignedNurse = 'Ana Pop';
//
//     const form = { valid: true, resetForm: jasmine.createSpy() } as any;
//     component.onSubmit(form);
//
//     expect(patientService.create).toHaveBeenCalledWith(
//       jasmine.objectContaining({
//         firstName: 'Maria',
//         lastName: 'Ionescu',
//         diagnosis: 'Hypertension',
//         assignedNurse: 'Ana Pop',
//         status: 'Active'
//       })
//     );
//     expect(refreshSpy).toHaveBeenCalled();
//     expect(component.visible()).toBeFalse();
//   });
//
//   it('should reset form fields after successful submit', () => {
//     component.newPatient.firstName = 'Maria';
//     component.newPatient.diagnosis = 'Hypertension';
//     const form = { valid: true, resetForm: jasmine.createSpy() } as any;
//     component.onSubmit(form);
//     expect(component.newPatient.firstName).toBe('');
//     expect(component.newPatient.diagnosis).toBe('');
//   });
//
//   it('empty patient should have all optional fields as empty strings', () => {
//     const p = component.newPatient;
//     expect(p.neurologicalStatus).toBe('');
//     expect(p.associatedConditions).toBe('');
//     expect(p.phone).toBe('');
//     expect(p.address).toBe('');
//     expect(p.notes).toBe('');
//   });
// });
