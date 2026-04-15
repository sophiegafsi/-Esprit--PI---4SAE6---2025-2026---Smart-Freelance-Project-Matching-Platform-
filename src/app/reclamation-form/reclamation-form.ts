import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ReclamationService, DuplicateCheckResponse } from '../services/reclamation.service';

@Component({
  selector: 'app-reclamation-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reclamation-form.html',
  styleUrls: ['./reclamation-form.css']
})
export class ReclamationFormComponent implements OnInit {
  form!: FormGroup;
  isEditMode = false;
  reclamationId!: number;

  priorites = [
    { value: 'BASSE', label: 'Low' },
    { value: 'MOYENNE', label: 'Medium' },
    { value: 'HAUTE', label: 'High' },
    { value: 'CRITIQUE', label: 'Critical' }
  ];

  statuts = [
    { value: 'EN_ATTENTE', label: 'Pending' },
    { value: 'EN_COURS', label: 'In Progress' },
    { value: 'RESOLUE', label: 'Resolved' },
    { value: 'REJETEE', label: 'Rejected' }
  ];

  types = [
    { value: 'PROJET', label: 'Project' },
    { value: 'PAIEMENT', label: 'Payment' },
    { value: 'UTILISATEUR', label: 'User' },
    { value: 'TECHNIQUE', label: 'Technical' }
  ];

  possibleDuplicates: DuplicateCheckResponse[] = [];
  checkingDuplicates = false;
  showDuplicateWarning = false;

  constructor(
    private fb: FormBuilder,
    private reclamationService: ReclamationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      sujet: ['', [Validators.required, Validators.minLength(4)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      priorite: ['', Validators.required],
      statut: ['EN_ATTENTE', Validators.required],
      type: ['', Validators.required]
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.reclamationId = +id;
      this.loadReclamation(this.reclamationId);
    }
  }

  loadReclamation(id: number): void {
    this.reclamationService.getById(id).subscribe({
      next: (data) => {
        this.form.patchValue({
          sujet: data.sujet,
          description: data.description,
          priorite: data.priorite,
          statut: data.statut,
          type: data.type
        });
      },
      error: (err) => {
        console.error('Error loading complaint', err);
      }
    });
  }

  checkDuplicatesBeforeSubmit(): void {
    const sujet = this.form.value.sujet?.trim();
    const description = this.form.value.description?.trim();

    if (!sujet || !description) {
      this.form.get('sujet')?.markAsTouched();
      this.form.get('description')?.markAsTouched();
      return;
    }

    this.checkingDuplicates = true;
    this.showDuplicateWarning = false;
    this.possibleDuplicates = [];

    this.reclamationService.checkDuplicates({ sujet, description }).subscribe({
      next: (data) => {
        this.checkingDuplicates = false;
        this.possibleDuplicates = data || [];
        this.showDuplicateWarning = this.possibleDuplicates.length > 0;
      },
      error: (err) => {
        console.error('Duplicate detection error', err);
        this.checkingDuplicates = false;
      }
    });
  }

  clearDuplicates(): void {
    this.possibleDuplicates = [];
    this.showDuplicateWarning = false;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.isEditMode) {
      this.reclamationService.update(this.reclamationId, this.form.value).subscribe({
        next: () => this.router.navigate(['/reclamations']),
        error: (err) => console.error('Update error', err)
      });
    } else {
      this.reclamationService.create(this.form.value).subscribe({
        next: () => this.router.navigate(['/reclamations']),
        error: (err) => console.error('Creation error', err)
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/reclamations']);
  }

  getSimilarityLabel(score: number): string {
    if (score >= 0.8) return 'Very high similarity';
    if (score >= 0.6) return 'High similarity';
    if (score >= 0.4) return 'Possible duplicate';
    return 'Low similarity';
  }

  get f() {
    return this.form.controls;
  }
}
