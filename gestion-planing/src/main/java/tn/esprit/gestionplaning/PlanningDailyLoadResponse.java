package tn.esprit.gestionplaning;

import java.time.LocalDate;

public class PlanningDailyLoadResponse {
    private LocalDate date;
    private int taskCount;
    private long totalMinutes;
    private double totalHours;

    public PlanningDailyLoadResponse() {
    }

    public PlanningDailyLoadResponse(LocalDate date, int taskCount, long totalMinutes, double totalHours) {
        this.date = date;
        this.taskCount = taskCount;
        this.totalMinutes = totalMinutes;
        this.totalHours = totalHours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
}