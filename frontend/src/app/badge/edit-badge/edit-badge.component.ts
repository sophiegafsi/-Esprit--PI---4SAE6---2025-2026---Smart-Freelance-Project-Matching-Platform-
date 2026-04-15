import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BadgeService } from '../services/badge.service';
import { Badge } from '../models/badge.model';

@Component({
  selector: 'app-edit-badge',
  templateUrl: './edit-badge.component.html',
  styleUrls: ['./edit-badge.component.css']
})
export class EditBadgeComponent implements OnInit {
  badges: Badge[] = [];
  selectedBadgeId = 0;
  isLoading = false;
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';

  formModel: Badge = this.emptyBadge();

  constructor(
    private readonly badgeService: BadgeService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadBadges();
  }

  loadBadges(): void {
    this.isLoading = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.badgeService.list().subscribe({
      next: (badges) => {
        this.badges = badges || [];
        this.isLoading = false;

        const routeId = Number(this.route.snapshot.paramMap.get('id') || 0);
        const initialId = routeId || this.selectedBadgeId || Number(this.badges[0]?.id || 0);

        if (initialId) {
          this.selectBadge(initialId);
        } else {
          this.formModel = this.emptyBadge();
        }
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load badges.';
      }
    });
  }

  selectBadge(id: number): void {
    this.selectedBadgeId = Number(id || 0);
    const found = this.badges.find((badge) => Number(badge.id) === this.selectedBadgeId);
    this.formModel = found ? this.clone(found) : this.emptyBadge();
  }

  save(): void {
    if (!this.selectedBadgeId || this.isSaving) {
      return;
    }

    if (!this.formModel.name?.trim()) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Badge name is required.';
      return;
    }

    this.isSaving = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    const payload: Badge = {
      ...this.formModel,
      category: this.normalizeCategory(this.formModel.category),
      conditionType: this.formModel.autoAssignable ? this.normalizeCondition(this.formModel.conditionType) : '',
      conditionValue: this.formModel.autoAssignable ? Number(this.formModel.conditionValue || 0) : 0
    };

    this.badgeService.update(this.selectedBadgeId, payload).subscribe({
      next: (updated) => {
        this.isSaving = false;
        this.feedbackType = 'success';
        this.feedbackMessage = `Badge #${updated?.id ?? this.selectedBadgeId} updated successfully.`;
        this.loadBadges();
      },
      error: (error) => {
        console.error(error);
        this.isSaving = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to update badge.';
      }
    });
  }

  reset(): void {
    if (!this.selectedBadgeId) {
      this.formModel = this.emptyBadge();
      return;
    }

    this.selectBadge(this.selectedBadgeId);
  }

  private clone(badge: Badge): Badge {
    return {
      ...badge,
      conditionValue: Number(badge.conditionValue ?? 0),
      pointsReward: Number(badge.pointsReward ?? 0),
      autoAssignable: Boolean(badge.autoAssignable),
      certificateEligible: Boolean(badge.certificateEligible),
      isActive: badge.isActive !== false
    };
  }

  private emptyBadge(): Badge {
    return {
      name: '',
      description: '',
      icon: 'Trophy',
      category: 'SCORE',
      conditionType: 'AVERAGE_SCORE',
      conditionValue: 0,
      pointsReward: 0,
      autoAssignable: true,
      certificateEligible: false,
      isActive: true
    };
  }

  private normalizeCategory(category: string | undefined): string {
    const value = String(category || 'SCORE').trim().toUpperCase();
    if (value.includes('POINT')) return 'POINTS';
    if (value.includes('LEVEL')) return 'LEVEL';
    if (value.includes('CUSTOM')) return 'CUSTOM';
    return 'SCORE';
  }

  private normalizeCondition(condition: string | undefined): string {
    const value = String(condition || 'AVERAGE_SCORE').trim().toUpperCase();
    if (value.includes('POINT')) return 'POINTS';
    if (value.includes('LEVEL')) return 'LEVEL';
    return 'AVERAGE_SCORE';
  }
}
