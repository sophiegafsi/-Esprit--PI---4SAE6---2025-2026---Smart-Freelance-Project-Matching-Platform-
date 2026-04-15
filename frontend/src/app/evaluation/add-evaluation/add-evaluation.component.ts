import { Component } from '@angular/core';
import { EvaluationService } from '../services/evaluation.service';
import { RewardService } from '../../badge/services/reward.service';
import { Evaluation } from '../models/evaluation.model';

@Component({
  selector: 'app-add-evaluation',
  templateUrl: './add-evaluation.component.html',
  styleUrls: ['./add-evaluation.component.css']
})
export class AddEvaluationComponent {
  isSaving = false;
  feedbackType: 'success' | 'error' | '' = '';
  feedbackMessage = '';
  rewardMessage = '';

  criteria = {
    quality: 5,
    communication: 4,
    deadline: 5,
    expertise: 4
  };

  formModel: Evaluation = {
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

  forceRewardSync = false;

  constructor(
    private readonly evaluationService: EvaluationService,
    private readonly rewardService: RewardService
  ) {}

  get computedScore(): number {
    const values = Object.values(this.criteria).map((value) => Number(value || 0));
    const average = values.reduce((sum, value) => sum + value, 0) / values.length;
    return Number(average.toFixed(2));
  }

  submit(): void {
    if (this.isSaving) return;

    if (!this.formModel.projectName?.trim() || !this.formModel.evaluatedUserEmail?.trim()) {
      this.feedbackType = 'error';
      this.feedbackMessage = 'Project and freelancer email are required.';
      return;
    }

    this.isSaving = true;
    this.feedbackType = '';
    this.feedbackMessage = '';
    this.rewardMessage = '';

    const payload: Evaluation = {
      ...this.formModel,
      score: Math.round(this.computedScore),
      projectName: this.formModel.projectName.trim(),
      evaluatorName: (this.formModel.evaluatorName || '').trim(),
      userEmail: (this.formModel.userEmail || '').trim(),
      evaluatedUserName: (this.formModel.evaluatedUserName || '').trim(),
      evaluatedUserEmail: this.formModel.evaluatedUserEmail.trim(),
      comment: (this.formModel.comment || '').trim()
    };

    this.evaluationService.create(payload).subscribe({
      next: (saved) => {
        this.feedbackType = 'success';
        this.feedbackMessage = `Evaluation #${saved?.id ?? ''} created successfully.`;

        if (!this.forceRewardSync) {
          this.rewardMessage = 'Badge and reward assignment is automatically triggered by the backend.';
          this.isSaving = false;
          this.reset();
          return;
        }

        this.rewardService.processEvaluation({
          evaluationId: saved?.id ?? null,
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
            this.rewardMessage = `Reward sync executed: ${String(response?.message || 'OK')}`;
            this.isSaving = false;
            this.reset();
          },
          error: (error) => {
            console.error(error);
            this.feedbackType = 'error';
            this.feedbackMessage = 'Evaluation saved, but reward sync failed.';
            this.isSaving = false;
          }
        });
      },
      error: (error) => {
        console.error(error);
        this.feedbackType = 'error';
        this.feedbackMessage = 'Unable to create evaluation.';
        this.isSaving = false;
      }
    });
  }

  reset(): void {
    this.formModel = {
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
    this.criteria = {
      quality: 5,
      communication: 4,
      deadline: 5,
      expertise: 4
    };
    this.forceRewardSync = false;
  }
}
