import {inject, Injectable, signal} from '@angular/core';
import {Observable, throwError, of} from 'rxjs';
import {delay} from 'rxjs/operators';
import {CookiesService} from '../cookies/cookieservice';

export type UserRole = 'admin' | 'nurse' | 'patient';

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface MockUser {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
  activated: boolean;
  role?: UserRole;
  displayName?: string;
}

const ACTIVATION_CODES: Record<string, UserRole> = {
  'NRS-0001': 'nurse',
  'NRS-0002': 'nurse',
  'PAT-0001': 'patient',
  'PAT-0002': 'patient',
  'ADM-0001': 'admin',
  'ADM-0002': 'admin',
};

const STORAGE_KEY = 'mock_users';

@Injectable({providedIn: 'root'})
export class AuthService {
  private cookiesService = inject(CookiesService);

  readonly currentRole = signal<UserRole>('admin');
  readonly currentUserName = signal<string>('Ana Pop');

  setRole(role: UserRole, name: string) {
    this.currentRole.set(role);
    this.currentUserName.set(name);
  }

  isAdmin(): boolean {
    return this.currentRole() === 'admin';
  }

  isNurse(): boolean {
    return this.currentRole() === 'nurse';
  }

  isPatient(): boolean {
    return this.currentRole() === 'patient';
  }

  private getUsers(): MockUser[] {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]');
  }

  private saveUsers(users: MockUser[]): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(users));
  }

  registerNewUser(payload: RegisterRequest): Observable<void> {
    const users = this.getUsers();
    if (users.some(u => u.email === payload.email)) {
      return throwError(() => ({error: 'Email already exists'})).pipe(delay(400));
    }
    users.push({...payload, activated: false});
    this.saveUsers(users);
    return of(void 0).pipe(delay(600));
  }

  activateUser(email: string, activationId: string): Observable<UserRole> {
    const role = ACTIVATION_CODES[activationId.toUpperCase()];
    if (!role) {
      return throwError(() => ({error: 'Invalid activation ID'})).pipe(delay(400));
    }
    const users = this.getUsers();
    const idx = users.findIndex(u => u.email === email);
    if (idx === -1) {
      return throwError(() => ({error: 'User not found'})).pipe(delay(400));
    }
    users[idx] = {...users[idx], activated: true, role};
    this.saveUsers(users);

    const displayName = users[idx].displayName ?? email;
    this.setRole(role, displayName);

    return of(role).pipe(delay(600));
  }
}
