import { Routes } from '@angular/router';
import {ThemeShowcaseComponent} from './theme/feature/theme-showcase/theme-showcase';
import {LandingPage} from './landing-page/landing-page';
import {HomeNurse} from './home-nurse/home-nurse';
import {Patient} from './patient/patient';
import {MainLayout} from './main-layout/main-layout';

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
      { path: 'patient', title: 'Patient', component: Patient },
      { path: 'theme', title: 'Theme', component: ThemeShowcaseComponent },
      { path: '', redirectTo: 'home-nurse', pathMatch: 'full' }
    ]
  }
];
