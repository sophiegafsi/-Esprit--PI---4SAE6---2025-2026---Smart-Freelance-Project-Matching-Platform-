export interface Notification {
    id: number;
    userId: string;
    type: 'NEW_BOOKING' | 'REMINDER_1H' | 'REMINDER_24H' | 'RESCHEDULE_SUGGESTION' | 'BOOKING_CONFIRMED' | 'BOOKING_CANCELLED';
    message: string;
    isRead: boolean;
    createdAt: string;
    referenceId?: number;
}
