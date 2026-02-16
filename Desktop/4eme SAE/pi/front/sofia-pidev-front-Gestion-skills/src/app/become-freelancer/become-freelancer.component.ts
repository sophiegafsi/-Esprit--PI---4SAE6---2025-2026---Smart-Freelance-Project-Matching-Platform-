import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-become-freelancer',
    templateUrl: './become-freelancer.component.html',
    styleUrls: ['./become-freelancer.component.css']
})
export class BecomeFreelancerComponent {
    profile = {
        jobTitle: '',
        bio: '',
        skills: '',
        hourlyRate: 0,
        portfolioUrl: ''
    };

    constructor(private authService: AuthService, private router: Router) { }

    onSubmit() {
        this.authService.becomeFreelancer(this.profile).subscribe({
            next: (res) => {
                alert('Congratulations! You are now a freelancer. Your profile has been updated.');
                this.router.navigate(['/']);
                // Reload to update navbar and user state
                setTimeout(() => window.location.reload(), 500);
            },
            error: (err) => {
                console.error(err);
                alert('Failed to update profile.');
            }
        });
    }
}
