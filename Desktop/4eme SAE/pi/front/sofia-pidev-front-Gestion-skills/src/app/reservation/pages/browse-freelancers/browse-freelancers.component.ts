import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Availability } from '../../models/availability.model';
import { Booking } from '../../models/booking.model';
import { AvailabilityService } from '../../services/availability.service';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-browse-freelancers',
    standalone: false,
    templateUrl: './browse-freelancers.component.html',
    styleUrls: ['./browse-freelancers.component.css']
})
export class BrowseFreelancersComponent implements OnInit {
    availabilities: Availability[] = [];
    filtered: Availability[] = [];
    loading = false;
    error = '';
    searchQuery = '';

    // Booking modal state
    showModal = false;
    selectedAvailability: Availability | null = null;
    booking: Partial<Booking> = { notes: '' };
    bookingLoading = false;
    bookingSuccess = '';
    bookingError = '';

    constructor(
        private availabilityService: AvailabilityService,
        private bookingService: BookingService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.loadAvailabilities();
    }

    loadAvailabilities(): void {
        this.loading = true;
        this.error = '';
        this.availabilityService.getAll().subscribe({
            next: (data) => {
                this.availabilities = data;
                this.filtered = data;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load freelancer availabilities. Please try again.';
                this.loading = false;
                console.error(err);
            }
        });
    }

    onSearch(): void {
        const q = this.searchQuery.toLowerCase().trim();
        if (!q) {
            this.filtered = this.availabilities;
            return;
        }
        this.filtered = this.availabilities.filter(a =>
            a.freelancerName?.toLowerCase().includes(q) ||
            a.description?.toLowerCase().includes(q) ||
            a.location?.toLowerCase().includes(q)
        );
    }

    clearSearch(): void {
        this.searchQuery = '';
        this.filtered = this.availabilities;
    }

    openBookingModal(availability: Availability): void {
        this.selectedAvailability = availability;
        const user = this.authService.getCurrentUserValue();
        this.booking = {
            availabilityId: availability.id!,
            userName: user?.firstName + ' ' + user?.lastName || user?.email || '',
            userEmail: user?.email || '',
            notes: ''
        };
        this.bookingSuccess = '';
        this.bookingError = '';
        this.showModal = true;
    }

    closeModal(): void {
        this.showModal = false;
        this.selectedAvailability = null;
    }

    submitBooking(): void {
        if (!this.selectedAvailability || !this.booking.userEmail) return;
        this.bookingLoading = true;
        this.bookingError = '';
        this.bookingSuccess = '';

        const payload: Booking = {
            availabilityId: this.selectedAvailability.id!,
            userId: this.authService.getCurrentUserValue()?.id?.toString() || '',
            userName: this.booking.userName || '',
            userEmail: this.booking.userEmail || '',
            notes: this.booking.notes
        };

        this.bookingService.create(payload).subscribe({
            next: () => {
                this.bookingSuccess = 'Booking confirmed! The freelancer will be notified.';
                this.bookingLoading = false;
                setTimeout(() => this.closeModal(), 2000);
            },
            error: (err) => {
                this.bookingError = err?.error?.message || err?.message || 'Booking failed. Please try again.';
                this.bookingLoading = false;
            }
        });
    }
}
