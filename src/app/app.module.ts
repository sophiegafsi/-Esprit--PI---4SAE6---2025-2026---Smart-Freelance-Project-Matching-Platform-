import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, provideHttpClient, withFetch } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Pages principales
import { AccueilComponent } from './accueil/accueil.component';
import { ProfileJobComponent } from './profile-job/profile-job.component';
import { ProfileFreelancerComponent } from './profile-freelancer/profile-freelancer.component';


import { ListEvaluationComponent } from './components/evaluation/list-evaluation/list-evaluation.component';
import { AddEvaluationComponent } from './components/evaluation/add-evaluation/add-evaluation.component';
import { EditEvaluationComponent } from './components/evaluation/edit-evaluation/edit-evaluation.component';


import { ListReviewComponent } from './components/review/list-review/list-review.component';
import { AddReviewComponent } from './components/review/add-review/add-review.component';
import { EditReviewComponent } from './components/review/edit-review/edit-review.component';
import { HistoriqueComponent } from './historique/historique.component';
import { DashboardClientComponent } from './dashboard-client/dashboard-client.component';







@NgModule({
  declarations: [
    AppComponent,
    AccueilComponent,
    ProfileJobComponent,
    ProfileFreelancerComponent,
    ListEvaluationComponent,
    AddEvaluationComponent,
    EditEvaluationComponent,
    ListReviewComponent,
    AddReviewComponent,
    EditReviewComponent,
    HistoriqueComponent,
    DashboardClientComponent,
   

    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,      // pour ngModel
    HttpClientModule , // pour HTTP
    RouterModule      // pour routerLink et router-outlet
  ],
  providers: [
    provideClientHydration(),
    provideHttpClient(withFetch())
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}