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

    constructor(private authService: AuthService, private router: Router) { }

    onSignup() {
        if (this.user.password !== this.user.confirmPassword) {
            alert('Passwords do not match');
            return;
        }

        this.authService.signup(this.user).subscribe({
            next: (res) => {
                this.router.navigate(['/verify-email']);
            },
            error: (err) => {
                console.error(err);
                alert('Signup failed. See console for details.');
            }
        });
    }
}
