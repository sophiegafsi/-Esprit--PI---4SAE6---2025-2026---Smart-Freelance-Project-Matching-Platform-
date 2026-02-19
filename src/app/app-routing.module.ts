import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Pages principales
import { AccueilComponent } from './accueil/accueil.component';
import { ProfileJobComponent } from './profile-job/profile-job.component';
import { ProfileFreelancerComponent } from './profile-freelancer/profile-freelancer.component';

// Evaluation
import { EvaluationCreateComponent } from './pages/evaluation/evaluation-create/evaluation-create.component';
import { EvaluationListPageComponent } from './pages/evaluation/evaluation-list-page/evaluation-list-page.component';
import { EvaluationEditComponent } from './pages/evaluation/evaluation-edit/evaluation-edit.component';

// Review
import { ReviewListComponent } from './review/components/review-list/review-list.component';
import { ReviewAddComponent } from './review/components/review-add/review-add.component';
import { ReviewEditComponent } from './review/components/review-edit/review-edit.component';

const routes: Routes = [
  { path: '', component: AccueilComponent },
  { path: 'accueil', component: AccueilComponent },
  { path: 'profile-job', component: ProfileJobComponent },
  { path: 'profile-freelancer', component: ProfileFreelancerComponent },

  // Evaluation
  { path: 'evaluations', component: EvaluationListPageComponent },
  { path: 'evaluations/new', component: EvaluationCreateComponent },
  { path: 'evaluations/:id/edit', component: EvaluationEditComponent },

  // Review
  { path: 'reviews', component: ReviewListComponent },
  { path: 'reviews/add/:evaluationId', component: ReviewAddComponent },
  { path: 'reviews/edit/:id', component: ReviewEditComponent },

  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
