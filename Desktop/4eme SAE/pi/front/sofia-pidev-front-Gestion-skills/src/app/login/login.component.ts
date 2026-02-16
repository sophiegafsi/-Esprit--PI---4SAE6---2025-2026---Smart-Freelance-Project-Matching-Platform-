import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    email = '';
    password = '';
    errorMessage = '';

    constructor(private authService: AuthService, private router: Router) { }

    onLogin() {
        if (!this.email || !this.password) {
            this.errorMessage = 'Please enter both email and password';
            return;
        }

        this.authService.login(this.email, this.password).subscribe({
            next: (response) => {
                this.authService.saveToken(response.access_token);
                // Navigate to home or dashboard
                this.router.navigate(['/']);
            },
            error: (err) => {
                console.error('Login failed', err);
                this.errorMessage = 'Invalid credentials or login failed';
            }
        });
    }
}
