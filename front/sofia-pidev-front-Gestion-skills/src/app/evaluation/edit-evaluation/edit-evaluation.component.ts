import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EvaluationService } from '../services/evaluation.service';
import { RewardService } from '../../badge/services/reward.service';
import { Evaluation } from '../models/evaluation.model';
import { confirmDialog } from '../../shared/dialog.util';

@Component({
  selector: 'app-edit-evaluation',
  templateUrl: './edit-evaluation.component.html',
  styleUrls: ['./edit-evaluation.component.css']
})
export class EditEvaluationComponent implements OnInit {
  isLoading = false;
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';
  rewardMessage = '';
  selectedId = 0;

  criteria = {
    quality: 5,
    communication: 4,
    deadline: 5,
    expertise: 4
  };

  model: Evaluation = this.emptyModel();
  forceRewardSync = false;

  constructor(
    private readonly evaluationService: EvaluationService,
    private readonly rewardService: RewardService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) { }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id') || 0);
    if (id) {
      this.selectedId = id;
      this.load(id);
    } else {
      this.router.navigate(['/evaluations']);
    }
  }

  load(id: number): void {
    this.isLoading = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.evaluationService.getById(id).subscribe({
      next: (found) => {
        if (found) {
          this.model = {
            ...found,
            score: Number(found.score || 0)
          };
          // Initialize criteria based on current score (best effort distribution)
          const baseScore = Math.max(1, Math.min(5, this.model.score));
          this.criteria = {
            quality: baseScore,
            communication: baseScore,
            deadline: baseScore,
            expertise: baseScore
          };
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load the requested evaluation.';
      }
    });
  }

  get computedScore(): number {
    const values = Object.values(this.criteria).map((value) => Number(value || 0));
    const average = values.reduce((sum, value) => sum + value, 0) / values.length;
    return Number(average.toFixed(2));
  }

  save(): void {
    if (!this.selectedId || this.isSaving) return;

    this.isSaving = true;
    this.feedbackType = '';
    this.feedbackMessage = '';
    this.rewardMessage = '';

    const payload: Evaluation = {
      ...this.model,
      score: Math.round(this.computedScore),
      projectName: (this.model.projectName || '').trim(),
      evaluatorName: (this.model.evaluatorName || '').trim(),
      userEmail: (this.model.userEmail || '').trim(),
      evaluatedUserName: (this.model.evaluatedUserName || '').trim(),
      evaluatedUserEmail: (this.model.evaluatedUserEmail || '').trim(),
      comment: (this.model.comment || '').trim()
    };

    this.evaluationService.update(this.selectedId, payload).subscribe({
      next: (updated) => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Evaluation #${this.selectedId} updated successfully.`;

        if (!this.forceRewardSync) {
          this.rewardMessage = 'Badge and reward assignment is automatically managed by the backend.';
          this.isSaving = false;
          return;
        }

        this.rewardService.processEvaluation({
          evaluationId: this.selectedId,
          freelancerEmail: payload.evaluatedUserEmail,
          freelancerName: payload.evaluatedUserName,
          projectName: payload.projectName,
          currentScore: payload.score,
          averageScore: this.computedScore,
          totalPoints: payload.score * 100,
          totalEvaluations: 1,
          positiveEvaluations: payload.score >= 3 ? 1 : 0,
          completedProjects: 1,
          evaluatedAt: new Date().toISOString()
        }).subscribe({
          next: (response) => {
            this.rewardMessage = `Reward sync executed: ${String(response?.['message'] || 'OK')}`;
            this.isSaving = false;
          },
          error: (error) => {
            console.error(error);
            this.feedbackType = 'error';
            this.feedbackMessage = 'Evaluation updated, but reward sync failed.';
            this.isSaving = false;
          }
        });
      },
      error: (error) => {
        console.error(error);
        this.isSaving = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Update failed.';
      }
    });
  }

  async deleteSelected(): Promise<void> {
    if (!this.selectedId) return;

    const confirmed = await confirmDialog('Are you sure you want to delete this evaluation?', 'Confirm Deletion');
    if (!confirmed) return;

    this.evaluationService.deleteById(this.selectedId).subscribe({
      next: () => {
        this.router.navigate(['/evaluations'], {
          queryParams: { deleted: this.selectedId }
        });
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Delete action failed.';
      }
    });
  }

  private emptyModel(): Evaluation {
    return {
      score: 0,
      projectName: '',
      evaluatorName: '',
      userEmail: '',
      evaluatedUserName: '',
      evaluatedUserEmail: '',
      typeEvaluation: 'TECHNIQUE',
      comment: '',
      anonymous: false
    };
  }
}
