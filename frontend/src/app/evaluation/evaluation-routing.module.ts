import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddEvaluationComponent } from './add-evaluation/add-evaluation.component';
import { EditEvaluationComponent } from './edit-evaluation/edit-evaluation.component';
import { ListEvaluationComponent } from './list-evaluation/list-evaluation.component';

const routes: Routes = [
  { path: 'add-evaluation', component: AddEvaluationComponent },
  { path: 'edit-evaluation/:id', component: EditEvaluationComponent },
  { path: 'list-evaluation', component: ListEvaluationComponent },
  { path: '', pathMatch: 'full', redirectTo: 'list-evaluation' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EvaluationRoutingModule {}
