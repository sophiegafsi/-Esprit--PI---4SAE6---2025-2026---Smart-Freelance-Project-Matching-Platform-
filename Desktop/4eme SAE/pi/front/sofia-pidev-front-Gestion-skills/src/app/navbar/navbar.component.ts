import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
    isLoggedIn = false;
    currentUser: any = null;
    isFreelancer = false;
    isClient = false;
    isAdmin = false;

    constructor(private authService: AuthService, private router: Router) { }

    ngOnInit(): void {
        this.authService.authState$.subscribe(state => {
            this.isLoggedIn = state;
        });

        this.authService.currentUser$.subscribe(user => {
            console.log('Navbar received user:', user);
            this.currentUser = user;
            this.isFreelancer = this.authService.isFreelancer();
            this.isClient = this.authService.isClient();
            this.isAdmin = this.authService.isAdmin();
            console.log('Navbar isAdmin:', this.isAdmin);
        });

        // Load user data if already logged in
        if (this.authService.isLoggedIn()) {
            this.authService.loadCurrentUser();
        }
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
