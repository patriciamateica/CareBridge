import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserRegistration } from './user-registration';
import { MessageService } from 'primeng/api';
import { provideRouter } from '@angular/router';

describe('UserRegistration', () => {
  let component: UserRegistration;
  let fixture: ComponentFixture<UserRegistration>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserRegistration],
      providers: [
        MessageService,
        provideRouter([])
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(UserRegistration);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
