import { Routes } from '@angular/router';
import { FormationsComponent } from './pages/formations/formations';
import { AddTrainingComponent } from './pages/add-training/add-training';

export const routes: Routes = [
  { path: 'trainings', component: FormationsComponent },
  { path: 'trainings/add', component: AddTrainingComponent },
  { path: '', redirectTo: 'trainings', pathMatch: 'full' },
  { path: '**', redirectTo: 'trainings' }
];
