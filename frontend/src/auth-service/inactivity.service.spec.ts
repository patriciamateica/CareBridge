import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { InactivityService } from './inactivity.service';
import { AuthService } from './auth.service';
import { ToastService } from '../app/toast-service/toast-service';
import { UserRole } from './auth.service';

describe('InactivityService', () => {
  let service: InactivityService;
  let routerSpy: jasmine.SpyObj<Router>;
  let toastSpy:  jasmine.SpyObj<ToastService>;

  const makeAuthStub = (loggedIn: boolean) => {
    const rolesSignal = signal<UserRole[]>(loggedIn ? ['Nurse'] : []);
    return {
      currentRoles: rolesSignal,
      logout: jasmine.createSpy('logout'),
    };
  };

  let authStub: ReturnType<typeof makeAuthStub>;

  const setup = (loggedIn = true) => {
    authStub  = makeAuthStub(loggedIn);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);
    toastSpy  = jasmine.createSpyObj<ToastService>('ToastService', ['showError', 'showSuccess']);

    TestBed.configureTestingModule({
      providers: [
        InactivityService,
        { provide: AuthService,  useValue: authStub  },
        { provide: Router,       useValue: routerSpy },
        { provide: ToastService, useValue: toastSpy  },
        provideRouter([]),
      ],
    });

    service = TestBed.inject(InactivityService);
  };

  afterEach(() => {
    service.stopWatching();
    TestBed.resetTestingModule();
  });


  it('should be created', () => {
    setup();
    expect(service).toBeTruthy();
  });


  it('should call logout and navigate after the inactivity timeout', fakeAsync(() => {
    setup(true);

    tick(InactivityService.TIMEOUT_MS);

    expect(authStub.logout).toHaveBeenCalledOnceWith();
    expect(toastSpy.showError).toHaveBeenCalledWith(
      'You have been logged out due to inactivity.'
    );
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/user-login']);
  }));


  it('should NOT logout when the user stays active within the window', fakeAsync(() => {
    setup(true);

    tick(InactivityService.TIMEOUT_MS - 1000);
    service.resetTimer();

    tick(InactivityService.TIMEOUT_MS - 1000);

    expect(authStub.logout).not.toHaveBeenCalled();

    tick(InactivityService.TIMEOUT_MS);
  }));


  it('should NOT navigate when the user is already logged out at timeout', fakeAsync(() => {
    setup(false); // currentRoles() returns []

    tick(InactivityService.TIMEOUT_MS);

    expect(authStub.logout).not.toHaveBeenCalled();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  }));


  it('stopWatching() should cancel the pending timeout', fakeAsync(() => {
    setup(true);

    service.stopWatching();
    tick(InactivityService.TIMEOUT_MS);

    expect(authStub.logout).not.toHaveBeenCalled();
  }));

  it('resetTimer() should restart the full countdown', fakeAsync(() => {
    setup(true);

    tick(InactivityService.TIMEOUT_MS / 2);
    service.resetTimer();

    tick(InactivityService.TIMEOUT_MS / 2);
    expect(authStub.logout).not.toHaveBeenCalled();

    tick(InactivityService.TIMEOUT_MS / 2);
    expect(authStub.logout).toHaveBeenCalledOnceWith();
  }));
});
