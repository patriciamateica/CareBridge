import { Injectable, signal } from '@angular/core';
import { fromEvent, merge, Observable, of } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class NetworkService {
  private readonly _isOnline = signal<boolean>(window.navigator.onLine);
  readonly isOnlineSignal = this._isOnline.asReadonly();

  readonly isOnline$: Observable<boolean>;

  constructor() {
    this.isOnline$ = merge(
      of(window.navigator.onLine),
      fromEvent(window, 'online').pipe(map(() => true)),
      fromEvent(window, 'offline').pipe(map(() => false))
    ).pipe(
      shareReplay(1)
    );

    this.isOnline$.subscribe(online => {
      this._isOnline.set(online);
    });
  }

  get isOnline(): boolean {
    return this._isOnline();
  }
}
