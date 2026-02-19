import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
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

@NgModule({
  declarations: [
    AppComponent,
    AccueilComponent,
    ProfileJobComponent,
    ProfileFreelancerComponent,
    EvaluationCreateComponent,
    EvaluationListPageComponent,
    EvaluationEditComponent,
    ReviewListComponent,
    ReviewAddComponent,
     ReviewEditComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [
    provideClientHydration(),
    provideHttpClient(withFetch())
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
