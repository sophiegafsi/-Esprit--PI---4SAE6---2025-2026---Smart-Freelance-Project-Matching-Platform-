import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BadgeRoutingModule } from './badge-routing.module';
import { CreateBadgeComponent } from './create-badge/create-badge.component';
import { EditBadgeComponent } from './edit-badge/edit-badge.component';
import { ListBadgeComponent } from './list-badge/list-badge.component';

@NgModule({
  declarations: [EditBadgeComponent, ListBadgeComponent],
  imports: [CommonModule, FormsModule, RouterModule, BadgeRoutingModule, CreateBadgeComponent]
})
export class BadgeModule {}
