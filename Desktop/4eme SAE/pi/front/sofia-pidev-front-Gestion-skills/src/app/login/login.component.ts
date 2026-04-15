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
    emailError = '';
    passwordError = '';

    constructor(private authService: AuthService, private router: Router) { }

    onLogin() {
        this.resetErrors();

        if (!this.email) {
            this.emailError = 'Email is required';
        } else if (!this.isValidEmail(this.email)) {
            this.emailError = 'Please enter a valid email address';
        }

        if (!this.password) {
            this.passwordError = 'Password is required';
        } else if (this.password.length < 8) {
            this.passwordError = 'Minimum 8 characters required';
        } else if (!/(?=.*[0-9])(?=.*[.!@#$%^&*?,;/])/.test(this.password)) {
            this.passwordError = 'Must include a number and symbol (.,?;/!#$%)';
        }

        if (this.emailError || this.passwordError) {
            return;
        }

        this.authService.login(this.email, this.password).subscribe({
            next: (response) => {
                this.authService.saveToken(response.access_token);
                this.router.navigate(['/']);
            },
            error: (err) => {
                console.error('Login failed', err);
                this.errorMessage = 'Freelink Web App: Invalid credentials or login failed.';
            }
        });
    }

    private isValidEmail(email: string): boolean {
        const re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        return re.test(email);
    }

    private resetErrors() {
        this.emailError = '';
        this.passwordError = '';
        this.errorMessage = '';
    }
}
