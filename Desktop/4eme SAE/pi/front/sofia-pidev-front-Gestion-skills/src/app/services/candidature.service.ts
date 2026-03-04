import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Candidature } from '../models/candidature.model';

@Injectable({
  providedIn: 'root'
})
export class CandidatureService {

  // Direct access to Condidature Service (port 8083) to match existing pattern
  private apiUrl = 'http://localhost:8083/api/candidatures';

  constructor(private http: HttpClient) { }

  apply(freelancerId: string, projectId: string, coverLetter: string, file?: File): Observable<Candidature> {
    const formData = new FormData();
    formData.append('freelancerId', freelancerId);
    formData.append('projectId', projectId);
    formData.append('coverLetter', coverLetter);

    if (file) {
      formData.append('file', file);
    }

    return this.http.post<Candidature>(this.apiUrl, formData);
  }

  updateApplication(id: string, freelancerId: string, coverLetter: string): Observable<Candidature> {
    const formData = new FormData();
    formData.append('freelancerId', freelancerId);
    formData.append('coverLetter', coverLetter);
    // Note: Update endpoint in backend expects RequestParam/RequestBody not necessarily multipart for update if no file, 
    // but controller might handle JSON. Actually Controller uses @RequestBody String coverLetter.
    // Let's check Controller again. 
    // Controller: put(@PathVariable UUID id, @RequestParam UUID freelancerId, @RequestBody String coverLetter)
    // So distinct params.

    return this.http.put<Candidature>(`${this.apiUrl}/${id}?freelancerId=${freelancerId}`, coverLetter);
  }

  deleteApplication(id: string, freelancerId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}?freelancerId=${freelancerId}`);
  }

  getMyApplications(freelancerId: string): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.apiUrl}/my?freelancerId=${freelancerId}`);
  }

  getProjectApplications(projectId: string, clientId: string): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.apiUrl}/project/${projectId}?clientId=${clientId}`);
  }

  acceptApplication(id: string, clientId: string): Observable<Candidature> {
    return this.http.put<Candidature>(`${this.apiUrl}/${id}/accept?clientId=${clientId}`, {});
  }

  rejectApplication(id: string, clientId: string): Observable<Candidature> {
    return this.http.put<Candidature>(`${this.apiUrl}/${id}/reject?clientId=${clientId}`, {});
  }

  // --- PROJECT ACTIONS ---
  getAllProjects(): Observable<any[]> {
    // apiUrl is .../api/candidatures. We need .../api/projects
    // Let's assume standard REST convention or define new URL.
    // Given the backend controller is ProjectController @RequestMapping("/api/projects")
    // And CandidatureService apiUrl is .../candidatures
    const projectApiUrl = this.apiUrl.replace('/candidatures', '/projects');
    return this.http.get<any[]>(projectApiUrl);
  }

  getProjectsByClient(clientId: string): Observable<any[]> {
    const projectApiUrl = this.apiUrl.replace('/candidatures', '/projects');
    return this.http.get<any[]>(`${projectApiUrl}/client/${clientId}`);
  }

  getProjectById(projectId: string): Observable<any> {
    const projectApiUrl = this.apiUrl.replace('/candidatures', '/projects');
    return this.http.get<any>(`${projectApiUrl}/${projectId}`);
  }

  getAllCandidatures(): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.apiUrl}/all`);
  }

  // --- CONTRACT ACTIONS ---
  private contractUrl = 'http://localhost:8083/api/contracts';

  createContract(contract: any): Observable<any> {
    return this.http.post<any>(this.contractUrl, contract);
  }

  getContract(id: string): Observable<any> {
    return this.http.get<any>(`${this.contractUrl}/${id}`);
  }

  getContractsByClient(clientId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.contractUrl}/client/${clientId}`);
  }

  getContractsByFreelancer(freelancerId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.contractUrl}/freelancer/${freelancerId}`);
  }

  getAllContracts(): Observable<any[]> {
    return this.http.get<any[]>(this.contractUrl);
  }

  downloadContractPdf(id: string): Observable<Blob> {
    return this.http.get(`${this.contractUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  signContractByClient(id: string, signature: string): Observable<any> {
    return this.http.put<any>(`${this.contractUrl}/${id}/sign/client`, signature);
  }

  signContractByFreelancer(id: string, signature: string): Observable<any> {
    return this.http.put<any>(`${this.contractUrl}/${id}/sign/freelancer`, signature);
  }
}
