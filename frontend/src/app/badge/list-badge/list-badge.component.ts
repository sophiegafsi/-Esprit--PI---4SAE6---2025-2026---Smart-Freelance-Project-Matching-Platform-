import { Component, OnInit } from '@angular/core';
import { BadgeService } from '../services/badge.service';
import { RewardService } from '../services/reward.service';
import { Badge } from '../models/badge.model';
import { RewardHistoryItem } from '../models/reward-history.model';
import { confirmDialog } from '../../shared/dialog.util';

@Component({
  selector: 'app-list-badge',
  templateUrl: './list-badge.component.html',
  styleUrls: ['./list-badge.component.css']
})
export class ListBadgeComponent implements OnInit {
  badges: Badge[] = [];
  history: RewardHistoryItem[] = [];
  searchTerm = '';
  onlyActive = false;
  isLoading = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';

  constructor(
    private readonly badgeService: BadgeService,
    private readonly rewardService: RewardService
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.isLoading = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.badgeService.list().subscribe({
      next: (badges) => {
        this.badges = badges || [];
        this.loadHistory();
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load badges.';
      }
    });
  }

  private loadHistory(): void {
    this.rewardService.getHistory().subscribe({
      next: (history) => {
        this.history = history || [];
        this.isLoading = false;
      },
      error: (error) => {
        console.error(error);
        this.history = [];
        this.isLoading = false;
      }
    });
  }

  get filteredBadges(): Badge[] {
    const query = this.searchTerm.trim().toLowerCase();
    return this.badges.filter((badge) => {
      const activeOk = this.onlyActive ? badge.isActive === true : true;
      if (!query) return activeOk;
      const text = `${badge.name || ''} ${badge.description || ''} ${badge.conditionType || ''}`.toLowerCase();
      return activeOk && text.includes(query);
    });
  }

  get autoAssignableCount(): number {
    return this.badges.filter((badge) => Boolean(badge.autoAssignable)).length;
  }

  get certificateCount(): number {
    return this.history.filter((item) => Boolean(item.certificateGenerated)).length;
  }

  toggleActive(badge: Badge): void {
    if (!badge.id) return;

    const payload: Badge = {
      ...badge,
      isActive: !badge.isActive
    };

    this.badgeService.update(badge.id, payload).subscribe({
      next: () => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Badge ${payload.isActive ? 'enabled' : 'disabled'}.`;
        this.refresh();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to update status.';
      }
    });
  }

  async deleteBadge(badge: Badge): Promise<void> {
    if (!badge.id) return;
    const confirmed = await confirmDialog(`Delete badge "${badge.name}"?`, 'Confirm deletion');
    if (!confirmed) return;

    this.badgeService.deleteById(badge.id).subscribe({
      next: () => {
        this.feedbackType = 'success';
        this.feedbackMessage = 'Badge deleted.';
        this.refresh();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Delete action failed.';
      }
    });
  }

  assignEligibleRewards(): void {
    this.rewardService.assignPendingRewards().subscribe({
      next: (response) => {
        const count = Number(response?.assignedRewards ?? 0);
        this.feedbackType = 'success';
        this.feedbackMessage = `${count} eligible reward(s) assigned.`;
        this.refresh();
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to assign rewards.';
      }
    });
  }

  recalculateLevels(): void {
    this.rewardService.recalculateLevels().subscribe({
      next: (response) => {
        this.feedbackType = 'success';
        this.feedbackMessage = response?.message || 'Levels recalculated.';
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to recalculate levels.';
      }
    });
  }
}
