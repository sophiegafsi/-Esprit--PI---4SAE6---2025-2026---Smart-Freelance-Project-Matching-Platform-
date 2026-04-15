import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ReponseService,
  ModerationResult,
  SentimentResult
} from '../services/reponse.service';
import { Reponse } from '../models/reclamation.model';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reponse-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reponse-form.html',
  styleUrls: ['./reponse-form.css']
})
export class ReponseFormComponent {
  @Input() reclamationId!: number;
  @Output() reponseAdded = new EventEmitter<void>();

  form: FormGroup;
  isSubmitting = false;

  moderationError = '';
  suggestedMessage = '';

  sentimentResult = '';
  sentimentReason = '';

  constructor(
    private fb: FormBuilder,
    private reponseService: ReponseService,
    private authService: AuthService
  ) {
    const isAdmin = this.authService.isAdmin();
    const currentUser = this.authService.getCurrentUserValue();
    const defaultUser = isAdmin ? 'Admin' : (currentUser?.firstName || 'User');

    this.form = this.fb.group({
      message: ['', [Validators.required, Validators.minLength(3)]],
      utilisateur: [defaultUser, Validators.required]
    });
  }

  analyzeMessageSentiment(): void {
    const message: string = this.form.value.message;

    if (!message || message.trim().length < 3) {
      this.sentimentResult = '';
      this.sentimentReason = '';
      return;
    }

    this.reponseService.analyzeSentiment(message).subscribe({
      next: (res: SentimentResult) => {
        this.sentimentResult = res.sentiment;
        this.sentimentReason = res.reason;
      },
      error: (err: HttpErrorResponse) => {
        console.error('Sentiment analysis error', err);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || !this.reclamationId) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.moderationError = '';
    this.suggestedMessage = '';

    const message: string = this.form.value.message;

    this.reponseService.moderateMessage(message).subscribe({
      next: (res: ModerationResult) => {
        if (!res.allowed) {
          this.isSubmitting = false;
          this.moderationError = res.reason;
          this.suggestedMessage = res.suggestion;
          return;
        }

        const payload: Reponse = {
          message: this.form.value.message,
          utilisateur: this.form.value.utilisateur
        };

        this.reponseService.addReponse(this.reclamationId, payload).subscribe({
          next: (_res: Reponse) => {
            const isAdmin = this.authService.isAdmin();
            const currentUser = this.authService.getCurrentUserValue();
            const defaultUser = isAdmin ? 'Admin' : (currentUser?.firstName || 'User');

            this.form.reset({
              message: '',
              utilisateur: defaultUser
            });

            this.isSubmitting = false;
            this.sentimentResult = '';
            this.sentimentReason = '';
            this.moderationError = '';
            this.suggestedMessage = '';
            this.reponseAdded.emit();
          },
          error: (err: HttpErrorResponse) => {
            console.error('Error adding response', err);
            this.isSubmitting = false;
          }
        });
      },
      error: (err: HttpErrorResponse) => {
        console.error('Moderation error', err);
        this.isSubmitting = false;
      }
    });
  }

  useSuggestion(): void {
    this.form.patchValue({
      message: this.suggestedMessage
    });

    this.moderationError = '';
    this.analyzeMessageSentiment();
  }

  get f() {
    return this.form.controls;
  }
}
