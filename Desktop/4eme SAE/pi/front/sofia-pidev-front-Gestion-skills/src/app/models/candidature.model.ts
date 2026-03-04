export enum CandidatureStatus {
    PENDING = 'PENDING',
    ACCEPTED = 'ACCEPTED',
    REJECTED = 'REJECTED'
}

export interface Candidature {
    id: string;
    freelancerId: string;
    projectId: string;
    coverLetter: string;
    status: CandidatureStatus;
    applicationDate: string;
    fileName?: string;
    fileType?: string;
    data?: string; // Spring Boot serializes byte[] to Base64 string by default
    projectTitle?: string;
    clientName?: string;
    freelancerName?: string;
    expanded?: boolean;
    contract?: any;
}
