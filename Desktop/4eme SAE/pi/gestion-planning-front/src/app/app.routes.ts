import { Routes } from '@angular/router';
import { ListPlanning } from './pages/list-planning/list-planning';
import { AddPlanning } from './pages/add-planning/add-planning';
import { EditPlanning } from './pages/edit-planning/edit-planning';
import { PlanningDetail } from './pages/planning-detail/planning-detail';

export const routes: Routes = [
  { path: '', redirectTo: 'plannings', pathMatch: 'full' },
  { path: 'plannings', component: ListPlanning },
  { path: 'plannings/add', component: AddPlanning },
  { path: 'plannings/edit/:id', component: EditPlanning },
  { path: 'plannings/:id', component: PlanningDetail }
];