package tn.esprit.service;

import org.springframework.stereotype.Service;
import tn.esprit.entities.Planning;
import tn.esprit.entities.Task;
import tn.esprit.entities.TaskPriority;
import tn.esprit.entities.TaskStatus;
import tn.esprit.exception.BusinessException;
import tn.esprit.exception.ResourceNotFoundException;
import tn.esprit.gestionplaning.PlanningDailyLoadResponse;
import tn.esprit.gestionplaning.PlanningEfficiencyResponse;
import tn.esprit.gestionplaning.PlanningProgressResponse;
import tn.esprit.gestionplaning.PlanningWeightedProgressResponse;
import tn.esprit.repository.PlanningRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanningService {

    private final PlanningRepository planningRepository;

    public PlanningService(PlanningRepository planningRepository) {
        this.planningRepository = planningRepository;
    }

    public List<Planning> getAllPlannings() {
        return planningRepository.findAll();
    }

    public Planning getPlanningById(Long id) {
        return planningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planning not found with id: " + id));
    }

    public Planning addPlanning(Planning planning) {
        validatePlanningDates(planning);
        return planningRepository.save(planning);
    }

    public Planning updatePlanning(Long id, Planning planningDetails) {
        Planning planning = getPlanningById(id);

        validatePlanningDates(planningDetails);

        planning.setTitle(planningDetails.getTitle());
        planning.setDescription(planningDetails.getDescription());
        planning.setStartDate(planningDetails.getStartDate());
        planning.setEndDate(planningDetails.getEndDate());
        planning.setStatus(planningDetails.getStatus());

        return planningRepository.save(planning);
    }

    public void deletePlanning(Long id) {
        Planning planning = getPlanningById(id);
        planningRepository.delete(planning);
    }

    public List<Planning> searchPlannings(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return planningRepository.findAll();
        }

        String lowerKeyword = keyword.toLowerCase().trim();

        List<Planning> titleDescMatches =
                planningRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        lowerKeyword,
                        lowerKeyword
                );

        List<Planning> all = planningRepository.findAll();

        for (Planning planning : all) {
            if (planning.getStatus() != null &&
                    planning.getStatus().name().toLowerCase().contains(lowerKeyword)) {
                if (!titleDescMatches.contains(planning)) {
                    titleDescMatches.add(planning);
                }
            }
        }

        return titleDescMatches;
    }

    private void validatePlanningDates(Planning planning) {
        if (planning.getStartDate() != null && planning.getEndDate() != null
                && planning.getEndDate().isBefore(planning.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }
    }

    public PlanningProgressResponse getPlanningProgress(Long planningId) {
        Planning planning = getPlanningById(planningId);

        int totalTasks = planning.getTasks() != null ? planning.getTasks().size() : 0;

        if (totalTasks == 0) {
            return new PlanningProgressResponse(
                    planningId,
                    0,
                    0,
                    0,
                    0,
                    0.0
            );
        }

        int doneTasks = 0;
        int inProgressTasks = 0;
        int todoTasks = 0;
        double totalScore = 0;

        for (Task task : planning.getTasks()) {
            if (task.getStatus() == TaskStatus.DONE) {
                doneTasks++;
                totalScore += 100;
            } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgressTasks++;
                totalScore += 50;
            } else if (task.getStatus() == TaskStatus.TODO) {
                todoTasks++;
            }
        }

        double progress = totalScore / totalTasks;

        return new PlanningProgressResponse(
                planningId,
                totalTasks,
                doneTasks,
                inProgressTasks,
                todoTasks,
                progress
        );
    }

    public PlanningWeightedProgressResponse getPlanningWeightedProgress(Long planningId) {
        Planning planning = getPlanningById(planningId);

        if (planning.getTasks() == null || planning.getTasks().isEmpty()) {
            return new PlanningWeightedProgressResponse(planningId, 0, 0.0);
        }

        double weightedScoreSum = 0.0;
        double totalWeight = 0.0;

        for (Task task : planning.getTasks()) {
            if (task.getStartTime() == null || task.getEndTime() == null
                    || task.getPriority() == null || task.getStatus() == null) {
                continue;
            }

            long durationMinutes = Duration.between(task.getStartTime(), task.getEndTime()).toMinutes();

            if (durationMinutes <= 0) {
                continue;
            }

            int priorityWeight = 1;
            if (task.getPriority() == TaskPriority.MEDIUM) {
                priorityWeight = 2;
            } else if (task.getPriority() == TaskPriority.HIGH) {
                priorityWeight = 3;
            }

            int baseProgress = 0;
            if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                baseProgress = 50;
            } else if (task.getStatus() == TaskStatus.DONE) {
                baseProgress = 100;
            }

            double taskWeight = durationMinutes * priorityWeight;

            weightedScoreSum += baseProgress * taskWeight;
            totalWeight += taskWeight;
        }

        double weightedProgress = 0.0;
        if (totalWeight > 0) {
            weightedProgress = weightedScoreSum / totalWeight;
        }

        weightedProgress = Math.round(weightedProgress * 10.0) / 10.0;

        return new PlanningWeightedProgressResponse(
                planningId,
                planning.getTasks().size(),
                weightedProgress
        );
    }

    public List<PlanningDailyLoadResponse> getPlanningDailyLoad(Long planningId) {
        Planning planning = getPlanningById(planningId);

        if (planning.getTasks() == null || planning.getTasks().isEmpty()) {
            return new ArrayList<>();
        }

        Map<LocalDate, Long> minutesByDate = new LinkedHashMap<>();
        Map<LocalDate, Integer> taskCountByDate = new LinkedHashMap<>();

        for (Task task : planning.getTasks()) {
            if (task.getTaskDate() == null || task.getStartTime() == null || task.getEndTime() == null) {
                continue;
            }

            long durationMinutes = Duration.between(task.getStartTime(), task.getEndTime()).toMinutes();

            if (durationMinutes <= 0) {
                continue;
            }

            LocalDate date = task.getTaskDate();

            minutesByDate.put(date, minutesByDate.getOrDefault(date, 0L) + durationMinutes);
            taskCountByDate.put(date, taskCountByDate.getOrDefault(date, 0) + 1);
        }

        List<PlanningDailyLoadResponse> result = new ArrayList<>();

        for (LocalDate date : minutesByDate.keySet()) {
            long totalMinutes = minutesByDate.get(date);
            int taskCount = taskCountByDate.get(date);
            double totalHours = Math.round((totalMinutes / 60.0) * 10.0) / 10.0;

            result.add(new PlanningDailyLoadResponse(
                    date,
                    taskCount,
                    totalMinutes,
                    totalHours
            ));
        }

        return result;
    }

    public PlanningEfficiencyResponse getPlanningEfficiency(Long planningId) {
        Planning planning = getPlanningById(planningId);

        if (planning.getTasks() == null || planning.getTasks().isEmpty()) {
            return new PlanningEfficiencyResponse(
                    planningId,
                    0,
                    0,
                    0.0,
                    0.0,
                    0.0,
                    "LOW"
            );
        }

        List<Task> validTasks = planning.getTasks().stream()
                .filter(task -> task.getTaskDate() != null
                        && task.getStartTime() != null
                        && task.getEndTime() != null)
                .toList();

        if (validTasks.isEmpty()) {
            return new PlanningEfficiencyResponse(
                    planningId,
                    0,
                    0,
                    0.0,
                    0.0,
                    0.0,
                    "LOW"
            );
        }

        long totalTaskMinutes = 0;
        long wastedMinutes = 0;

        HashMap<LocalDate, List<Task>> tasksByDate = new HashMap<>();

        for (Task task : validTasks) {
            long duration = Duration.between(task.getStartTime(), task.getEndTime()).toMinutes();
            if (duration > 0) {
                totalTaskMinutes += duration;
                tasksByDate.computeIfAbsent(task.getTaskDate(), k -> new ArrayList<>()).add(task);
            }
        }

        for (LocalDate date : tasksByDate.keySet()) {
            List<Task> dayTasks = tasksByDate.get(date);
            dayTasks.sort(Comparator.comparing(Task::getStartTime));

            for (int i = 0; i < dayTasks.size() - 1; i++) {
                LocalTime currentEnd = dayTasks.get(i).getEndTime();
                LocalTime nextStart = dayTasks.get(i + 1).getStartTime();

                long gap = Duration.between(currentEnd, nextStart).toMinutes();
                if (gap > 0) {
                    wastedMinutes += gap;
                }
            }
        }

        double averageTaskDuration = Math.round((totalTaskMinutes / (double) validTasks.size()) * 10.0) / 10.0;

        double taskDensity = 0.0;
        if ((totalTaskMinutes + wastedMinutes) > 0) {
            taskDensity = totalTaskMinutes / (double) (totalTaskMinutes + wastedMinutes);
        }
        taskDensity = Math.round(taskDensity * 100.0) / 100.0;

        double wastedPenalty = Math.min(40, wastedMinutes / 15.0);
        double durationPenalty = 0.0;

        if (averageTaskDuration < 30) {
            durationPenalty = 15;
        } else if (averageTaskDuration > 180) {
            durationPenalty = 10;
        }

        double densityBonus = taskDensity * 30.0;

        double efficiencyScore = 100 - wastedPenalty - durationPenalty + densityBonus;

        if (efficiencyScore > 100) efficiencyScore = 100;
        if (efficiencyScore < 0) efficiencyScore = 0;

        efficiencyScore = Math.round(efficiencyScore * 10.0) / 10.0;

        String efficiencyLevel;
        if (efficiencyScore >= 75) {
            efficiencyLevel = "HIGH";
        } else if (efficiencyScore >= 50) {
            efficiencyLevel = "GOOD";
        } else if (efficiencyScore >= 30) {
            efficiencyLevel = "MEDIUM";
        } else {
            efficiencyLevel = "LOW";
        }

        return new PlanningEfficiencyResponse(
                planningId,
                validTasks.size(),
                wastedMinutes,
                averageTaskDuration,
                taskDensity,
                efficiencyScore,
                efficiencyLevel
        );
    }
}