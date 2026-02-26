import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListEvaluationComponent } from './list-evaluation/list-evaluation.component';
import { AddEvaluationComponent } from './add-evaluation/add-evaluation.component';
import { EditEvaluationComponent } from './edit-evaluation/edit-evaluation.component';



@NgModule({
  declarations: [
    ListEvaluationComponent,
    AddEvaluationComponent,
    EditEvaluationComponent
  ],
  imports: [
    CommonModule
  ]
})
export class EvaluationModule { }
