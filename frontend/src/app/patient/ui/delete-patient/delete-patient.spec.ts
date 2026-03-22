import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DeletePatient } from './delete-patient';
import { PatientService } from '../../patient-crud/patient-service';
import { MessageService } from 'primeng/api';
import { ComponentRef } from '@angular/core';

const mockPatient: {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  diagnosis: string;
  status: string;
  phone: string;
  address: string;
  assignedNurse: string;
  notes: string;
  createdAt: Date
} = {
  id: 'del-1', firstName: 'Ion', lastName: 'Popa', dateOfBirth: new Date('1938-07-22'), diagnosis: 'Diabetes', status: 'Critical', phone: '0733987654',
  address: 'Bd. Unirii 5', assignedNurse: 'Elena Marin', notes: '', createdAt: new Date()
};

describe('DeletePatient', () => {
  let component: DeletePatient;
  let fixture: ComponentFixture<DeletePatient>;
  let ref: ComponentRef<DeletePatient>;
  let patientService: jasmine.SpyObj<PatientService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    patientService = jasmine.createSpyObj('PatientService', ['delete']);
    messageService = jasmine.createSpyObj('MessageService', ['add']);

    await TestBed.configureTestingModule({
      imports: [DeletePatient],
      providers: [
        { provide: PatientService, useValue: patientService },
        { provide: MessageService, useValue: messageService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeletePatient);
    component = fixture.componentInstance;
    ref = fixture.componentRef;
    ref.setInput('patientData', mockPatient);
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());
  it('should start hidden', () => expect(component.visible()).toBeFalse());

  it('openDialog() shows dialog', () => {
    component.openDialog();
    expect(component.visible()).toBeTrue();
  });

  it('confirmDelete() calls service and emits refresh', () => {
    const spy = jasmine.createSpy();
    component.refreshTable.subscribe(spy);
    component.confirmDelete();
    expect(patientService.delete).toHaveBeenCalledWith('del-1');
    expect(spy).toHaveBeenCalled();
    expect(component.visible()).toBeFalse();
  });

  it('success message includes patient name', () => {
    component.confirmDelete();
    const args = messageService.add.calls.mostRecent().args[0];
    expect(args.detail).toContain('Ion');
    expect(args.detail).toContain('Popa');
  });
});
