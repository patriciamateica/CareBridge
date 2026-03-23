import { Injectable, signal } from '@angular/core';

export type UserRole = 'admin' | 'nurse' | 'patient';

@Injectable({ providedIn: 'root' })
export class AuthService {
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
}
