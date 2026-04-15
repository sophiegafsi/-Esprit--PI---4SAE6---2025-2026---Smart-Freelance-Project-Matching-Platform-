import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CandidatureService } from '../../services/candidature.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-apply',
  templateUrl: './apply.component.html',
  styleUrls: ['./apply.component.css']
})
export class ApplyComponent implements OnInit {
  projectId: string | null = null;
  freelancerId: string | null = null;
  coverLetter: string = '';
  selectedFile: File | undefined = undefined;
  errorMessage: string = '';
  successMessage: string = '';
  project: any = null;
  loading: boolean = false;
  checkingGrammar: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private candidatureService: CandidatureService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('projectId');

    if (this.projectId) {
      this.candidatureService.getAllProjects().subscribe(projects => {
        this.project = projects.find(p => p.id === this.projectId);
        if (this.project && this.project.status === 'CLOSED') {
          this.errorMessage = "Freelink: This project is no longer accepting applications.";
        }
      });
    }

    this.authService.getCurrentUser().subscribe(user => {
      if (user) {
        this.freelancerId = user.id;
        // Optional: Check if user is actually a freelancer
        if (!this.authService.isFreelancer()) {
          this.errorMessage = "Freelink: Only freelancers can apply to projects.";
        }
      } else {
        this.errorMessage = "Freelink: Please log in to your account to apply.";
      }
    });
  }

  onFileSelected(file: File | undefined): void {
    this.selectedFile = file;
  }

  checkGrammar(): void {
    if (!this.coverLetter.trim()) {
      this.errorMessage = 'Freelink AI: Please write your cover letter before auto-correcting.';
      return;
    }

    this.checkingGrammar = true;
    this.errorMessage = '';

    this.candidatureService.checkGrammar(this.coverLetter).subscribe({
      next: (correctedText) => {
        this.coverLetter = correctedText;
        this.checkingGrammar = false;
        this.successMessage = "Freelink Insight: Cover letter auto-corrected successfully!";
        this.cdr.detectChanges();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Freelink Server Error: Failed to auto-correct grammar. Our AI services might be busy.';
        this.checkingGrammar = false;
        this.cdr.detectChanges();
      }
    });
  }

  apply(): void {
    if (!this.freelancerId || !this.projectId) {
      this.errorMessage = 'Freelink check failed: Missing freelancer or project information.';
      return;
    }

    if (!this.coverLetter) {
      this.errorMessage = 'Freelink check failed: Please provide a cover letter.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.candidatureService.apply(this.freelancerId, this.projectId, this.coverLetter, this.selectedFile)
      .subscribe({
        next: (res) => {
          this.successMessage = 'Freelink Success: Application submitted successfully!';
          this.loading = false;
          setTimeout(() => this.router.navigate(['/my-applications']), 2000);
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Freelink Server Error: Failed to submit application. ' + (err.error?.message || err.message || '');
          this.loading = false;
        }
      });
  }
}
