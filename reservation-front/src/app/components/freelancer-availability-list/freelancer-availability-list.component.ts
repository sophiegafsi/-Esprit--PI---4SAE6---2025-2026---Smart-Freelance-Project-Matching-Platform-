import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Availability } from '../../models/availability.model';
import { AvailabilityService } from '../../services/availability.service';
import { NotificationService } from '../../services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-freelancer-availability-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './freelancer-availability-list.component.html',
  styleUrls: ['./freelancer-availability-list.component.css']
})
export class FreelancerAvailabilityListComponent implements OnInit, OnDestroy {
  availabilities: Availability[] = [];
  loading = false;
  error = '';
  currentFreelancerName = '';
  private userSub: Subscription | undefined;

  constructor(
    private availabilityService: AvailabilityService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.userSub = this.notificationService.currentUserId$.subscribe(name => {
      this.currentFreelancerName = name;
      this.loadMyAvailabilities();
    });
  }

  ngOnDestroy(): void {
    if (this.userSub) {
      this.userSub.unsubscribe();
    }
  }

  loadMyAvailabilities(): void {
    if (!this.currentFreelancerName) return;
    
    this.loading = true;
    this.error = '';
    // Search specifically for this freelancer's name
    this.availabilityService.search(this.currentFreelancerName).subscribe({
      next: (data) => {
        this.availabilities = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load your availabilities.';
        console.error(err);
        this.loading = false;
      }
    });
  }

  deleteAvailability(id: number | undefined): void {
    if (!id) return;
    if (confirm('Are you sure you want to delete this availability?')) {
      this.availabilityService.delete(id).subscribe({
        next: () => this.loadMyAvailabilities(),
        error: () => this.error = 'Failed to delete availability'
      });
    }
  }
}
