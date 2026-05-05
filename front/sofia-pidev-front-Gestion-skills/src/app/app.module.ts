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
import { ApplyComponent } from './candidature/apply/apply.component';
import { MyApplicationsComponent } from './candidature/my-applications/my-applications.component';
import { ProjectApplicationsComponent } from './candidature/project-applications/project-applications.component';
import { ClientProjectsComponent } from './candidature/client-projects/client-projects.component';
import { SignaturePadComponent } from './shared/components/signature-pad/signature-pad.component';
import { ContractSignatureModalComponent } from './shared/components/contract-signature-modal/contract-signature-modal.component';
import { PremiumInputComponent } from './shared/components/premium-controls/premium-input.component';
import { PremiumTextareaComponent } from './shared/components/premium-controls/premium-textarea.component';
import { PremiumSelectComponent } from './shared/components/premium-controls/premium-select.component';
import { PremiumFileUploadComponent } from './shared/components/premium-controls/premium-file-upload.component';
import { TimeTrackerComponent } from './time-tracker/time-tracker.component';
import { WorkReviewComponent } from './work-review/work-review.component';

// Reclamations (Standalone)
import { ReclamationListComponent } from './reclamation-list/reclamation-list';
import { ReclamationFormComponent } from './reclamation-form/reclamation-form';
import { ReclamationDetailComponent } from './reclamation-detail/reclamation-detail';
import { ReponseFormComponent } from './reponse-form/reponse-form';
import { ReponseListComponent } from './reponse-list/reponse-list';

// Planning (Standalone)
import { ListPlanning } from './planning/pages/list-planning/list-planning';
import { PlanningDetail } from './planning/pages/planning-detail/planning-detail';
import { AddPlanning } from './planning/pages/add-planning/add-planning';
import { EditPlanning } from './planning/pages/edit-planning/edit-planning';
import { PopupComponent } from './planning/pages/popup/popup';

// Reservation
import { BrowseFreelancersComponent } from './reservation/pages/browse-freelancers/browse-freelancers.component';
import { AddAvailabilityComponent } from './reservation/pages/add-availability/add-availability.component';
import { MyAvailabilitiesComponent } from './reservation/pages/my-availabilities/my-availabilities.component';
import { MyBookingsComponent } from './reservation/pages/my-bookings/my-bookings.component';

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
    ProfileClientComponent,
    ApplyComponent,
    MyApplicationsComponent,
    ProjectApplicationsComponent,
    ClientProjectsComponent,
    SignaturePadComponent,
    ContractSignatureModalComponent,
    PremiumInputComponent,
    PremiumTextareaComponent,
    PremiumSelectComponent,
    PremiumFileUploadComponent,
    TimeTrackerComponent,
    WorkReviewComponent,
    // Reservation
    BrowseFreelancersComponent,
    AddAvailabilityComponent,
    MyAvailabilitiesComponent,
    MyBookingsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReclamationListComponent,
    ReclamationFormComponent,
    ReclamationDetailComponent,
    ReponseFormComponent,
    ReponseListComponent,
    // Planning
    ListPlanning,
    PlanningDetail,
    AddPlanning,
    EditPlanning,
    PopupComponent
  ],
  providers: [
    provideClientHydration(),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    AuthService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
