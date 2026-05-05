export interface Availability {
    id?: number;
    freelancerId?: string;
    freelancerName?: string;
    resourceName?: string;
    description: string;
    date: string;
    startTime: string;
    endTime: string;
    maxSlots: number;
    location: string;
    isActive?: boolean;
}
