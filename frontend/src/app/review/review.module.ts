import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReviewRoutingModule } from './review-routing.module';
import { AddReviewComponent } from './add-review/add-review.component';
import { EditReviewComponent } from './edit-review/edit-review.component';
import { ListReviewComponent } from './list-review/list-review.component';

@NgModule({
  declarations: [AddReviewComponent, EditReviewComponent, ListReviewComponent],
  imports: [CommonModule, FormsModule, RouterModule, ReviewRoutingModule]
})
export class ReviewModule {}
