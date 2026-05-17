import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { UserService } from '../app/cruds/services/userService';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUserService = {
    create: jasmine.createSpy('create'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserService, useValue: mockUserService },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    service  = TestBed.inject(AuthService);

    const req = httpMock.expectOne(r => r.url.includes('/api/users/me'));
    req.flush({}, { status: 401, statusText: 'Unauthorized' });
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should default to empty roles', () => {
    expect(service.currentRoles()).toEqual([]);
  });

  it('should default username to Guest', () => {
    expect(service.currentUserName()).toBe('Guest');
  });

  it('setRoles() should update roles, permissions and name', () => {
    service.setRoles(['Admin'], ['user:read'], 'Admin User');

    expect(service.currentRoles()).toEqual(['Admin']);
    expect(service.currentPermissions()).toEqual(['user:read']);
    expect(service.currentUserName()).toBe('Admin User');
  });

  it('isAdmin() returns true for Admin', () => {
    service.setRoles(['Admin'], [], 'Admin');
    expect(service.isAdmin()).toBeTrue();
  });

  it('isAdmin() returns false for Nurse', () => {
    service.setRoles(['Nurse'], [], 'Nurse');
    expect(service.isAdmin()).toBeFalse();
  });

  it('isNurse() returns true for Nurse', () => {
    service.setRoles(['Nurse'], [], 'Nurse');
    expect(service.isNurse()).toBeTrue();
  });

  it('isNurse() returns false for Admin', () => {
    service.setRoles(['Admin'], [], 'Admin');
    expect(service.isNurse()).toBeFalse();
  });

  it('isPatient() returns true for Patient', () => {
    service.setRoles(['Patient'], [], 'Patient User');
    expect(service.isPatient()).toBeTrue();
  });

  it('isPatient() returns false for Nurse', () => {
    service.setRoles(['Nurse'], [], 'Nurse');
    expect(service.isPatient()).toBeFalse();
  });

  it('isFamily() returns true for Family', () => {
    service.setRoles(['Family'], [], 'Family User');
    expect(service.isFamily()).toBeTrue();
  });

  it('hasPermission() returns true when permission is present', () => {
    service.setRoles(['Admin'], ['user:write', 'user:read'], 'Admin');
    expect(service.hasPermission('user:write')).toBeTrue();
  });

  it('hasPermission() returns false when permission is absent', () => {
    service.setRoles(['Nurse'], ['checkin:write'], 'Nurse');
    expect(service.hasPermission('user:delete')).toBeFalse();
  });

  it('currentRole() returns first role', () => {
    service.setRoles(['Nurse', 'Patient'], [], 'Nurse');
    expect(service.currentRole()).toBe('Nurse');
  });

  it('currentRole() returns Patient when no roles set', () => {
    service.setRoles([], [], 'Guest');
    expect(service.currentRole()).toBe('Patient');
  });

  it('logout() should reset local state and call backend', () => {
    service.setRoles(['Nurse'], [], 'Nurse');

    service.logout();

    const logoutReq = httpMock.expectOne(r => r.url.includes('/api/logout'));
    logoutReq.flush(null);

    expect(service.currentRoles()).toEqual([]);
    expect(service.currentUserName()).toBe('Guest');
  });
});
