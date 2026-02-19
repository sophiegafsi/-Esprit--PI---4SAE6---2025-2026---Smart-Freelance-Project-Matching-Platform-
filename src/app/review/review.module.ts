import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewAddComponent } from './components/review-add/review-add.component';
import { ReviewListComponent } from './components/review-list/review-list.component';
import { ReviewEditComponent } from './components/review-edit/review-edit.component';

@NgModule({
  declarations: [ReviewAddComponent, ReviewListComponent, ReviewEditComponent],
  imports: [CommonModule, FormsModule],
  exports: [ReviewAddComponent, ReviewListComponent]
})
export class ReviewModule { }
