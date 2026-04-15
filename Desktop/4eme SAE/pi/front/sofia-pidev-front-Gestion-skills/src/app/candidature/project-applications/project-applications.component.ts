import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CandidatureService } from '../../services/candidature.service';
import { AuthService } from '../../services/auth.service';
import { Candidature } from '../../models/candidature.model';

@Component({
  selector: 'app-project-applications',
  templateUrl: './project-applications.component.html',
  styleUrls: ['./project-applications.component.css']
})
export class ProjectApplicationsComponent implements OnInit {
  applications: Candidature[] = [];
  projectId: string | null = null;
  clientId: string | null = null;
  loading: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  // Contract Form State
  showContractModal: boolean = false;
  selectedApp: Candidature | null = null;
  contractTerms: string = '';
  contractStartDate: string = '';
  contractEndDate: string = '';
  clientSignature: string = '';
  isSubmittingContract: boolean = false;
  contractErrors: any = {};

  hasActiveContract: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private candidatureService: CandidatureService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('projectId');

    this.authService.getCurrentUser().subscribe(user => {
      if (user) {
        this.clientId = user.id;
        if (this.projectId) {
          this.loadApplications();
        } else {
          this.errorMessage = 'Freelink: Project ID not found.';
          this.loading = false;
        }
      } else {
        this.errorMessage = 'Freelink: You must be securely logged in as a Client to view this.';
        this.loading = false;
      }
    });
  }

  loadApplications(): void {
    if (!this.projectId) return;

    this.loading = true;
    this.hasActiveContract = false;

    let requestObservable;
    if (this.authService.isAdmin()) {
      requestObservable = this.candidatureService.getProjectApplicationsForAdmin(this.projectId);
    } else if (this.clientId) {
      requestObservable = this.candidatureService.getProjectApplications(this.projectId, this.clientId);
    } else {
      this.errorMessage = 'Freelink: Cannot load applications without client ID or admin access.';
      this.loading = false;
      return;
    }

    requestObservable.subscribe({
      next: (data) => {
        this.candidatureService.getAllContracts().subscribe(contracts => {
          this.applications = data.map(app => {
            const contract = contracts.find(c => c.candidatureId === app.id);
            if (contract && contract.status !== 'ABORTED') {
              this.hasActiveContract = true;
            }
            return {
              ...app,
              expanded: false,
              contract: contract
            };
          });

          this.applications.forEach(app => {
            this.authService.getUserById(app.freelancerId).subscribe({
              next: (user) => app.freelancerName = user.firstName + ' ' + user.lastName,
              error: () => app.freelancerName = 'Unknown Freelancer'
            });
          });

          this.loading = false;
        });
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Freelink Server Error: Failed to load applications. Ensure you are the owner of this project.';
        this.loading = false;
      }
    });
  }

  accept(app: Candidature): void {
    this.selectedApp = app;
    this.showContractModal = true;
    this.contractTerms = "The freelancer agrees to provide services for the project...\n- Quality assurance\n- Weekly updates\n- Final delivery in source code";
  }

  closeContractModal(): void {
    this.showContractModal = false;
    this.selectedApp = null;
    this.contractErrors = {};
  }

  submitContract(): void {
    if (!this.selectedApp || !this.clientId || !this.projectId) return;

    this.contractErrors = {};
    let isValid = true;



    if (this.contractStartDate && this.contractEndDate) {
      if (new Date(this.contractEndDate) <= new Date(this.contractStartDate)) {
        this.contractErrors.endDate = 'End date must be after start date';
        isValid = false;
      }
    }

    if (!this.clientSignature) {
      this.errorMessage = 'Freelink Validation: Please provide your signature to proceed.';
      return;
    }

    if (!isValid) return;

    this.isSubmittingContract = true;

    const contractData = {
      candidatureId: this.selectedApp.id,
      projectId: this.projectId,
      clientId: this.clientId,
      freelancerId: this.selectedApp.freelancerId,
      terms: this.contractTerms,
      startDate: this.contractStartDate ? new Date(this.contractStartDate).toISOString() : null,
      endDate: this.contractEndDate ? new Date(this.contractEndDate).toISOString() : null,
      status: 'PENDING'
    };

    this.candidatureService.createContract(contractData).subscribe({
      next: (savedContract) => {
        this.candidatureService.signContractByClient(savedContract.id, this.clientSignature).subscribe({
          next: () => {
            this.candidatureService.acceptApplication(this.selectedApp!.id, this.clientId!).subscribe({
              next: (updatedApp) => {
                if (this.selectedApp) this.selectedApp.status = updatedApp.status;
                this.successMessage = `Freelink Success: Contract created and signed!`;
                this.closeContractModal();
                this.isSubmittingContract = false;
                this.loadApplications(); // Refresh to show status
                setTimeout(() => this.successMessage = '', 3000);
              },
              error: (err) => {
                this.errorMessage = 'Freelink Server Error: Contract created but failed to link properly.';
                this.isSubmittingContract = false;
              }
            });
          },
          error: (err) => {
            this.errorMessage = 'Freelink Server Error: Contract created but signature failed.';
            this.isSubmittingContract = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Freelink Server Error: Failed to create contract on our network.';
        this.isSubmittingContract = false;
      }
    });
  }

  reject(app: Candidature): void {
    if (!this.clientId) return;
    if (confirm('Freelink: Are you sure you want to reject this talent application?')) {
      this.candidatureService.rejectApplication(app.id, this.clientId).subscribe({
        next: (updatedApp) => {
          app.status = updatedApp.status;
          this.successMessage = `Freelink Update: Application rejected successfully.`;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => this.errorMessage = 'Freelink Server Error: Failed to reject application.'
      });
    }
  }

  downloadContract(appId: string): void {
    this.candidatureService.getAllContracts().subscribe(contracts => {
      const contract = contracts.find(c => c.candidatureId === appId);
      if (contract) {
        this.candidatureService.downloadContractPdf(contract.id).subscribe(blob => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `contract-${appId.substring(0, 8)}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        });
      } else {
        alert('Freelink: Contract not found for this application.');
      }
    });
  }

  downloadFile(app: Candidature): void {
    if (!app.data || !app.fileName) {
      alert('Freelink: No file attached to this application.');
      return;
    }
    const byteCharacters = atob(app.data);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: app.fileType });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = app.fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  // --- UI Helpers ---
  getContractStatusColor(status: string): string {
    switch (status) {
      case 'PENDING': return '#f39c12';
      case 'ONESIDED': return '#3498db';
      case 'COMPLETED': return '#27ae60';
      default: return '#95a5a6';
    }
  }

  getContractStatusText(status: string): string {
    switch (status) {
      case 'PENDING': return 'Awaiting Client Signature';
      case 'ONESIDED': return 'Signed by Client';
      case 'COMPLETED': return 'Fully Signed';
      default: return status;
    }
  }
}
