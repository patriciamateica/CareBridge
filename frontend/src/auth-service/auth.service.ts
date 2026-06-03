import { Injectable, inject, signal } from '@angular/core';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { UserService } from '../app/cruds/services/userService';
import {buildApiUrl} from '../app/api-config';

export type UserRole = 'Admin' | 'Nurse' | 'Patient' | 'Family';

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  otp: string;
  newPassword: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSvc = inject(UserService);
  private readonly http = inject(HttpClient);

  readonly currentRoles = signal<UserRole[]>([]);
  readonly currentPermissions = signal<string[]>([]);
  readonly currentUserName = signal<string>('Guest');
  readonly currentUserId = signal<string>('');

  readonly currentRole = () => {
    const roles = this.currentRoles();
    if (roles.includes('Admin')) return 'Admin';
    if (roles.includes('Nurse')) return 'Nurse';
    return roles[0] || 'Patient';
  };

  constructor() {
    this.hydrateSessionFromToken();
  }

  setRoles(roles: UserRole[], permissions: string[], name: string) {
    this.currentRoles.set(roles);
    this.currentPermissions.set(permissions || []);
    this.currentUserName.set(name || 'Guest');
  }

  setCurrentUser(id: string, roles: UserRole[], permissions: string[], name: string) {
    this.currentUserId.set(id);
    this.setRoles(roles, permissions, name);
  }

  isAdmin(): boolean    { return this.currentRoles().includes('Admin'); }
  isNurse(): boolean    { return this.currentRoles().includes('Nurse'); }
  isPatient(): boolean  { return this.currentRoles().includes('Patient'); }
  isFamily(): boolean   { return this.currentRoles().includes('Family'); }

  hasRole(role: UserRole): boolean {
    return this.currentRoles().includes(role);
  }

  hasPermission(permission: string): boolean {
    return this.currentPermissions().includes(permission);
  }

   login(credentials: LoginRequest): Observable<void> {
     return this.http
       .post<void>(buildApiUrl('/api/login'), credentials, { withCredentials: true })
       .pipe(
         switchMap(() => this.fetchAndHydrateUser()),
         catchError(() => throwError(() => ({ error: 'Invalid email or password.' })))
       );
   }

   forgotPassword(email: string): Observable<void> {
     const body: ForgotPasswordRequest = { email };
     return this.http
       .post<void>(buildApiUrl('/api/auth/forgot-password'), body)
       .pipe(catchError(() => of(void 0)));
   }

   resetPassword(email: string, otp: string, newPassword: string): Observable<void> {
     const body: ResetPasswordRequest = { email, otp, newPassword };
     return this.http
       .post<{ message?: string; error?: string }>(buildApiUrl('/api/auth/reset-password'), body)
       .pipe(
         map(() => void 0),
         catchError(err => {
           const msg = err?.error?.error ?? 'Password reset failed. The link may have expired.';
           return throwError(() => ({ error: msg }));
         })
       );
   }

   registerNewUser(payload: RegisterRequest): Observable<void> {
     return this.http
       .post<void>(buildApiUrl('/api/register'), payload)
       .pipe(
         map(() => void 0),
         catchError(() => throwError(() => ({ error: 'Registration failed.' })))
       );
   }

   logout(): void {
     this.http.post(buildApiUrl('/api/logout'), {}, { withCredentials: true })
       .subscribe({ error: () => {} });
     this.resetLocalState();
   }

   private fetchAndHydrateUser(): Observable<void> {
     return this.http
       .get<any>(buildApiUrl('/api/users/me'), { withCredentials: true })
       .pipe(
         map(user => {
           const roles = this.mapRoles(user.roles);
           const permissions = Array.from(new Set<string>(user.permissions || [])) as string[];
           this.setCurrentUser(user.id, roles, permissions, `${user.firstName} ${user.lastName}`);
         }),
         catchError(() => of(void 0))
       );
   }

  private hydrateSessionFromToken(): void {
    this.fetchAndHydrateUser().subscribe({
      error: () => this.resetLocalState()
    });
  }

  private resetLocalState(): void {
    this.currentRoles.set([]);
    this.currentPermissions.set([]);
    this.currentUserName.set('Guest');
    this.currentUserId.set('');
  }

  private mapRoles(roles?: string[]): UserRole[] {
    if (!roles || roles.length === 0) return [];
    return roles.map(r => {
      const role = r.toUpperCase().replace('ROLE_', '').trim();
      if (role === 'ADMIN')   return 'Admin';
      if (role === 'NURSE')   return 'Nurse';
      if (role === 'FAMILY')  return 'Family';
      return 'Patient';
    }) as UserRole[];
  }
}
