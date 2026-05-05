export interface PlanningEfficiency {
  planningId: number;
  totalTasks: number;
  wastedMinutes: number;
  averageTaskDuration: number;
  taskDensity: number;
  efficiencyScore: number;
  efficiencyLevel: string;
}