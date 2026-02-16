import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './services/auth.interceptor';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AccueilComponent } from './accueil/accueil.component';
import { ProfileJobComponent } from './profile-job/profile-job.component';
import { ProfileFreelancerComponent } from './profile-freelancer/profile-freelancer.component';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { AuthService } from './services/auth.service';
import { VerifyEmailComponent } from './verify-email/verify-email.component';
import { NavbarComponent } from './navbar/navbar.component';
import { BecomeFreelancerComponent } from './become-freelancer/become-freelancer.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { ProfileClientComponent } from './profile-client/profile-client.component';

@NgModule({
  declarations: [
    AppComponent,
    AccueilComponent,
    ProfileJobComponent,
    ProfileFreelancerComponent,
    LoginComponent,
    SignupComponent,
    ForgotPasswordComponent,
    VerifyEmailComponent,
    NavbarComponent,
    BecomeFreelancerComponent,
    AdminDashboardComponent,
    ProfileClientComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [
    provideClientHydration(),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    AuthService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
