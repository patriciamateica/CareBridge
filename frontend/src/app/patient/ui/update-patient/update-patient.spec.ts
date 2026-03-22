import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UpdatePatient } from './update-patient';
import { PatientService } from '../../patient-crud/patient-service';
import { MessageService } from 'primeng/api';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Patient } from '../../patient-crud/patient-model';
import { ComponentRef } from '@angular/core';

const mockPatient: Patient = {
  id: 'p1', firstName: 'Maria', lastName: 'Ionescu',
  dateOfBirth: new Date('1945-03-12'),
  diagnosis: 'Hypertension', neurologicalStatus: 'Normal',
  associatedConditions: 'None', status: 'Active',
  phone: '0721123456', address: 'Str. Test 1',
  assignedNurse: 'Ana Pop', notes: 'Some notes',
  createdAt: new Date()
};

describe('UpdatePatient', () => {
  let component: UpdatePatient;
  let fixture: ComponentFixture<UpdatePatient>;
  let ref: ComponentRef<UpdatePatient>;
  let patientService: jasmine.SpyObj<PatientService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    patientService = jasmine.createSpyObj('PatientService', ['update']);
    messageService = jasmine.createSpyObj('MessageService', ['add']);

    await TestBed.configureTestingModule({
      imports: [UpdatePatient, NoopAnimationsModule],
      providers: [
        { provide: PatientService, useValue: patientService },
        { provide: MessageService, useValue: messageService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdatePatient);
    component = fixture.componentInstance;
    ref = fixture.componentRef;
    ref.setInput('patientData', mockPatient);
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('should start with dialog hidden', () => {
    expect(component.visible()).toBeFalse();
  });

  it('openDialog() should show dialog and sync nurse fields', () => {
    component.openDialog();
    expect(component.visible()).toBeTrue();
    expect(component.editPatient.firstName).toBe('Maria');
    expect(component.editPatient.lastName).toBe('Ionescu');
    expect(component.editPatient.diagnosis).toBe('Hypertension');
    expect(component.editPatient.status).toBe('Active');
    expect(component.editPatient.assignedNurse).toBe('Ana Pop');
  });

  it('sync() should preserve non-editable patient fields', () => {
    component.openDialog();
    // Fields nurse cannot edit must be preserved from original
    expect(component.editPatient.phone).toBe('0721123456');
    expect(component.editPatient.address).toBe('Str. Test 1');
    expect(component.editPatient.notes).toBe('Some notes');
  });

  it('should not update when form is invalid', () => {
    component.onSubmit({ valid: false } as any);
    expect(patientService.update).not.toHaveBeenCalled();
  });

  it('should update and emit refresh on valid submit', () => {
    const refreshSpy = jasmine.createSpy();
    component.refreshTable.subscribe(refreshSpy);
    component.onSubmit({ valid: true } as any);
    expect(patientService.update).toHaveBeenCalledWith('p1', component.editPatient);
    expect(refreshSpy).toHaveBeenCalled();
    expect(component.visible()).toBeFalse();
  });

  it('should show success toast on valid submit', () => {
    component.onSubmit({ valid: true } as any);
    expect(messageService.add).toHaveBeenCalledWith(
      jasmine.objectContaining({ severity: 'success' })
    );
  });

  it('should re-sync when patientData input changes', () => {
    ref.setInput('patientData', { ...mockPatient, firstName: 'Updated' });
    fixture.detectChanges();
    expect(component.editPatient.firstName).toBe('Updated');
  });

  it('should have correct status options', () => {
    expect(component.statusOptions).toEqual(['Active', 'Inactive', 'Critical']);
  });
});
