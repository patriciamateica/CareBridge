import { Routes } from '@angular/router';
import {ThemeShowcaseComponent} from './theme/feature/theme-showcase/theme-showcase';
import {LandingPage} from './landing-page/landing-page';
import {HomeNurse} from './home-nurse/home-nurse';
import {MainLayout} from './main-layout/main-layout';
import {PatientManagement} from './patient/feature/patient-management/patient-management';
import {PatientDetail} from './patient/patient-detail/patient-detail';

export const routes: Routes = [
  {
    path: '',
    component: LandingPage
  },

  {
    path: 'dashboard',
    component: MainLayout,
    children: [
      { path: 'home-nurse', title: 'Home Nurse', component: HomeNurse },
      { path: 'patient-management', title: 'Patients', component: PatientManagement },
      { path: 'patient-management/:id', title: 'Patient Detail', component: PatientDetail },
      { path: 'theme', title: 'Theme', component: ThemeShowcaseComponent },
      { path: '', redirectTo: 'home-nurse', pathMatch: 'full' }
    ]
  }
];
