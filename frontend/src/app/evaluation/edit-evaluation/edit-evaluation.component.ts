import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  evaluations: Evaluation[] = [];
  selectedId = 0;
  model: Evaluation = this.emptyModel();
  isLoading = false;
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';
  forceRewardSync = false;

  constructor(
    private readonly evaluationService: EvaluationService,
    private readonly rewardService: RewardService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    this.evaluationService.getAll().subscribe({
      next: (evaluations) => {
        this.evaluations = evaluations || [];
        this.isLoading = false;
        const routeId = Number(this.route.snapshot.paramMap.get('id') || 0);
        const first = Number(this.evaluations[0]?.id || 0);
        this.select(routeId || this.selectedId || first);
      },
      error: (error) => {
        console.error(error);
        this.isLoading = false;
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to load evaluations.';
      }
    });
  }

  select(id: number): void {
    this.selectedId = Number(id || 0);
    const found = this.evaluations.find((item) => Number(item.id) === this.selectedId);
    this.model = found
      ? {
          ...found,
          projectName: found.projectName || '',
          evaluatorName: found.evaluatorName || '',
          userEmail: found.userEmail || '',
          evaluatedUserName: found.evaluatedUserName || '',
          evaluatedUserEmail: found.evaluatedUserEmail || '',
          comment: found.comment || '',
          typeEvaluation: found.typeEvaluation || 'TECHNIQUE',
          score: Number(found.score || 0),
          anonymous: Boolean(found.anonymous)
        }
      : this.emptyModel();
  }

  save(): void {
    if (!this.selectedId || this.isSaving) return;

    this.isSaving = true;
    this.feedbackType = '';
    this.feedbackMessage = '';

    const payload: Evaluation = {
      ...this.model,
      score: Number(this.model.score || 0),
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
        this.feedbackMessage = `Evaluation #${updated?.id ?? this.selectedId} updated.`;

        if (!this.forceRewardSync) {
          this.isSaving = false;
          this.load();
          return;
        }

        this.rewardService.processEvaluation({
          evaluationId: updated?.id ?? this.selectedId,
          freelancerEmail: payload.evaluatedUserEmail,
          freelancerName: payload.evaluatedUserName,
          projectName: payload.projectName,
          currentScore: payload.score,
          averageScore: payload.score,
          totalPoints: payload.score * 100,
          totalEvaluations: 1,
          positiveEvaluations: payload.score >= 3 ? 1 : 0,
          completedProjects: 1,
          evaluatedAt: new Date().toISOString()
        }).subscribe({
          next: () => {
            this.isSaving = false;
            this.load();
          },
          error: (error) => {
            console.error(error);
            this.isSaving = false;
            this.feedbackType = 'error';
            this.feedbackMessage = 'Evaluation updated, but reward sync failed.';
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

    const confirmed = await confirmDialog('Delete this evaluation?', 'Confirm deletion');
    if (!confirmed) return;

    this.evaluationService.deleteById(this.selectedId).subscribe({
      next: () => {
        this.feedbackType = 'success';
        this.feedbackMessage = 'Evaluation deleted.';
        this.selectedId = 0;
        this.model = this.emptyModel();
        this.load();
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
