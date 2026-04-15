import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { NotificationBellComponent } from './components/notification-bell/notification-bell.component';
import { NotificationService } from './services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule, HttpClientModule, NotificationBellComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'reservation-front';
  simulatedUser = 'DaliClient'; // Default simulator
  currentUserRole = 'CLIENT';
  menuOpen = false;

  constructor(private notificationService: NotificationService) {
    this.notificationService.setCurrentUser(this.simulatedUser, 'CLIENT');
    this.notificationService.currentUserRole$.subscribe(role => this.currentUserRole = role);
  }

  onUserSimulateChange(event: any) {
    const selected = event.target.value;
    if (selected === 'DaliClient') {
      this.simulatedUser = 'DaliClient';
      this.notificationService.setCurrentUser(this.simulatedUser, 'CLIENT');
    } else {
      this.simulatedUser = selected;
      this.notificationService.setCurrentUser(this.simulatedUser, 'FREELANCER');
    }
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }
}
