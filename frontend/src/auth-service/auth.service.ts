import { Injectable, inject, signal } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { GET_USERS, LOGIN_USER, REGISTER_USER } from '../app/graphql/user.graphql';

export type UserRole = 'admin' | 'nurse' | 'patient';

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

type GqlUser = {
  id: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  role?: string;
};

const AUTH_TOKEN_KEY = 'carebridge_auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apollo = inject(Apollo);

  readonly currentRole = signal<UserRole>('patient');
  readonly currentUserName = signal<string>('Guest');

  constructor() {
    this.hydrateSessionFromToken();
  }

  setRole(role: UserRole, name: string) {
    this.currentRole.set(role);
    this.currentUserName.set(name || 'Guest');
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

  registerNewUser(payload: RegisterRequest): Observable<void> {
    return this.apollo.mutate<{ registerUser: GqlUser }>({
      mutation: REGISTER_USER,
      variables: {
        input: {
          firstName: payload.firstName,
          lastName: payload.lastName,
          email: payload.email,
          phoneNumber: payload.phoneNumber,
          password: payload.password,
        }
      }
    }).pipe(
      map(() => void 0),
      catchError((error) => {
        const backendMessage = error?.message ?? error?.graphQLErrors?.[0]?.message ?? 'Registration failed.';
        return throwError(() => ({ error: backendMessage }));
      })
    );
  }

  login(credentials: LoginRequest): Observable<UserRole> {
    const email = credentials.email.trim().toLowerCase();

    return this.apollo.mutate<{ loginUser: string }>({
      mutation: LOGIN_USER,
      variables: {
        input: {
          email,
          password: credentials.password,
        }
      }
    }).pipe(
      switchMap((result) => {
        const token = result.data?.loginUser;
        if (!token) {
          return throwError(() => ({ error: 'Login failed. Empty token returned by backend.' }));
        }

        this.persistToken(token);

        return this.apollo.query<{ getUsers: GqlUser[] }>({
          query: GET_USERS,
          variables: { page: 0, size: 500 },
          fetchPolicy: 'network-only',
        }).pipe(
          map((queryResult) => {
            const users = queryResult.data?.getUsers ?? [];
            const user = users.find((row) => (row.email ?? '').toLowerCase() === email);
            const role = this.mapRole(user?.role);
            const name = `${user?.firstName ?? ''} ${user?.lastName ?? ''}`.trim() || email;
            this.setRole(role, name);
            return role;
          })
        );
      }),
      catchError((error) => {
        const backendMessage = error?.error ?? error?.message ?? error?.graphQLErrors?.[0]?.message ?? 'Invalid email or password.';
        return throwError(() => ({ error: backendMessage }));
      })
    );
  }

  activateUser(_: string, __: string): Observable<UserRole> {
    return of(this.currentRole());
  }

  logout(): void {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    this.currentRole.set('patient');
    this.currentUserName.set('Guest');
  }

  getAuthToken(): string | null {
    return localStorage.getItem(AUTH_TOKEN_KEY);
  }

  private hydrateSessionFromToken(): void {
    const token = this.getAuthToken();
    if (!token) return;

    const claims = this.parseJwtClaims(token);
    const roleFromToken = this.extractRoleFromClaims(claims);
    if (roleFromToken) {
      this.currentRole.set(roleFromToken);
    }

    const subjectClaim = claims?.['sub'];
    const emailFromToken = typeof subjectClaim === 'string' ? subjectClaim : '';
    if (!emailFromToken) return;

    this.apollo.query<{ getUsers: GqlUser[] }>({
      query: GET_USERS,
      variables: { page: 0, size: 500 },
      fetchPolicy: 'network-only',
    }).pipe(
      map((result) => result.data?.getUsers ?? []),
      tap((users) => {
        const user = users.find((row) => (row.email ?? '').toLowerCase() === emailFromToken.toLowerCase());
        if (!user) return;
        const resolvedRole = this.mapRole(user.role) || roleFromToken || 'patient';
        const name = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || emailFromToken;
        this.setRole(resolvedRole, name);
      }),
      catchError(() => of([]))
    ).subscribe();
  }

  private persistToken(token: string): void {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
  }

  private mapRole(role?: string): UserRole {
    switch ((role ?? '').toUpperCase()) {
      case 'ADMIN':
        return 'admin';
      case 'NURSE':
        return 'nurse';
      default:
        return 'patient';
    }
  }

  private parseJwtClaims(token: string): Record<string, unknown> | null {
    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;
      const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = payload.padEnd(Math.ceil(payload.length / 4) * 4, '=');
      return JSON.parse(atob(padded));
    } catch {
      return null;
    }
  }

  private extractRoleFromClaims(claims: Record<string, unknown> | null): UserRole | null {
    if (!claims) return null;
    const authorities = claims['authorities'];
    if (!Array.isArray(authorities)) return null;
    const normalized = authorities.map((a) => String(a).toUpperCase());
    if (normalized.some((a) => a.includes('ADMIN'))) return 'admin';
    if (normalized.some((a) => a.includes('NURSE'))) return 'nurse';
    if (normalized.some((a) => a.includes('PATIENT'))) return 'patient';
    return null;
  }
}
