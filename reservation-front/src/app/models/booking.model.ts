export interface Booking {
  id?: number;
  availabilityId: number;
  userId: string;
  userName: string;
  userEmail: string;
  status?: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}
