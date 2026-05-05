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

import { ApplyComponent } from './candidature/apply/apply.component';
import { MyApplicationsComponent } from './candidature/my-applications/my-applications.component';
import { ProjectApplicationsComponent } from './candidature/project-applications/project-applications.component';
import { ClientProjectsComponent } from './candidature/client-projects/client-projects.component';

// Imported from projectfront
import { DashboardComponent } from './projet-dashboard/dashboard';
import { ListeProjetsComponent } from './liste-projets/liste-projets';
import { PostulerProjetComponent } from './postuler-projet/postuler-projet';
import { ProjetDetailComponent } from './projet-detail/projet-detail';
import { UpdateProjetComponent } from './update-projet/update-projet';
import { AddProjetDetailComponent } from './add-projet-detail/add-projet-detail';
import { UpdateTaskComponent } from './update-task/update-task';
import { KanbanBoardComponent } from './kanban-board/kanban-board';
import { TimeTrackerComponent } from './time-tracker/time-tracker.component';
import { WorkReviewComponent } from './work-review/work-review.component';

import { ReclamationListComponent } from './reclamation-list/reclamation-list';
import { ReclamationFormComponent } from './reclamation-form/reclamation-form';
import { ReclamationDetailComponent } from './reclamation-detail/reclamation-detail';

// Planning
import { ListPlanning } from './planning/pages/list-planning/list-planning';
import { PlanningDetail } from './planning/pages/planning-detail/planning-detail';
import { AddPlanning } from './planning/pages/add-planning/add-planning';
import { EditPlanning } from './planning/pages/edit-planning/edit-planning';

// Reservation
import { BrowseFreelancersComponent } from './reservation/pages/browse-freelancers/browse-freelancers.component';
import { AddAvailabilityComponent } from './reservation/pages/add-availability/add-availability.component';
import { MyAvailabilitiesComponent } from './reservation/pages/my-availabilities/my-availabilities.component';
import { MyBookingsComponent } from './reservation/pages/my-bookings/my-bookings.component';

const routes: Routes = [
  { path: '', component: AccueilComponent },
  { path: 'accueil', component: AccueilComponent },
  { path: 'profile-job', component: ProfileJobComponent },
  { path: 'profile-freelancer', component: ProfileFreelancerComponent, canActivate: [FreelancerGuard] },
  { path: 'profile-client', component: ProfileClientComponent, canActivate: [ClientGuard] },
  { path: 'client-projects', redirectTo: 'projet-dashboard', pathMatch: 'full' },
  { path: 'apply/:projectId', component: ApplyComponent, canActivate: [FreelancerGuard] },
  { path: 'my-applications', component: MyApplicationsComponent, canActivate: [FreelancerGuard] },
  { path: 'project-applications/:projectId', component: ProjectApplicationsComponent, canActivate: [ClientGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'become-freelancer', component: BecomeFreelancerComponent },
  { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [AdminGuard] },

  // New routes for Projet management
  { path: 'projet-dashboard', component: DashboardComponent, canActivate: [ClientGuard] },
  { path: 'projets', component: ListeProjetsComponent }, // Open to all (Freelancers & Clients)
  { path: 'postuler', component: PostulerProjetComponent, canActivate: [ClientGuard] },
  { path: 'projet/:id', component: ProjetDetailComponent }, // Open
  { path: 'update-projet/:id', component: UpdateProjetComponent, canActivate: [ClientGuard] },
  { path: 'projet/:id/add-details', component: AddProjetDetailComponent, canActivate: [ClientGuard] },
  { path: 'task/edit/:projectId/:taskId', component: UpdateTaskComponent, canActivate: [ClientGuard] },
  { path: 'kanban', component: KanbanBoardComponent, canActivate: [ClientGuard] },

  // Time Tracking 
  { path: 'work-review/:contractId', component: WorkReviewComponent, canActivate: [ClientGuard] },

  // Reclamations
  { path: 'reclamations', component: ReclamationListComponent },
  { path: 'reclamations/new', component: ReclamationFormComponent },
  { path: 'reclamations/:id', component: ReclamationDetailComponent },
  { path: 'reclamations/edit/:id', component: ReclamationFormComponent },

  // Skills & Portfolio
  { path: 'skills', loadChildren: () => import('./skills/skills.module').then(m => m.SkillsModule), canActivate: [FreelancerGuard] },
  { path: 'skills-proof', loadChildren: () => import('./skills-proof/skills-proof.module').then(m => m.SkillsProofModule), canActivate: [FreelancerGuard] },
  { path: 'portfolio', loadChildren: () => import('./portfolio/portfolio.module').then(m => m.PortfolioModule), canActivate: [FreelancerGuard] },

  // Planning
  { path: 'planning', component: ListPlanning, canActivate: [FreelancerGuard] },
  { path: 'planning/new', component: AddPlanning, canActivate: [FreelancerGuard] },
  { path: 'planning/:id', component: PlanningDetail, canActivate: [FreelancerGuard] },
  { path: 'planning/:id/edit', component: EditPlanning, canActivate: [FreelancerGuard] },

  // Reservation
  { path: 'browse-freelancers', component: BrowseFreelancersComponent },
  { path: 'my-availabilities', component: MyAvailabilitiesComponent, canActivate: [FreelancerGuard] },
  { path: 'my-bookings', component: MyBookingsComponent },
  { path: 'availability/new', component: AddAvailabilityComponent, canActivate: [FreelancerGuard] },
  { path: 'availability/:id/edit', component: AddAvailabilityComponent, canActivate: [FreelancerGuard] },

  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
