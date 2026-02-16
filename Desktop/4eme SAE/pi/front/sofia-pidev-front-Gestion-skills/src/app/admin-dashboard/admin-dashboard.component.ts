import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
    users: any[] = [];
    loading = true;
    error = '';

    constructor(private authService: AuthService, private router: Router) { }

    ngOnInit(): void {
        // Check if user is admin
        if (!this.authService.isAdmin()) {
            this.router.navigate(['/']);
            return;
        }

        // Fetch all users
        this.authService.getAllUsers().subscribe({
            next: (users) => {
                this.users = users;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load users';
                this.loading = false;
                console.error('Error fetching users:', err);
            }
        });
    }
}
