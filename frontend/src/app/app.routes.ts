import { Routes } from '@angular/router';
import {LandingPage} from './landing-page/landing-page';
import {HomeNurse} from './home-nurse/home-nurse';
import {MainLayout} from './main-layout/main-layout';
import {PatientManagement} from './patient/feature/patient-management/patient-management';
import {PatientDetail} from './patient/patient-detail/patient-detail';
import {UserRegistration} from './user-registration/user-registration';
import {ActivateAccountFlow} from './activate-account-flow/activate-account-flow';
import {UserLogin} from './user-login/user-login';
import {ForgotPassword} from './forgot-password-flow/forgot-password-flow';
import {HomePatient} from './home-patient/home-patient';

export const routes: Routes = [
  {
    path: '',
    component: LandingPage
  },

  {
    path: 'user-registration',
    component: UserRegistration,
  },
  {
    path: 'activate-account-flow',
    component: ActivateAccountFlow,
  },

  {
    path:'user-login',
    component: UserLogin,
  },
  {
    path: 'forgot-password-flow',
    component: ForgotPassword
  },

  {
    path: 'dashboard',
    component: MainLayout,
    children: [
      { path: 'home-nurse', title: 'Home Nurse', component: HomeNurse },
      {path: 'home-patient', title: 'Home Patient', component: HomePatient },
      { path: 'patient-management', title: 'Patients', component: PatientManagement },
      { path: 'patient-management/:id', title: 'Patient Detail', component: PatientDetail },
      { path: '', redirectTo: 'home-nurse', pathMatch: 'full' }
    ]
  }
];
