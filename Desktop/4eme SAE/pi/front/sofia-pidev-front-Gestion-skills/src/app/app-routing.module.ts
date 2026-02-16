import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccueilComponent } from './accueil/accueil.component';
import { ProfileJobComponent } from './profile-job/profile-job.component';
import { ProfileFreelancerComponent } from './profile-freelancer/profile-freelancer.component';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { VerifyEmailComponent } from './verify-email/verify-email.component';
import { BecomeFreelancerComponent } from './become-freelancer/become-freelancer.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { ProfileClientComponent } from './profile-client/profile-client.component';
import { AdminGuard } from './guards/admin.guard';
import { FreelancerGuard } from './guards/freelancer.guard';
import { ClientGuard } from './guards/client.guard';

const routes: Routes = [
  { path: '', component: AccueilComponent },
  { path: 'accueil', component: AccueilComponent },
  { path: 'profile-job', component: ProfileJobComponent },
  { path: 'profile-freelancer', component: ProfileFreelancerComponent, canActivate: [FreelancerGuard] },
  { path: 'profile-client', component: ProfileClientComponent, canActivate: [ClientGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'become-freelancer', component: BecomeFreelancerComponent },
  { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [AdminGuard] },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
