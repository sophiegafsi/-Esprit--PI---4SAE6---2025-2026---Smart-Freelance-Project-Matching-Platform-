package tn.esprit.gestionplaning;

public class PlanningEfficiencyResponse {
    private Long planningId;
    private int totalTasks;
    private long wastedMinutes;
    private double averageTaskDuration;
    private double taskDensity;
    private double efficiencyScore;
    private String efficiencyLevel;

    public PlanningEfficiencyResponse() {
    }

    public PlanningEfficiencyResponse(Long planningId, int totalTasks, long wastedMinutes,
                                      double averageTaskDuration, double taskDensity,
                                      double efficiencyScore, String efficiencyLevel) {
        this.planningId = planningId;
        this.totalTasks = totalTasks;
        this.wastedMinutes = wastedMinutes;
        this.averageTaskDuration = averageTaskDuration;
        this.taskDensity = taskDensity;
        this.efficiencyScore = efficiencyScore;
        this.efficiencyLevel = efficiencyLevel;
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

    public long getWastedMinutes() {
        return wastedMinutes;
    }

    public void setWastedMinutes(long wastedMinutes) {
        this.wastedMinutes = wastedMinutes;
    }

    public double getAverageTaskDuration() {
        return averageTaskDuration;
    }

    public void setAverageTaskDuration(double averageTaskDuration) {
        this.averageTaskDuration = averageTaskDuration;
    }

    public double getTaskDensity() {
        return taskDensity;
    }

    public void setTaskDensity(double taskDensity) {
        this.taskDensity = taskDensity;
    }

    public double getEfficiencyScore() {
        return efficiencyScore;
    }

    public void setEfficiencyScore(double efficiencyScore) {
        this.efficiencyScore = efficiencyScore;
    }

    public String getEfficiencyLevel() {
        return efficiencyLevel;
    }

    public void setEfficiencyLevel(String efficiencyLevel) {
        this.efficiencyLevel = efficiencyLevel;
    }
}