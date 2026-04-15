import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Availability } from '../../models/availability.model';
import { AvailabilityService } from '../../services/availability.service';
import { BookingService } from '../../services/booking.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-availability-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './availability-list.component.html',
  styleUrls: ['./availability-list.component.css']
})
export class AvailabilityListComponent implements OnInit {
  availabilities: Availability[] = [];
  filteredAvailabilities: Availability[] = [];
  loading = false;
  error = '';
  searchQuery = '';
  filterDate = '';
  slotsMap: Map<number, number> = new Map();
  
  simulatedUserId = '';
  simulatedRole = '';

  constructor(
    private availabilityService: AvailabilityService,
    private bookingService: BookingService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.notificationService.currentUserId$.subscribe(id => {
      this.simulatedUserId = id;
      this.simulatedRole = this.notificationService.getCurrentRole();
    });
    this.loadAvailabilities();
  }

  loadAvailabilities(): void {
    this.loading = true;
    this.error = '';
    this.availabilityService.getAll().subscribe({
      next: (data) => {
        this.availabilities = data;
        this.filteredAvailabilities = data;
        this.loading = false;
        data.forEach(a => {
          if (a.id) this.loadSlots(a.id);
        });
      },
      error: (err) => {
        this.error = 'Failed to load availabilities. Make sure the backend is running. Details: ' + (err?.message || JSON.stringify(err));
        console.error('List Error:', err);
        this.loading = false;
      }
    });
  }

  loadSlots(availabilityId: number): void {
    this.bookingService.getAvailableSlots(availabilityId).subscribe({
      next: (res) => this.slotsMap.set(availabilityId, res.availableSlots),
      error: () => {}
    });
  }

  getAvailableSlots(id: number | undefined): number {
    if (!id) return 0;
    return this.slotsMap.get(id) ?? 0;
  }

  filterAvailabilities(): void {
    let filtered = [...this.availabilities];
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      filtered = filtered.filter(a =>
        a.resourceName.toLowerCase().includes(q) ||
        a.description.toLowerCase().includes(q) ||
        a.location.toLowerCase().includes(q)
      );
    }
    if (this.filterDate) {
      filtered = filtered.filter(a => a.date === this.filterDate);
    }
    this.filteredAvailabilities = filtered;
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.filterDate = '';
    this.filteredAvailabilities = [...this.availabilities];
  }

  deleteAvailability(id: number | undefined): void {
    if (!id) return;
    if (confirm('Are you sure you want to delete this availability?')) {
      this.availabilityService.delete(id).subscribe({
        next: () => this.loadAvailabilities(),
        error: () => this.error = 'Failed to delete availability'
      });
    }
  }

  getStatusClass(id: number | undefined): string {
    const slots = this.getAvailableSlots(id);
    if (slots === 0) return 'status-full';
    if (slots <= 2) return 'status-low';
    return 'status-available';
  }

  getStatusLabel(id: number | undefined, maxSlots: number): string {
    const slots = this.getAvailableSlots(id);
    if (slots === 0) return 'Full';
    return `${slots}/${maxSlots} slots`;
  }
}
