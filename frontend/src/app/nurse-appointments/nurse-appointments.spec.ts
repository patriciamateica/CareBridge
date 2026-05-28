import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NurseAppointments } from './nurse-appointments';

describe('NurseAppointments', () => {
  let component: NurseAppointments;
  let fixture: ComponentFixture<NurseAppointments>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NurseAppointments]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NurseAppointments);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
