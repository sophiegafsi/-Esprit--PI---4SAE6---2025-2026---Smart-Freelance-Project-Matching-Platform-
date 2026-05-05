package tn.esprit.gestionplaning;

public class PlanningProgressResponse {
    private Long planningId;
    private int totalTasks;
    private int doneTasks;
    private int inProgressTasks;
    private int todoTasks;
    private double progress;

    public PlanningProgressResponse() {
    }

    public PlanningProgressResponse(Long planningId, int totalTasks, int doneTasks, int inProgressTasks, int todoTasks, double progress) {
        this.planningId = planningId;
        this.totalTasks = totalTasks;
        this.doneTasks = doneTasks;
        this.inProgressTasks = inProgressTasks;
        this.todoTasks = todoTasks;
        this.progress = progress;
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

    public int getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(int doneTasks) {
        this.doneTasks = doneTasks;
    }

    public int getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(int inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public int getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(int todoTasks) {
        this.todoTasks = todoTasks;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }
}