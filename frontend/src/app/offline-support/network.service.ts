import { Injectable, computed, signal } from '@angular/core';
import { combineLatest, from, fromEvent, merge, Observable, of, timer, Subject } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, switchMap, filter, pairwise } from 'rxjs/operators';

type OfflineReason = 'NETWORK_DOWN' | 'SERVER_UNREACHABLE' | 'ONLINE';

@Injectable({ providedIn: 'root' })
export class NetworkService {
  private readonly probeIntervalMs = 10000;
  private readonly probeTimeoutMs = 4000;
  private readonly probeUrl = this.resolveProbeUrl();

  private readonly _browserOnline = signal<boolean>(typeof window === 'undefined' ? true : window.navigator.onLine);
  private readonly _serverReachable = signal<boolean>(true);
  private readonly connectionRestoredSubject = new Subject<void>();

  readonly isBrowserOnlineSignal = this._browserOnline.asReadonly();
  readonly isServerReachableSignal = this._serverReachable.asReadonly();
  readonly isOnlineSignal = computed(() => this._browserOnline() && this._serverReachable());

  readonly isServerReachable$: Observable<boolean>;
  readonly isOnline$: Observable<boolean>;
  readonly connectionRestored$ = this.connectionRestoredSubject.asObservable();

  constructor() {
    const browserOnline$ = this.buildBrowserOnlineStream();

    this.isServerReachable$ = combineLatest([browserOnline$, timer(0, this.probeIntervalMs)]).pipe(
      switchMap(([browserOnline]) => {
        if (!browserOnline) return of(false);
        return from(this.pingServer()).pipe(catchError(() => of(false)));
      }),
      distinctUntilChanged(),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    browserOnline$.subscribe((online) => this._browserOnline.set(online));
    this.isServerReachable$.subscribe((reachable) => this._serverReachable.set(reachable));

    this.isOnline$ = combineLatest([browserOnline$, this.isServerReachable$]).pipe(
      map(([browserOnline, serverReachable]) => browserOnline && serverReachable),
      distinctUntilChanged(),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    this.isOnline$.pipe(
      pairwise(),
      filter(([prev, curr]) => !prev && curr),
      map(() => undefined)
    ).subscribe(() => {
      console.log('[NetworkService] Connection restored');
      this.connectionRestoredSubject.next();
    });
  }

  get isOnline(): boolean {
    return this.isOnlineSignal();
  }

  get isBrowserOnline(): boolean {
    return this._browserOnline();
  }

  get isServerReachable(): boolean {
    return this._serverReachable();
  }

  get offlineReason(): OfflineReason {
    if (!this._browserOnline()) return 'NETWORK_DOWN';
    if (!this._serverReachable()) return 'SERVER_UNREACHABLE';
    return 'ONLINE';
  }
  private buildBrowserOnlineStream(): Observable<boolean> {
    if (typeof window === 'undefined') {
      return of(true).pipe(shareReplay({ bufferSize: 1, refCount: true }));
    }

    return merge(
      of(window.navigator.onLine),
      fromEvent(window, 'online').pipe(map(() => true)),
      fromEvent(window, 'offline').pipe(map(() => false))
    ).pipe(
      distinctUntilChanged(),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  private resolveProbeUrl(): string {
    const host = (typeof window !== 'undefined' && window.location.hostname) || 'localhost';
    const url = `http://${host}:8080/api/health`;
    console.log('[NetworkService] Initializing connectivity probe to:', url);
    return url;
  }

  private async pingServer(): Promise<boolean> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.probeTimeoutMs);
    try {
      await fetch(this.probeUrl, {
        method: 'GET',
        signal: controller.signal,
      });
      return true;
    } catch {
      return false;
    } finally {
      clearTimeout(timeoutId);
    }
  }
}
