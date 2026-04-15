export interface Availability {
  id?: number;
  resourceName: string;
  description: string;
  date: string;
  startTime: string;
  endTime: string;
  maxSlots: number;
  location: string;
  isActive?: boolean;
  availableSlots?: number;
}
