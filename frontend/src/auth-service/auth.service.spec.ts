import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => expect(service).toBeTruthy());

  it('should default to admin role', () => {
    expect(service.currentRole()).toBe('admin');
  });

  it('should default username to Ana Pop', () => {
    expect(service.currentUserName()).toBe('Ana Pop');
  });

  it('setRole() updates role and name', () => {
    service.setRole('admin', 'Admin User');
    expect(service.currentRole()).toBe('admin');
    expect(service.currentUserName()).toBe('Admin User');
  });

  it('isAdmin() returns true for admin', () => {
    service.setRole('admin', 'Admin');
    expect(service.isAdmin()).toBeTrue();
  });

  it('isAdmin() returns false for nurse', () => {
    service.setRole('nurse', 'Nurse');
    expect(service.isAdmin()).toBeFalse();
  });

  it('isNurse() returns true for nurse', () => {
    service.setRole('nurse', 'Nurse');
    expect(service.isNurse()).toBeTrue();
  });

  it('isNurse() returns false for admin', () => {
    service.setRole('admin', 'Admin');
    expect(service.isNurse()).toBeFalse();
  });

  it('isPatient() returns true for patient', () => {
    service.setRole('patient', 'Patient User');
    expect(service.isPatient()).toBeTrue();
  });

  it('isPatient() returns false for nurse', () => {
    service.setRole('nurse', 'Nurse');
    expect(service.isPatient()).toBeFalse();
  });
});
