import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { NotificationService } from '../services/notification.service';
import { Subscription, interval } from 'rxjs';

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

    // Notifications
    notifications: any[] = [];
    unreadCount = 0;
    showNotifications = false;
    private pollSub?: Subscription;

    constructor(
        private authService: AuthService,
        private router: Router,
        private notificationService: NotificationService
    ) { }

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

            if (user && user.id) {
                this.startPolling(user.id);
            } else {
                this.pollSub?.unsubscribe();
            }
        });

        // Load user data if already logged in
        if (this.authService.isLoggedIn()) {
            this.authService.loadCurrentUser();
        }
    }

    logout() {
        this.pollSub?.unsubscribe();
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    startPolling(userId: string) {
        this.pollSub?.unsubscribe();
        this.fetchNotifications(userId);
        this.pollSub = interval(3000).subscribe(() => this.fetchNotifications(userId));
    }

    fetchNotifications(userId: string) {
        this.notificationService.getNotifications(userId).subscribe({
            next: (data) => {
                this.notifications = data;
                this.unreadCount = data.filter((n: any) => !n.read).length;
            },
            error: (err) => console.error("Failed to fetch notifications:", err)
        });
    }

    toggleNotifications() {
        this.showNotifications = !this.showNotifications;
    }

    closeNotifications() {
        this.showNotifications = false;
    }

    markAsRead(n: any) {
        if (!n.read) {
            this.notificationService.markAsRead(n.id).subscribe(() => {
                n.read = true;
                this.unreadCount = Math.max(0, this.unreadCount - 1);
            });
        }

        if (n.actionUrl && n.actionUrl.trim() !== '') {
            this.router.navigateByUrl(n.actionUrl);
            this.closeNotifications();
        }
    }
}
