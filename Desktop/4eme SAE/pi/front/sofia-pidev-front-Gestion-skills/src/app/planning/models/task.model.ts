export interface Task {
  id?: number;
  title: string;
  description: string;
  taskDate: string;
  startTime: string;
  endTime: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
}