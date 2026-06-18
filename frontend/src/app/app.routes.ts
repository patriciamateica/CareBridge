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
import {ObservationList} from './audit/observation-list/observation-list';
import {AuditLogsComponent} from './audit/audit-logs/audit-logs';
import {MedicationComponent} from './medication/medication';
import {MedicalRecordsComponent} from './medical-records/medical-records';
import {FindNurseComponent} from './find-nurse/find-nurse';
import {CareVillageComponent} from './care-village/care-village';
import {NurseRequestsComponent} from './nurse-requests/nurse-requests';

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
      { path: 'home-patient', title: 'Home Patient', component: HomePatient },
      { path: 'patient-management', title: 'Patients', component: PatientManagement },
      { path: 'patient-management/:id', title: 'Patient Detail', component: PatientDetail },
      {path:'nurse-appointments', title: 'Appointments Nurse', component: NurseAppointmentsComponent },
      { path: 'patient-appointments', title: 'Appointments Patient', component: PatientAppointmentsComponent },
      { path: 'chat', title: 'Community Chat', component: ChatComponent },
       { path: 'observation-list', title: 'Security Monitor', component: ObservationList},
       { path: 'audit-logs', title: 'System Audit Logs', component: AuditLogsComponent},
       { path: 'medication', title: 'Medication', component: MedicationComponent },
       { path: 'records', title: 'Medical Records', component: MedicalRecordsComponent },
       { path: 'find-nurse', title: 'Find a Nurse', component: FindNurseComponent },
       { path: 'village', title: 'Care Village', component: CareVillageComponent },
       { path: 'requests', title: 'Requests', component: NurseRequestsComponent },
       { path: '', redirectTo: 'home-nurse', pathMatch: 'full' }
    ]
  }
];
