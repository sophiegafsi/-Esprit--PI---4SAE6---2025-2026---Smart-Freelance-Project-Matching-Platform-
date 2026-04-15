import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { BadgeService } from '../services/badge.service';
import { Badge } from '../models/badge.model';

@Component({
  selector: 'app-create-badge',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-badge.component.html',
  styleUrls: ['./create-badge.component.css']
})
export class CreateBadgeComponent {
  showIconPicker = false;
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';

  icons: string[] = [
    'Trophy', 'Medal', 'Ribbon', 'Star', 'Diamond', 'Crown', 'Target', 'Rocket',
    'Bolt', 'Fire', 'Strength', 'Palette', 'Book', 'Idea', 'Wrench', 'Robot',
    'Laptop', 'Gamepad', 'Chart', 'Microscope', 'Mask', 'Heart', 'Rainbow'
  ];

  badge: Badge = this.createDefaultBadge();

  constructor(private readonly badgeService: BadgeService) {}

  saveBadge(form: NgForm): void {
    if (this.isSaving) {
      return;
    }

    if (form.invalid) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Please complete required fields.';
      return;
    }

    if (this.badge.autoAssignable && (!this.badge.conditionType || (this.badge.conditionValue ?? -1) < 0)) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Automatic badges require a condition type and value.';
      return;
    }

    const payload: Badge = {
      ...this.badge,
      category: this.normalizeCategory(this.badge.category),
      conditionType: this.badge.autoAssignable ? this.normalizeConditionType(this.badge.conditionType) : '',
      conditionValue: this.badge.autoAssignable ? this.badge.conditionValue : 0
    };

    this.isSaving = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.badgeService.create(payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.feedbackType = 'success';
        this.feedbackMessage = 'Badge created successfully.';
        this.resetForm(form);
      },
      error: (error) => {
        this.isSaving = false;
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Error while creating badge.';
      }
    });
  }

  resetForm(form?: NgForm): void {
    this.badge = this.createDefaultBadge();
    this.showIconPicker = false;
    form?.resetForm(this.badge);
  }

  selectIcon(icon: string): void {
    this.badge.icon = icon;
    this.showIconPicker = false;
  }

  onAutoAssignableChange(): void {
    if (this.badge.autoAssignable) {
      if (!this.badge.conditionType) {
        this.badge.conditionType = this.badge.category === 'POINTS' ? 'POINTS' : 'AVERAGE_SCORE';
      }
      if ((this.badge.conditionValue ?? 0) < 0) {
        this.badge.conditionValue = 0;
      }
      if (this.badge.category === 'CUSTOM') {
        this.badge.category = 'SCORE';
      }
      return;
    }

    this.badge.category = 'CUSTOM';
    this.badge.conditionType = '';
    this.badge.conditionValue = 0;
  }

  onCategoryChange(): void {
    if (!this.badge.autoAssignable) {
      return;
    }

    const normalizedCategory = this.normalizeCategory(this.badge.category);
    this.badge.category = normalizedCategory;

    if (normalizedCategory === 'POINTS') {
      this.badge.conditionType = 'POINTS';
      return;
    }

    if (normalizedCategory === 'LEVEL') {
      this.badge.conditionType = 'LEVEL';
      return;
    }

    this.badge.conditionType = 'AVERAGE_SCORE';
  }

  private createDefaultBadge(): Badge {
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

  private normalizeConditionType(conditionType: string | undefined): string {
    if (!conditionType) {
      return 'AVERAGE_SCORE';
    }

    const normalized = conditionType.trim().toUpperCase().replace(/[\s-]+/g, '_');

    if (['AVERAGE_SCORE', 'SCORE', 'SCORE_MOYEN'].includes(normalized)) {
      return 'AVERAGE_SCORE';
    }

    if (['POINTS', 'POINTS_CUMULES', 'POINTS_CUMULATIFS'].includes(normalized)) {
      return 'POINTS';
    }

    if (normalized === 'LEVEL') {
      return 'LEVEL';
    }

    return normalized;
  }

  private normalizeCategory(category: string | undefined): string {
    if (!category) {
      return this.badge.autoAssignable ? 'SCORE' : 'CUSTOM';
    }

    const normalized = category.trim().toUpperCase().replace(/[\s-]+/g, '_');

    if (['SCORE', 'AVERAGE_SCORE', 'SCORE_MOYEN'].includes(normalized)) {
      return 'SCORE';
    }

    if (['POINTS', 'POINTS_CUMULES', 'POINTS_CUMULATIFS'].includes(normalized)) {
      return 'POINTS';
    }

    if (normalized === 'LEVEL') {
      return 'LEVEL';
    }

    return 'CUSTOM';
  }
}
