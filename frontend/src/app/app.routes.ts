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
import {NurseAppointmentsComponent} from './nurse-appointments/nurse-appointments';
import {PatientAppointmentsComponent} from './patient-appointments/patient-appointments';
import {ChatComponent} from './chat/chat';

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
      {path:'nurse-appointments', title: 'Appointments Nurse', component: NurseAppointmentsComponent },
      { path: 'patient-appointments', title: 'Appointments Patient', component: PatientAppointmentsComponent },
      { path: 'chat', title: 'Community Chat', component: ChatComponent },
      { path: 'observation-list', title: 'Security Monitor', loadComponent: () => import('./audit/observation-list/observation-list').then(m => m.ObservationList) },
      { path: '', redirectTo: 'home-nurse', pathMatch: 'full' }
    ]
  }
];
