export interface Project {
    id: number | string;
    title: string;
    description: string;
    clientId: string;
    budget: number;
    status: 'OPEN' | 'CLOSED';
    tasks?: any[];
}
