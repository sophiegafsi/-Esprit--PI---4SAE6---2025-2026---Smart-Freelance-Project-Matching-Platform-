import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Availability } from '../../models/availability.model';
import { AvailabilityService } from '../../services/availability.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-availability-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './availability-form.component.html',
  styleUrls: ['./availability-form.component.css']
})
export class AvailabilityFormComponent implements OnInit {
  availability: Availability = {
    resourceName: '',
    description: '',
    date: '',
    startTime: '',
    endTime: '',
    maxSlots: 1,
    location: '',
    isActive: true
  };
  isEditMode = false;
  editId?: number;
  loading = false;
  saving = false;
  error = '';
  success = '';

  constructor(
    private availabilityService: AvailabilityService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Auto-fill resourceName if we are a freelancer
    this.notificationService.currentUserId$.subscribe(id => {
      if (this.notificationService.getCurrentRole() === 'FREELANCER' && !this.isEditMode) {
        this.availability.resourceName = id;
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.editId = +id;
      this.loading = true;
      this.availabilityService.getById(this.editId).subscribe({
        next: (data) => {
          // Strict security: Only the owning freelancer can edit!
          if (data.resourceName !== this.notificationService.getCurrentUser() && this.notificationService.getCurrentRole() === 'FREELANCER') {
             alert('You are not authorized to edit or delete someone else\'s availability!');
             this.router.navigate(['/availabilities']);
             return;
          }
          this.availability = data;
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load availability';
          this.loading = false;
        }
      });
    }
  }

  onSubmit(): void {
    this.saving = true;
    this.error = '';
    this.success = '';

    const request = this.isEditMode
      ? this.availabilityService.update(this.editId!, this.availability)
      : this.availabilityService.create(this.availability);

    request.subscribe({
      next: () => {
        this.success = this.isEditMode ? 'Availability updated!' : 'Availability created!';
        this.saving = false;
        setTimeout(() => this.router.navigate(['/availabilities']), 1200);
      },
      error: (err) => {
        this.error = (err?.error?.message || err?.message || 'Operation failed.') + " | Details: " + JSON.stringify(err);
        this.saving = false;
      }
    });
  }
}
