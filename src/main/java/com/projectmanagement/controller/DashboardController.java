package com.projectmanagement.controller;

import com.projectmanagement.service.AssigneeService;
import com.projectmanagement.service.ProjectService;
import com.projectmanagement.service.TaskService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class DashboardController {

    @FXML
    private Label lblProjectCount;

    @FXML
    private Label lblTaskCount;

    @FXML
    private Label lblAssigneeCount;

    @FXML
    private Label lblOverdueCount;

    private final ProjectService projectService;
    private final TaskService taskService;
    private final AssigneeService assigneeService;

    public DashboardController() {
        this.projectService = new ProjectService();
        this.taskService = new TaskService();
        this.assigneeService = new AssigneeService();
    }

    @FXML
    public void initialize() {
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try {
            // Активные проекты
            long activeProjectCount = projectService.getAllProjects().stream()
                    .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                    .count();
            lblProjectCount.setText(String.valueOf(activeProjectCount));

            // Все задачи
            int taskCount = taskService.getAllTasks().size();
            lblTaskCount.setText(String.valueOf(taskCount));

            // Активные разработчики
            int activeAssigneeCount = (int) assigneeService.getAllAssignees().stream()
                    .filter(a -> a.isActive())
                    .count();
            lblAssigneeCount.setText(String.valueOf(activeAssigneeCount));

            // Просроченные задачи
            int overdueCount = (int) taskService.getAllTasks().stream()
                    .filter(t -> t.getDeadline() != null &&
                            t.getDeadline().isBefore(LocalDate.now()) &&
                            !"COMPLETED".equals(t.getStatus()))
                    .count();
            lblOverdueCount.setText(String.valueOf(overdueCount));

        } catch (Exception e) {
            System.err.println("Ошибка загрузки статистики дашборда: " + e.getMessage());
        }
    }
}