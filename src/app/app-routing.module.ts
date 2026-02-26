import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Pages principales
import { AccueilComponent } from './accueil/accueil.component';
import { ProfileJobComponent } from './profile-job/profile-job.component';
import { ProfileFreelancerComponent } from './profile-freelancer/profile-freelancer.component';

import { DashboardClientComponent } from './dashboard-client/dashboard-client.component';
import { ListEvaluationComponent } from './components/evaluation/list-evaluation/list-evaluation.component';
import { AddEvaluationComponent } from './components/evaluation/add-evaluation/add-evaluation.component';
import { EditEvaluationComponent } from './components/evaluation/edit-evaluation/edit-evaluation.component';

// Review
import { ListReviewComponent } from './components/review/list-review/list-review.component';
import { AddReviewComponent } from './components/review/add-review/add-review.component';
import { EditReviewComponent } from './components/review/edit-review/edit-review.component';

// Historique
import { HistoriqueComponent } from './historique/historique.component';

const routes: Routes = [
  { path: '', component: AccueilComponent },
  { path: 'accueil', component: AccueilComponent },
  { path: 'profile-job', component: ProfileJobComponent },
  { path: 'profile-freelancer', component: ProfileFreelancerComponent },
  { path: 'dashboard', component: DashboardClientComponent },

  // Evaluation Routes
  { path: '', redirectTo: '/evaluations', pathMatch: 'full' },
  { path: 'evaluations', component: ListEvaluationComponent },
  { path: 'evaluations/add', component: AddEvaluationComponent },
  { path: 'evaluations/edit/:id', component: EditEvaluationComponent },
  { path: 'evaluations/history', component: HistoriqueComponent },
  // Review Routes
  { path: 'reviews', component: ListReviewComponent },
  { path: 'reviews/add', component: AddReviewComponent },
  { path: 'reviews/edit/:id', component: EditReviewComponent },

  // Historique


  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}