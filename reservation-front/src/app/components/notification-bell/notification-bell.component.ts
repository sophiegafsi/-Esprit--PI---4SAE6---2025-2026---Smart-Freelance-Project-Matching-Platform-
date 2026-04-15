import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { Notification } from '../../models/notification.model';
import { Subscription, interval } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount = 0;
  dropdownOpen = false;
  private pollingSub?: Subscription;
  private userSub?: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.userSub = this.notificationService.currentUserId$.subscribe(userId => {
      this.loadNotifications(userId);
      this.startPolling(userId);
    });
  }

  ngOnDestroy() {
    this.pollingSub?.unsubscribe();
    this.userSub?.unsubscribe();
  }

  startPolling(userId: string) {
    this.pollingSub?.unsubscribe();
    // Poll every 10 seconds for new notifications
    this.pollingSub = interval(10000).pipe(
      startWith(0),
      switchMap(() => this.notificationService.getUnreadCount(userId))
    ).subscribe(count => {
      this.unreadCount = count;
      // If dropdown is open, also refresh notifications list
      if (this.dropdownOpen) {
        this.loadNotifications(userId);
      }
    });
  }

  loadNotifications(userId: string) {
    this.notificationService.getNotifications(userId).subscribe(data => {
      this.notifications = data;
      this.unreadCount = data.filter(n => !n.isRead).length;
    });
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
    if (this.dropdownOpen) {
      this.loadNotifications(this.notificationService.getCurrentUser());
    }
  }

  markAsRead(notification: Notification, event: Event) {
    event.stopPropagation();
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe(() => {
        notification.isRead = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      });
    }
  }

  formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleString();
  }
}
