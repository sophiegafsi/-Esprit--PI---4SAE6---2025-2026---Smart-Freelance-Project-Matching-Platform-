import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Booking } from '../../models/booking.model';
import { BookingService } from '../../services/booking.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-booking-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './booking-list.component.html',
  styleUrls: ['./booking-list.component.css']
})
export class BookingListComponent implements OnInit {
  bookings: Booking[] = [];
  filteredBookings: Booking[] = [];
  loading = false;
  error = '';
  activeFilter: 'ALL' | 'CONFIRMED' | 'PENDING' | 'CANCELLED' = 'ALL';

  simulatedRole: 'CLIENT' | 'FREELANCER' = 'CLIENT';
  simulatedUserId: string = 'DaliClient';

  constructor(private bookingService: BookingService, private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.currentUserId$.subscribe(id => {
      this.simulatedUserId = id;
      this.simulatedRole = this.notificationService.getCurrentRole();
      this.loadBookings();
    });
  }

  loadBookings(): void {
    this.loading = true;
    this.error = '';

    const fetchObservable = this.simulatedRole === 'CLIENT' 
      ? this.bookingService.getByUser(this.simulatedUserId) 
      : this.bookingService.getByFreelancer(this.simulatedUserId);

    fetchObservable.subscribe({
      next: (data: any) => {
        this.bookings = data.sort((a: any, b: any) =>
          new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime()
        );
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load bookings. Make sure the backend is running.';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredBookings = [...this.bookings];
    } else {
      this.filteredBookings = this.bookings.filter(b => b.status === this.activeFilter);
    }
  }

  setFilter(filter: 'ALL' | 'CONFIRMED' | 'PENDING' | 'CANCELLED'): void {
    this.activeFilter = filter;
    this.applyFilter();
  }

  cancelBooking(id: number | undefined): void {
    if (!id) return;
    if (confirm('Cancel this booking?')) {
      this.bookingService.cancel(id).subscribe({
        next: () => this.loadBookings(),
        error: (err) => this.error = err?.error?.message || 'Failed to cancel booking'
      });
    }
  }

  confirmBooking(id: number | undefined): void {
    if (!id) return;
    this.bookingService.confirm(id).subscribe({
      next: () => this.loadBookings(),
      error: (err) => this.error = err?.error?.message || 'Failed to confirm booking'
    });
  }

  deleteBooking(id: number | undefined): void {
    if (!id) return;
    if (confirm('Permanently delete this booking?')) {
      this.bookingService.delete(id).subscribe({
        next: () => this.loadBookings(),
        error: () => this.error = 'Failed to delete booking'
      });
    }
  }

  getCount(status: string): number {
    if (status === 'ALL') return this.bookings.length;
    return this.bookings.filter(b => b.status === status).length;
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString();
  }
}
