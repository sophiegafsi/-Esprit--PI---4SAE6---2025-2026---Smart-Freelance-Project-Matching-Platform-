import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Availability } from '../../models/availability.model';
import { AvailabilityService } from '../../services/availability.service';

@Component({
    selector: 'app-my-availabilities',
    standalone: false,
    templateUrl: './my-availabilities.component.html',
    styleUrls: ['./my-availabilities.component.css']
})
export class MyAvailabilitiesComponent implements OnInit {
    availabilities: Availability[] = [];
    loading = false;
    error = '';
    deleteError = '';

    constructor(
        private availabilityService: AvailabilityService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading = true;
        this.error = '';
        this.availabilityService.getMyAvailabilities().subscribe({
            next: (data) => {
                this.availabilities = data;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load your availabilities.';
                this.loading = false;
                console.error(err);
            }
        });
    }

    edit(id: number | undefined): void {
        if (id) this.router.navigate(['/availability', id, 'edit']);
    }

    delete(id: number | undefined): void {
        if (!id) return;
        if (!confirm('Are you sure you want to remove this availability slot?')) return;
        this.deleteError = '';
        this.availabilityService.delete(id).subscribe({
            next: () => this.load(),
            error: () => this.deleteError = 'Failed to delete. Please try again.'
        });
    }

    addNew(): void {
        this.router.navigate(['/availability/new']);
    }
}
