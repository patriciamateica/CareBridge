import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Sidebar} from './sidebar';
import {Router} from '@angular/router';
import {provideRouter} from '@angular/router';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';

describe('Sidebar', () => {
  let component: Sidebar;
  let fixture: ComponentFixture<Sidebar>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sidebar, NoopAnimationsModule],
      providers: [
        provideRouter([]),
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Sidebar);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('should start with drawer hidden', () => {
    expect(component.visible).toBeFalse();
  });

  it('should default userRole to admin', () => {
    expect(component.userRole).toBe('admin');
  });

  it('should have 2 menu sections', () => {
    expect(component.menuSections.length).toBe(2);
  });

  it('should have a MAIN section', () => {
    expect(component.menuSections.find(s => s.label === 'MAIN')).toBeDefined();
  });

  it('should have a MEDICAL section', () => {
    expect(component.menuSections.find(s => s.label === 'MEDICAL')).toBeDefined();
  });

  it('every section should have at least one item', () => {
    component.menuSections.forEach(s => expect(s.items.length).toBeGreaterThan(0));
  });

  it('every item should have a path and title', () => {
    component.menuSections.forEach(s =>
      s.items.forEach(item => {
        expect(item.path).toBeTruthy();
        expect(item.title).toBeTruthy();
      })
    );
  });

  it('every item path should start with /dashboard/', () => {
    component.menuSections.forEach(s =>
      s.items.forEach(item => expect(item.path).toMatch(/^\/dashboard\//))
    );
  });

  it('all sections should be visible to admin', () => {
    component.userRole;
    const visible = component.menuSections.filter(s => s.roles.includes('admin'));
    expect(visible.length).toBe(2);
  });

  it('nurse should see MAIN and MEDICAL sections', () => {
    const visible = component.menuSections.filter(s => s.roles.includes('nurse'));
    expect(visible.length).toBe(2);
  });

  it('patient should see MAIN and MEDICAL sections', () => {
    const visible = component.menuSections.filter(s => s.roles.includes('patient'));
    expect(visible.length).toBe(2);
  });

  it('nurse should see Patients item in MEDICAL', () => {
    const medical = component.menuSections.find(s => s.label === 'MEDICAL')!;
    const titles = medical.items.filter(i => i.roles.includes('nurse')).map(i => i.title);
    expect(titles).toContain('Patients');
  });

  it('patient should NOT see Patients item in MEDICAL', () => {
    const medical = component.menuSections.find(s => s.label === 'MEDICAL')!;
    const titles = medical.items.filter(i => i.roles.includes('patient')).map(i => i.title);
    expect(titles).not.toContain('Patients');
  });

  it('nurse should NOT see Care Village in MAIN', () => {
    const main = component.menuSections.find(s => s.label === 'MAIN')!;
    const titles = main.items.filter(i => i.roles.includes('nurse')).map(i => i.title);
    expect(titles).not.toContain('Care Village');
  });

  it('patient should NOT see Home Nurse in MAIN', () => {
    const main = component.menuSections.find(s => s.label === 'MAIN')!;
    const titles = main.items.filter(i => i.roles.includes('patient')).map(i => i.title);
    expect(titles).not.toContain('Home Nurse');
  });

  it('Medication should be visible to all roles', () => {
    const medical = component.menuSections.find(s => s.label === 'MEDICAL')!;
    const medication = medical.items.find(i => i.title === 'Medication')!;
    expect(medication.roles).toContain('admin');
    expect(medication.roles).toContain('nurse');
    expect(medication.roles).toContain('patient');
  });

  it('setting visible to true opens drawer', () => {
    component.visible = true;
    expect(component.visible).toBeTrue();
  });

  it('setting visible to false closes drawer', () => {
    component.visible = true;
    component.visible = false;
    expect(component.visible).toBeFalse();
  });

  it('logout() should navigate to landing page', () => {
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.logout();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

});
