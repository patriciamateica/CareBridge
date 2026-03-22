import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeNurse } from './home-nurse';

describe('HomeNurse', () => {
  let component: HomeNurse;
  let fixture: ComponentFixture<HomeNurse>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeNurse]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomeNurse);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
