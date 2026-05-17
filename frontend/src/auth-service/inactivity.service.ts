import { Injectable, inject, NgZone, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ToastService } from '../app/toast-service/toast-service';

@Injectable({ providedIn: 'root' })
export class InactivityService implements OnDestroy {

  static readonly TIMEOUT_MS = 15 * 60 * 1000;

  private static readonly ACTIVITY_EVENTS: ReadonlyArray<keyof WindowEventMap> = [
    'mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll', 'click',
  ];

  private readonly router    = inject(Router);
  private readonly authSvc   = inject(AuthService);
  private readonly toastSvc  = inject(ToastService);
  private readonly zone      = inject(NgZone);

  private timerId: ReturnType<typeof setTimeout> | null = null;
  private readonly boundReset = () => this.resetTimer();

  constructor() {
    this.startWatching();
  }

  ngOnDestroy(): void {
    this.stopWatching();
  }


  resetTimer(): void {
    this.clearTimer();
    this.zone.runOutsideAngular(() => {
      this.timerId = setTimeout(() => {
        this.zone.run(() => this.handleTimeout());
      }, InactivityService.TIMEOUT_MS);
    });
  }

  stopWatching(): void {
    this.clearTimer();
    InactivityService.ACTIVITY_EVENTS.forEach(evt =>
      window.removeEventListener(evt, this.boundReset));
  }


  private startWatching(): void {
    InactivityService.ACTIVITY_EVENTS.forEach(evt =>
      window.addEventListener(evt, this.boundReset, { passive: true }));
    this.resetTimer();
  }

  private handleTimeout(): void {
    if (this.authSvc.currentRoles().length === 0) {
      return;
    }
    this.authSvc.logout();
    this.toastSvc.showError('You have been logged out due to inactivity.');
    this.router.navigate(['/user-login']);
  }

  private clearTimer(): void {
    if (this.timerId !== null) {
      clearTimeout(this.timerId);
      this.timerId = null;
    }
  }
}
