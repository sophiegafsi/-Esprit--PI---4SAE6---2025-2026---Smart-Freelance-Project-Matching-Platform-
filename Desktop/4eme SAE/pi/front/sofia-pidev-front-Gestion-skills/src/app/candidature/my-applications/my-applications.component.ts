import { Component, OnInit } from '@angular/core';
import { CandidatureService } from '../../services/candidature.service';
import { AuthService } from '../../services/auth.service';
import { Candidature } from '../../models/candidature.model';
import { TimeTrackingService } from '../../services/time-tracking.service';

@Component({
  selector: 'app-my-applications',
  templateUrl: './my-applications.component.html',
  styleUrls: ['./my-applications.component.css']
})
export class MyApplicationsComponent implements OnInit {
  applications: (Candidature & {
    editing?: boolean;
    editDraft?: string;
    canFreelancerSign?: boolean;
  })[] = [];
  loading: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  // Signature Modal State
  showSignatureModal: boolean = false;
  selectedContractId: string | null = null;
  isSubmittingSignature: boolean = false;

  public currentUserId: string = '';

  constructor(
    private candidatureService: CandidatureService,
    private authService: AuthService,
    private timeTrackingService: TimeTrackingService
  ) { }

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(user => {
      if (user) {
        this.currentUserId = user.id;
        this.loadApplications(user.id);
      } else {
        this.errorMessage = 'Freelink: You must be securely logged into your account to view your applications.';
        this.loading = false;
      }
    });
  }

  loadApplications(userId: string): void {
    this.candidatureService.getMyApplications(userId).subscribe({
      next: (data) => {
        this.candidatureService.getAllContracts().subscribe(contracts => {
          this.applications = data.map(app => {
            const contract = contracts.find(c => c.candidatureId === app.id);
            return {
              ...app,
              editing: false,
              editDraft: '',
              expanded: false,
              contract: contract,
              canFreelancerSign: contract?.status === 'ONESIDED'
            };
          });
          this.loading = false;

          this.applications.forEach(app => {
            this.candidatureService.getProjectById(app.projectId).subscribe({
              next: (project) => {
                app.projectTitle = project.title;
                if (project.clientId) {
                  this.authService.getUserById(project.clientId).subscribe({
                    next: (user) => app.clientName = user.firstName + ' ' + user.lastName,
                    error: () => app.clientName = 'Unknown Client'
                  });
                }
              },
              error: () => app.projectTitle = 'Unknown Project'
            });
          });
        });
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Freelink Server Error: Failed to load your applications.';
        this.loading = false;
      }
    });
  }

  startEdit(app: any): void {
    app.editing = true;
    app.editDraft = app.coverLetter;
  }

  cancelEdit(app: any): void {
    app.editing = false;
    app.editDraft = '';
  }

  saveEdit(app: any): void {
    if (!app.editDraft || !app.editDraft.trim()) {
      alert('Freelink Rules: Cover letter cannot be empty.');
      return;
    }
    this.candidatureService.updateApplication(app.id, this.currentUserId, app.editDraft.trim()).subscribe({
      next: (updated) => {
        app.coverLetter = updated.coverLetter;
        app.editing = false;
      },
      error: (err) => {
        console.error(err);
        alert('Freelink Server Error: Failed to update application.');
      }
    });
  }

  deleteApplication(app: any): void {
    if (!confirm(`Freelink: Are you sure you want to withdraw this application?`)) return;
    this.candidatureService.deleteApplication(app.id, this.currentUserId).subscribe({
      next: () => {
        this.applications = this.applications.filter(a => a.id !== app.id);
      },
      error: (err) => console.error(err)
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACCEPTED': return '#27ae60';
      case 'REJECTED': return '#e74c3c';
      default: return '#f39c12';
    }
  }

  downloadFile(app: any, event: Event): void {
    event.preventDefault();
    if (!app.data || !app.fileName) return;
    const byteCharacters = atob(app.data);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: app.fileType });
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = app.fileName;
    link.click();
  }

  downloadContract(appId: string): void {
    const app = this.applications.find(a => a.id === appId);
    if (app?.contract) {
      this.candidatureService.downloadContractPdf(app.contract.id).subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `contract-${appId.substring(0, 8)}.pdf`;
        a.click();
      });
    }
  }

  openSignatureModal(contractId: string): void {
    this.selectedContractId = contractId;
    this.showSignatureModal = true;
  }

  closeSignatureModal(): void {
    this.showSignatureModal = false;
    this.selectedContractId = null;
  }

  submitFreelancerSignature(signatureData: string): void {
    if (!this.selectedContractId || !signatureData) return;
    this.isSubmittingSignature = true;
    this.candidatureService.signContractByFreelancer(this.selectedContractId, signatureData).subscribe({
      next: () => {
        this.successMessage = 'Freelink Success: Contract signed successfully!';
        this.closeSignatureModal();
        this.isSubmittingSignature = false;
        this.loadApplications(this.currentUserId);
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        console.error(err);
        this.isSubmittingSignature = false;
      }
    });
  }

  launchTracker(contractId: string, freelancerId: string) {
    this.timeTrackingService.launchTracker(contractId, freelancerId);
  }
}
