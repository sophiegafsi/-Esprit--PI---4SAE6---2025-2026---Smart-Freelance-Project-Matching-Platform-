import { Routes } from '@angular/router';
import { AvailabilityListComponent } from './components/availability-list/availability-list.component';
import { AvailabilityFormComponent } from './components/availability-form/availability-form.component';
import { BookingListComponent } from './components/booking-list/booking-list.component';
import { BookingFormComponent } from './components/booking-form/booking-form.component';
import { FreelancerAvailabilityListComponent } from './components/freelancer-availability-list/freelancer-availability-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/availabilities', pathMatch: 'full' },
  { path: 'availabilities', component: AvailabilityListComponent },
  { path: 'availabilities/new', component: AvailabilityFormComponent },
  { path: 'availabilities/edit/:id', component: AvailabilityFormComponent },
  { path: 'my-availabilities', component: FreelancerAvailabilityListComponent },
  { path: 'bookings', component: BookingListComponent },
  { path: 'bookings/new', component: BookingFormComponent },
  { path: '**', redirectTo: '/availabilities' }
];
