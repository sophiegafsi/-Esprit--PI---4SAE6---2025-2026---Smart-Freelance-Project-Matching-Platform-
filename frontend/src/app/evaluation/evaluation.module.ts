import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EvaluationRoutingModule } from './evaluation-routing.module';
import { AddEvaluationComponent } from './add-evaluation/add-evaluation.component';
import { EditEvaluationComponent } from './edit-evaluation/edit-evaluation.component';
import { ListEvaluationComponent } from './list-evaluation/list-evaluation.component';

@NgModule({
  declarations: [AddEvaluationComponent, EditEvaluationComponent, ListEvaluationComponent],
  imports: [CommonModule, FormsModule, RouterModule, EvaluationRoutingModule]
})
export class EvaluationModule {}
