import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Booking } from '../../models/booking.model';
import { Availability } from '../../models/availability.model';
import { BookingService } from '../../services/booking.service';
import { AvailabilityService } from '../../services/availability.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './booking-form.component.html',
  styleUrls: ['./booking-form.component.css']
})
export class BookingFormComponent implements OnInit {
  booking: Booking = {
    availabilityId: 0,
    userId: '',
    userName: '',
    userEmail: '',
    notes: ''
  };

  availabilities: Availability[] = [];
  selectedAvailability?: Availability;
  availableSlots = 0;
  loading = false;
  saving = false;
  error = '';
  success = '';

  constructor(
    private bookingService: BookingService,
    private availabilityService: AvailabilityService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.notificationService.currentUserId$.subscribe(id => {
      if (this.notificationService.getCurrentRole() === 'CLIENT') {
        this.booking.userId = id;
      }
    });

    this.loading = true;
    this.availabilityService.getAll().subscribe({
      next: (data) => {
        this.availabilities = data;
        this.loading = false;
        // Pre-select if availabilityId provided via query param
        const preselected = this.route.snapshot.queryParamMap.get('availabilityId');
        if (preselected) {
          this.booking.availabilityId = +preselected;
          this.onAvailabilityChange();
        }
      },
      error: () => {
        this.error = 'Could not load availabilities';
        this.loading = false;
      }
    });
  }

  onAvailabilityChange(): void {
    const id = +this.booking.availabilityId;
    if (!id) { this.selectedAvailability = undefined; return; }
    this.selectedAvailability = this.availabilities.find(a => a.id === id);
    if (id) {
      this.bookingService.getAvailableSlots(id).subscribe({
        next: (res) => this.availableSlots = res.availableSlots,
        error: () => this.availableSlots = 0
      });
    }
  }

  onSubmit(): void {
    this.saving = true;
    this.error = '';
    // Use email as userId if not set
    if (!this.booking.userId) {
      this.booking.userId = this.booking.userEmail;
    }
    this.bookingService.create(this.booking).subscribe({
      next: () => {
        this.success = '🎉 Booking confirmed successfully!';
        this.saving = false;
        setTimeout(() => this.router.navigate(['/bookings']), 1500);
      },
      error: (err) => {
        this.error = err?.error?.message || err?.error || 'Booking failed. The slot may be full or already booked.';
        this.saving = false;
      }
    });
  }
}
