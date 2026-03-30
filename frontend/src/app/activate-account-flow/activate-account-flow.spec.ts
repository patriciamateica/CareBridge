import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivateAccountFlow } from './activate-account-flow';
import { MessageService } from 'primeng/api';
import { provideRouter } from '@angular/router';

describe('ActivateAccountFlow', () => {
  let component: ActivateAccountFlow;
  let fixture: ComponentFixture<ActivateAccountFlow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivateAccountFlow],
      providers: [
        MessageService,
        provideRouter([])
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ActivateAccountFlow);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
