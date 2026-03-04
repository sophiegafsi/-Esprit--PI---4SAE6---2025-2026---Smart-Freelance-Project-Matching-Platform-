import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { CandidatureService } from '../services/candidature.service';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
    clients: any[] = [];
    freelancers: any[] = [];
    admins: any[] = [];
    candidatures: any[] = [];
    contracts: any[] = [];

    activeTab: 'users' | 'applications' | 'contracts' = 'users';
    activeSubTab: 'clients' | 'freelancers' | 'admins' = 'clients';
    isUsersMenuOpen = true; // Default open for admin dashboard

    loading = true;
    error = '';

    constructor(
        private authService: AuthService,
        private candidatureService: CandidatureService,
        private router: Router
    ) { }

    ngOnInit(): void {
        if (!this.authService.isAdmin()) {
            this.router.navigate(['/']);
            return;
        }
        this.loadData();
    }

    toggleUsersMenu(): void {
        this.isUsersMenuOpen = !this.isUsersMenuOpen;
    }

    setActiveTab(tab: 'users' | 'applications' | 'contracts'): void {
        this.activeTab = tab;
    }

    setActiveSubTab(subTab: 'clients' | 'freelancers' | 'admins'): void {
        this.activeTab = 'users';
        this.activeSubTab = subTab;
    }

    toggleId(item: any, field: string): void {
        const key = `expanded_${field}`;
        item[key] = !item[key];
    }

    loadData(): void {
        this.loading = true;

        forkJoin({
            users: this.authService.getAllUsers(),
            projects: this.candidatureService.getAllProjects(),
            apps: this.candidatureService.getAllCandidatures(),
            contracts: this.candidatureService.getAllContracts()
        }).subscribe({
            next: (data) => {
                // 1. Separate Users by Role
                this.clients = data.users.filter((u: any) =>
                    u.role && u.role.split(',').map((r: string) => r.trim().toLowerCase()).includes('client')
                );
                this.freelancers = data.users.filter((u: any) =>
                    u.role && u.role.split(',').map((r: string) => r.trim().toLowerCase()).includes('freelancer')
                );
                this.admins = data.users.filter((u: any) =>
                    u.role && u.role.split(',').map((r: string) => r.trim().toLowerCase()).includes('admin')
                );

                // 2. Map Candidatures with Detailed Info
                this.candidatures = data.apps.map((app: any) => {
                    const freelancer = data.users.find((u: any) => u.id === app.freelancerId);
                    const project = data.projects.find((p: any) => p.id === app.projectId);
                    let client = null;
                    if (project) {
                        client = data.users.find((u: any) => u.id === project.clientId);
                    }

                    return {
                        ...app,
                        freelancerName: freelancer ? `${freelancer.firstName} ${freelancer.lastName}` : 'Unknown',
                        projectName: project ? project.title : 'Deleted Project',
                        clientName: client ? `${client.firstName} ${client.lastName}` : (project ? 'Unknown Client' : '-'),
                        clientId: project ? project.clientId : null
                    };
                });

                // 3. Map Contracts with Detailed Info
                this.contracts = data.contracts.map((contract: any) => {
                    const freelancer = data.users.find((u: any) => u.id === contract.freelancerId);
                    const client = data.users.find((u: any) => u.id === contract.clientId);
                    const project = data.projects.find((p: any) => p.id === contract.projectId);

                    return {
                        ...contract,
                        freelancerName: freelancer ? `${freelancer.firstName} ${freelancer.lastName}` : 'Unknown',
                        clientName: client ? `${client.firstName} ${client.lastName}` : 'Unknown',
                        projectName: project ? project.title : 'Deleted Project'
                    };
                });

                this.loading = false;
            },
            error: (err) => {
                this.error = 'Failed to load system data';
                this.loading = false;
                console.error('Admin Dashboard Load Error:', err);
            }
        });
    }

    downloadPdf(contractId: string): void {
        this.candidatureService.downloadContractPdf(contractId).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `contract-${contractId.substring(0, 8)}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            },
            error: (err) => console.error('PDF Download Error:', err)
        });
    }
}
