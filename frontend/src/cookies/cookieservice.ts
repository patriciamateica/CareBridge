import { Injectable, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CookiesService {
  private router = inject(Router);

  private readonly ONE_YEAR_DAYS = 365;
  private readonly SESSION_DAYS = 0;

  constructor() {
    this.initSession();
    this.trackPageViews();
  }

  set(name: string, value: string, days = 0, path = '/'): void {
    let expires = '';
    if (days > 0) {
      const date = new Date();
      date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
      expires = `; expires=${date.toUTCString()}`;
    }
    document.cookie = `${encodeURIComponent(name)}=${encodeURIComponent(value)}${expires}; path=${path}; SameSite=Lax`;
  }

  get(name: string): string {
    const key = encodeURIComponent(name) + '=';
    for (const part of document.cookie.split(';')) {
      const trimmed = part.trimStart();
      if (trimmed.startsWith(key)) {
        return decodeURIComponent(trimmed.substring(key.length));
      }
    }
    return '';
  }

  check(name: string): boolean {
    return this.get(name) !== '';
  }

  delete(name: string, path = '/'): void {
    document.cookie = `${encodeURIComponent(name)}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=${path}`;
  }

  deleteAll(path = '/'): void {
    document.cookie.split(';').forEach(c => {
      const name = c.split('=')[0].trim();
      this.delete(name, path);
    })
  }

  savePreference(key: string, value: string): void {
    this.set(`pref_${key}`, value, this.ONE_YEAR_DAYS);
  }

  getPreference(key: string): string {
    return this.get(`pref_${key}`);
  }

  removePreference(key: string): void {
    this.delete(`pref_${key}`);
  }

  logActivity(action: string, details?: string): void {
    const entry = {
      visitorId: this.get('visitor_id'),
      sessionStart: this.get('session_start'),
      action,
      details,
      timestamp: new Date().toISOString(),
    };
    // TODO: batch and POST to backend when ready
    console.log('[CareBridge Activity]', entry);
  }

  getVisitorId(): string {
    return this.get('visitor_id');
  }

  getSessionStart(): string {
    return this.get('session_start');
  }

  getPageViewCount(): number {
    return parseInt(this.get('page_views') || '0', 10);
  }

  getLastVisit(): string {
    return this.get('last_visit');
  }


  private initSession(): void {
    if (!this.check('visitor_id')) {
      const id = typeof crypto !== 'undefined' && crypto.randomUUID
        ? crypto.randomUUID()
        : Math.random().toString(36).substring(2);
      this.set('visitor_id', id, this.ONE_YEAR_DAYS);
    }

    if (!this.check('session_start')) {
      const previousSession = this.get('last_session_start');
      if (previousSession) {
        this.set('last_visit', previousSession, this.ONE_YEAR_DAYS);
      }

      const now = new Date().toISOString();
      this.set('session_start', now);
      this.set('last_session_start', now, this.ONE_YEAR_DAYS);
      this.set('page_views', '0');
    }
  }

  private trackPageViews(): void {
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((event: any) => {
        const count = this.getPageViewCount() + 1;
        this.set('page_views', String(count));
        this.logActivity('page_view', event.urlAfterRedirects);
      });
  }
}
