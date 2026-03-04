export interface Project {
    id: string;
    title: string;
    description: string;
    clientId: string;
    budget: number;
    status: 'OPEN' | 'CLOSED';
}
