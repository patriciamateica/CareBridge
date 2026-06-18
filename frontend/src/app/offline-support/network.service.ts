import { Injectable, computed, signal } from '@angular/core';
import { fromEvent, merge, Observable, of, Subject } from 'rxjs';
import { distinctUntilChanged, filter, map, pairwise, shareReplay } from 'rxjs/operators';

type OfflineReason = 'NETWORK_DOWN' | 'ONLINE';

@Injectable({ providedIn: 'root' })
export class NetworkService {
  private readonly _browserOnline = signal<boolean>(typeof window === 'undefined' ? true : window.navigator.onLine);
  private readonly connectionRestoredSubject = new Subject<void>();

  readonly isBrowserOnlineSignal = this._browserOnline.asReadonly();
  readonly isServerReachableSignal = computed(() => true);
  readonly isOnlineSignal = computed(() => this._browserOnline());

  readonly isServerReachable$: Observable<boolean> = of(true);
  readonly isOnline$: Observable<boolean>;
  readonly connectionRestored$ = this.connectionRestoredSubject.asObservable();

  constructor() {
    const browserOnline$ = this.buildBrowserOnlineStream();

    browserOnline$.subscribe((online) => this._browserOnline.set(online));

    this.isOnline$ = browserOnline$.pipe(
      distinctUntilChanged(),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    this.isOnline$.pipe(
      pairwise(),
      filter(([prev, curr]) => !prev && curr),
      map(() => undefined)
    ).subscribe(() => {
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
    return true;
  }

  get offlineReason(): OfflineReason {
    if (!this._browserOnline()) return 'NETWORK_DOWN';
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
}
