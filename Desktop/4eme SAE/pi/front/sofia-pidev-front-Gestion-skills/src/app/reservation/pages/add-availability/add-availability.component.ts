import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Availability } from '../../models/availability.model';
import { AvailabilityService } from '../../services/availability.service';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-add-availability',
    standalone: false,
    templateUrl: './add-availability.component.html',
    styleUrls: ['./add-availability.component.css']
})
export class AddAvailabilityComponent implements OnInit {
    availability: Availability = {
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
    today = new Date().toISOString().split('T')[0];

    constructor(
        private availabilityService: AvailabilityService,
        private authService: AuthService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.isEditMode = true;
            this.editId = +id;
            this.loading = true;
            this.availabilityService.getById(this.editId).subscribe({
                next: (data) => {
                    this.availability = data;
                    this.loading = false;
                },
                error: () => {
                    this.error = 'Failed to load availability for editing.';
                    this.loading = false;
                }
            });
        }
    }

    onSubmit(): void {
        if (!this.availability.date || !this.availability.startTime || !this.availability.endTime || !this.availability.description || !this.availability.location) {
            this.error = 'Please fill in all required fields.';
            return;
        }
        if (this.availability.maxSlots < 1) {
            this.error = 'Max slots must be at least 1.';
            return;
        }

        this.saving = true;
        this.error = '';
        this.success = '';

        const request = this.isEditMode
            ? this.availabilityService.update(this.editId!, this.availability)
            : this.availabilityService.create(this.availability);

        request.subscribe({
            next: () => {
                this.success = this.isEditMode ? 'Availability updated successfully!' : 'Availability published successfully!';
                this.saving = false;
                setTimeout(() => this.router.navigate(['/my-availabilities']), 1500);
            },
            error: (err) => {
                this.error = err?.error?.message || err?.message || 'Operation failed. Please check the data and try again.';
                this.saving = false;
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/my-availabilities']);
    }
}
