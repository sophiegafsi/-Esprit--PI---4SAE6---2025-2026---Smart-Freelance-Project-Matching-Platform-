export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface Booking {
    id?: number;
    availabilityId: number;
    userId?: string;
    freelancerId?: string;
    freelancerName?: string;
    userName: string;
    userEmail: string;
    status?: BookingStatus;
    notes?: string;
    createdAt?: string;
}
