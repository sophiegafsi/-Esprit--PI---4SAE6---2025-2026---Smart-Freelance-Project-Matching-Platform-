import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
    email = '';

    constructor(private authService: AuthService, private router: Router) { }

    onSubmit() {
        this.authService.forgotPassword(this.email).subscribe({
            next: () => {
                this.router.navigate(['/verify-email']);
            },
            error: (err) => {
                console.error('Forgot password error', err);
                // Even on error (to avoid enumeration attacks), usually good to navigate or show geneirc message
                // But for now, let's navigate
                this.router.navigate(['/verify-email']);
            }
        });
    }
}
