import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreateBadgeComponent } from './create-badge/create-badge.component';
import { EditBadgeComponent } from './edit-badge/edit-badge.component';
import { ListBadgeComponent } from './list-badge/list-badge.component';

const routes: Routes = [
  { path: 'create-badge', component: CreateBadgeComponent },
  { path: 'edit-badge/:id', component: EditBadgeComponent },
  { path: 'list-badge', component: ListBadgeComponent },
  { path: '', pathMatch: 'full', redirectTo: 'list-badge' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BadgeRoutingModule {}
