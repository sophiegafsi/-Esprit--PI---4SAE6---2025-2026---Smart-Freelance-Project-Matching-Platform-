import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReviewListComponent } from './components/review-list/review-list.component';
import { ReviewAddComponent } from './components/review-add/review-add.component';

const routes: Routes = [
  { path: 'reviews', component: ReviewListComponent },
  { path: 'reviews/add', component: ReviewAddComponent },
  { path: 'reviews/edit/:id', component: ReviewAddComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReviewRoutingModule {}
