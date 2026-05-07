import { Injectable, inject, signal } from '@angular/core';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map, switchMap} from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

import { UserService } from '../app/cruds/services/userService';

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

const AUTH_TOKEN_KEY = 'carebridge_auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSvc = inject(UserService);
  private readonly http = inject(HttpClient);

  readonly currentRoles = signal<UserRole[]>([]);
  readonly currentPermissions = signal<string[]>([]);
  readonly currentUserName = signal<string>('Guest');
  readonly currentUserId = signal<string>('');

  readonly currentRole = () => this.currentRoles()[0] || 'Patient';

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

  isAdmin(): boolean { return this.currentRoles().includes('Admin'); }
  isNurse(): boolean { return this.currentRoles().includes('Nurse'); }
  isPatient(): boolean { return this.currentRoles().includes('Patient'); }
  isFamily(): boolean { return this.currentRoles().includes('Family'); }

  hasRole(role: UserRole): boolean {
    return this.currentRoles().includes(role);
  }

  hasPermission(permission: string): boolean {
    return this.currentPermissions().includes(permission);
  }

  registerNewUser(payload: RegisterRequest): Observable<void> {
    return this.userSvc.create({
      firstName: payload.firstName,
      lastName: payload.lastName,
      email: payload.email,
      phoneNumber: parseInt(payload.phoneNumber) || 0,
      roles: ['PATIENT'],
      userStatus: 'ACTIVE'
    } as any).pipe(
      map(() => void 0),
      catchError((error) => {
        return throwError(() => ({ error: 'Registration failed.' }));
      })
    );
  }

  login(credentials: LoginRequest): Observable<void> {
    return this.http.post<void>('http://localhost:8080/api/login', credentials, { withCredentials: true }).pipe(
      switchMap(() => this.fetchAndHydrateUser()),
      catchError(error => throwError(() => ({ error: 'Invalid email or password.' })))
    );
  }

  logout(): void {
    this.http.post('http://localhost:8080/api/logout', {}, { withCredentials: true })
      .subscribe({ error: () => {} });
    this.resetLocalState();
  }

  getAuthToken(): string | null {
    return localStorage.getItem(AUTH_TOKEN_KEY);
  }

  private resetLocalState(): void {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    this.currentRoles.set([]);
    this.currentPermissions.set([]);
    this.currentUserName.set('Guest');
    this.currentUserId.set('');
  }

  private fetchAndHydrateUser(): Observable<void> {
    return this.http.get<any>('http://localhost:8080/api/users/me', { withCredentials: true }).pipe(
      map((user) => {
        console.log('[AuthService] User data received:', user);
        const roles = this.mapRoles(user.roles);
        const permissions = Array.from(new Set(user.permissions || [])) as string[];
        console.log('[AuthService] Mapped roles:', roles);
        console.log('[AuthService] Mapped permissions:', permissions);
        this.setCurrentUser(user.id, roles, permissions, `${user.firstName} ${user.lastName}`);
      }),
      catchError((err) => {
        console.error('[AuthService] Hydration failed:', err);
        return of(void 0);
      })
    );
  }

  private hydrateSessionFromToken(): void {
    this.fetchAndHydrateUser().subscribe({
      error: () => {
        this.resetLocalState();
      }
    });
  }

  private persistToken(token: string): void {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
  }

  private mapRoles(roles?: string[]): UserRole[] {
    if (!roles || roles.length === 0) return [];
    return roles.map(r => {
        const role = r.toUpperCase().replace('ROLE_', '').trim();
        if (role === 'ADMIN') return 'Admin';
        if (role === 'NURSE') return 'Nurse';
        if (role === 'FAMILY') return 'Family';
        if (role === 'PATIENT') return 'Patient';
        return 'Patient';
    }) as UserRole[];
  }
}
