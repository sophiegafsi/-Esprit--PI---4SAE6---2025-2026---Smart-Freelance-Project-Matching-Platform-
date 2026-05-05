import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-signup',
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.css']
})
export class SignupComponent {
    user = {
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        birthDate: '',
        country: 'Tunisia'
    };

    errors: any = {};

    constructor(private authService: AuthService, private router: Router) { }

    onSignup() {
        if (!this.validateForm()) {
            return;
        }

        this.authService.signup(this.user).subscribe({
            next: (res) => {
                this.router.navigate(['/verify-email']);
            },
            error: (err) => {
                console.error(err);
                if (err.status === 409) {
                    this.errors.email = 'This email is already registered.';
                } else {
                    alert('Signup failed: ' + (err.error || 'Please try again.'));
                }
            }
        });
    }

    private validateForm(): boolean {
        this.errors = {};
        let isValid = true;

        if (!this.user.firstName) { this.errors.firstName = 'First name is required'; isValid = false; }
        if (!this.user.lastName) { this.errors.lastName = 'Last name is required'; isValid = false; }

        if (!this.user.email) {
            this.errors.email = 'Email is required';
            isValid = false;
        } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(this.user.email)) {
            this.errors.email = 'Invalid email format';
            isValid = false;
        }

        if (!this.user.password) {
            this.errors.password = 'Password is required';
            isValid = false;
        } else {
            const pass = this.user.password;
            if (pass.length < 8) {
                this.errors.password = 'Minimum 8 characters required';
                isValid = false;
            } else if (!/(?=.*[0-9])(?=.*[.!@#$%^&*?,;/])/.test(pass)) {
                this.errors.password = 'Must include at least one number and one symbol (.,?;/!@#$%)';
                isValid = false;
            }
        }

        if (this.user.password !== this.user.confirmPassword) {
            this.errors.confirmPassword = 'Passwords do not match';
            isValid = false;
        }

        if (!this.user.birthDate) {
            this.errors.birthDate = 'Birth date is required';
            isValid = false;
        } else {
            const birth = new Date(this.user.birthDate);
            const today = new Date();
            let age = today.getFullYear() - birth.getFullYear();
            const m = today.getMonth() - birth.getMonth();
            if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
                age--;
            }
            if (age < 18) {
                this.errors.birthDate = 'You must be at least 18 years old';
                isValid = false;
            }
        }

        return isValid;
    }
}

