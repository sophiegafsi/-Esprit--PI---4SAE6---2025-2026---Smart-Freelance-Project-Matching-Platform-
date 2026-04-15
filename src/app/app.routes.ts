import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { DashboardComponent } from './dashboard/dashboard';
import { ListeProjetsComponent } from './liste-projets/liste-projets';
import { PostulerProjetComponent } from './postuler-projet/postuler-projet';
import { ProjetDetailComponent } from './projet-detail/projet-detail';
import { UpdateProjetComponent } from './update-projet/update-projet';
import { AddProjetDetailComponent } from './add-projet-detail/add-projet-detail';
import { UpdateTaskComponent } from './update-task/update-task';
import { KanbanBoardComponent } from './kanban-board/kanban-board';
import { ReclamationListComponent } from './reclamation-list/reclamation-list';
import { ReclamationFormComponent } from './reclamation-form/reclamation-form';
import { ReclamationDetailComponent } from './reclamation-detail/reclamation-detail';

export const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  { path: 'home', component: HomeComponent },


  { path: 'dashboard', component: DashboardComponent },

  { path: 'projets', component: ListeProjetsComponent },

  { path: 'postuler', component: PostulerProjetComponent },

  { path: 'projet/:id', component: ProjetDetailComponent },

  { path: 'update-projet/:id', component: UpdateProjetComponent },

  { path: 'projet/:id/add-details', component: AddProjetDetailComponent },

  { path: 'task/edit/:projectId/:taskId', component: UpdateTaskComponent },

  { path: 'reclamations', component: ReclamationListComponent },
  { path: 'reclamations/new', component: ReclamationFormComponent },
  { path: 'reclamations/edit/:id', component: ReclamationFormComponent },
  { path: 'reclamations/:id', component: ReclamationDetailComponent },

  { path: '**', redirectTo: 'home' }
];
