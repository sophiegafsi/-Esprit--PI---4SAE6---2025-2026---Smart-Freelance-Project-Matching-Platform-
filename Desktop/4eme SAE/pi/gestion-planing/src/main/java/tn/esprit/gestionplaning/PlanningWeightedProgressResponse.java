package tn.esprit.gestionplaning;

public class PlanningWeightedProgressResponse {
    private Long planningId;
    private int totalTasks;
    private double weightedProgress;

    public PlanningWeightedProgressResponse() {
    }

    public PlanningWeightedProgressResponse(Long planningId, int totalTasks, double weightedProgress) {
        this.planningId = planningId;
        this.totalTasks = totalTasks;
        this.weightedProgress = weightedProgress;
    }

    public Long getPlanningId() {
        return planningId;
    }

    public void setPlanningId(Long planningId) {
        this.planningId = planningId;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public double getWeightedProgress() {
        return weightedProgress;
    }

    public void setWeightedProgress(double weightedProgress) {
        this.weightedProgress = weightedProgress;
    }
}