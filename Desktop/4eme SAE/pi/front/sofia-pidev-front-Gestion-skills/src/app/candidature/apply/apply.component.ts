import { Component, OnInit } from '@angular/core';
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private candidatureService: CandidatureService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('projectId');

    if (this.projectId) {
      this.candidatureService.getAllProjects().subscribe(projects => {
        this.project = projects.find(p => p.id === this.projectId);
        if (this.project && this.project.status === 'CLOSED') {
          this.errorMessage = "This project is no longer accepting applications.";
        }
      });
    }

    this.authService.getCurrentUser().subscribe(user => {
      if (user) {
        this.freelancerId = user.id;
        // Optional: Check if user is actually a freelancer
        if (!this.authService.isFreelancer()) {
          this.errorMessage = "Only freelancers can apply.";
        }
      } else {
        this.errorMessage = "You must be logged in to apply.";
      }
    });
  }

  onFileSelected(file: File | undefined): void {
    this.selectedFile = file;
  }

  apply(): void {
    if (!this.freelancerId || !this.projectId) {
      this.errorMessage = 'Missing freelancer or project information.';
      return;
    }

    if (!this.coverLetter) {
      this.errorMessage = 'Please provide a cover letter.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.candidatureService.apply(this.freelancerId, this.projectId, this.coverLetter, this.selectedFile)
      .subscribe({
        next: (res) => {
          this.successMessage = 'Application submitted successfully!';
          this.loading = false;
          setTimeout(() => this.router.navigate(['/my-applications']), 2000);
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Failed to submit application. ' + (err.error?.message || err.message || '');
          this.loading = false;
        }
      });
  }
}
