import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserLogin } from './user-login';
import { MessageService } from 'primeng/api';
import { provideRouter } from '@angular/router';

describe('UserLogin', () => {
  let component: UserLogin;
  let fixture: ComponentFixture<UserLogin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserLogin],
      providers: [
        MessageService,
        provideRouter([])
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(UserLogin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
