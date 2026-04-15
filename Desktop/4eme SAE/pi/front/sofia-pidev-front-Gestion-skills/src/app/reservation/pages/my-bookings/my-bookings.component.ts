import { Component, OnInit } from '@angular/core';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../../services/auth.service';
import { Booking } from '../../models/booking.model';

@Component({
  selector: 'app-my-bookings',
  standalone: false,
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css']
})
export class MyBookingsComponent implements OnInit {
  bookings: Booking[] = [];
  loading = false;
  error = '';
  role: 'FREELANCER' | 'CLIENT' = 'CLIENT';

  constructor(
    private bookingService: BookingService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.role = user.roles.includes('FREELANCER') ? 'FREELANCER' : 'CLIENT';
        this.loadBookings(user.id);
      }
    });
  }

  loadBookings(userId: string): void {
    this.loading = true;
    this.error = '';

    const obs = this.role === 'FREELANCER' 
      ? this.bookingService.getMyBookingsAsFreelancer()
      : this.bookingService.getMyBookingsAsClient(userId);

    obs.subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load bookings.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  confirmBooking(id: number | undefined): void {
    if (!id) return;
    this.bookingService.confirm(id).subscribe({
      next: () => this.refreshLocally(id, 'CONFIRMED'),
      error: (err) => alert('Failed to confirm booking.')
    });
  }

  cancelBooking(id: number | undefined): void {
    if (!id) return;
    if (!confirm('Are you sure you want to cancel/refuse this booking?')) return;
    this.bookingService.cancel(id).subscribe({
      next: () => this.refreshLocally(id, 'CANCELLED'),
      error: (err) => alert('Failed to update booking status.')
    });
  }

  private refreshLocally(id: number, status: any): void {
    const booking = this.bookings.find(b => b.id === id);
    if (booking) booking.status = status;
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'status-pending';
    switch (status) {
      case 'CONFIRMED': return 'status-confirmed';
      case 'CANCELLED': return 'status-cancelled';
      case 'PENDING': return 'status-pending';
      default: return '';
    }
  }
}
