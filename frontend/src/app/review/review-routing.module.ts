import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AddReviewComponent } from './add-review/add-review.component';
import { EditReviewComponent } from './edit-review/edit-review.component';
import { ListReviewComponent } from './list-review/list-review.component';

const routes: Routes = [
  { path: 'add-review', component: AddReviewComponent },
  { path: 'edit-review/:id', component: EditReviewComponent },
  { path: 'list-review', component: ListReviewComponent },
  { path: '', pathMatch: 'full', redirectTo: 'list-review' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReviewRoutingModule {}
