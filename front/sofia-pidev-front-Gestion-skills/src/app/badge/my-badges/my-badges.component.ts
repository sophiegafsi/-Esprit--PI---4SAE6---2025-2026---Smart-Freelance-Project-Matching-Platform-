import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { UserBadgeDTO } from '../models/badge.model';
import { BadgeService } from '../services/badge.service';

@Component({
    selector: 'app-my-badges',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './my-badges.component.html',
    styleUrls: ['./my-badges.component.css']
})
export class MyBadgesComponent implements OnInit {
    @Input() userName: string = '';
    badges: UserBadgeDTO[] = [];
    isLoading = true;
    error: string | null = null;

    constructor(private readonly badgeService: BadgeService) { }

    ngOnInit(): void {
        if (this.userName) {
            this.loadBadges();
        }
    }

    loadBadges(): void {
        this.isLoading = true;
        this.badgeService.getUserBadges(this.userName).subscribe({
            next: (data) => {
                this.badges = data;
                this.isLoading = false;
            },
            error: (err) => {
                console.error('Error loading badges:', err);
                this.error = 'Failed to load badges.';
                this.isLoading = false;
            }
        });
    }
}
