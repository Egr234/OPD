package com.projectmanagement.service;

import com.projectmanagement.dao.TaskDAO;
import com.projectmanagement.model.Task;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TaskService {
    private final TaskDAO taskDAO;

    public TaskService() {
        this.taskDAO = new TaskDAO();
    }

    public boolean addTask(Task task) {
        try {
            return taskDAO.add(task);
        } catch (SQLException e) {
            System.err.println("Ошибка добавления задачи: " + e.getMessage());
            return false;
        }
    }

    public List<Task> getTasksByProject(int projectId) {
        try {
            return taskDAO.getByProject(projectId);
        } catch (SQLException e) {
            System.err.println("Ошибка получения задач по проекту: " + e.getMessage());
            return List.of();
        }
    }

    public Task getTaskById(int taskId) {
        try {
            return taskDAO.getTaskById(taskId);
        } catch (SQLException e) {
            System.err.println("Ошибка получения задачи по ID: " + e.getMessage());
            return null;
        }
    }

    public boolean updateTask(Task task) {
        try {
            return taskDAO.update(task);
        } catch (SQLException e) {
            System.err.println("Ошибка обновления задачи: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTask(int id) {
        try {
            return taskDAO.delete(id);
        } catch (SQLException e) {
            System.err.println("Ошибка удаления задачи: " + e.getMessage());
            return false;
        }
    }

    public List<Task> searchByAssigneeName(String assigneeName) {
        try {
            return taskDAO.searchByAssigneeName(assigneeName);
        } catch (SQLException e) {
            System.err.println("Ошибка поиска задач по исполнителю: " + e.getMessage());
            return List.of();
        }
    }

    public List<Task> searchTasks(String criteria, String value) {
        try {
            return taskDAO.searchTasks(criteria, value);
        } catch (SQLException e) {
            System.err.println("Ошибка поиска задач: " + e.getMessage());
            return List.of();
        }
    }

    public List<Task> getAllTasks() {
        try {
            return taskDAO.getAllTasks();
        } catch (SQLException e) {
            System.err.println("Ошибка получения всех задач: " + e.getMessage());
            return List.of();
        }
    }

    public List<Task> getOverdueTasks() {
        try {
            return taskDAO.getOverdueTasks();
        } catch (SQLException e) {
            System.err.println("Ошибка получения просроченных задач: " + e.getMessage());
            return List.of();
        }
    }

    public List<Task> getTasksByAssignee(int assigneeId) {
        try {
            return taskDAO.getTasksByAssignee(assigneeId);
        } catch (SQLException e) {
            System.err.println("Ошибка получения задач по исполнителю: " + e.getMessage());
            return List.of();
        }
    }

    public int[] getTasksStatistics() {
        try {
            return taskDAO.getTasksStatistics();
        } catch (SQLException e) {
            System.err.println("Ошибка получения статистики задач: " + e.getMessage());
            return new int[5];
        }
    }

    public int[] getPriorityDistribution() {
        try {
            return taskDAO.getPriorityDistribution();
        } catch (SQLException e) {
            System.err.println("Ошибка получения распределения приоритетов: " + e.getMessage());
            return new int[3];
        }
    }

    public List<Object[]> getTasksPerProject() {
        try {
            return taskDAO.getTasksPerProject();
        } catch (SQLException e) {
            System.err.println("Ошибка получения задач по проектам: " + e.getMessage());
            return List.of();
        }
    }

    public List<Object[]> getWeeklyCompletionStats() {
        try {
            return taskDAO.getWeeklyCompletionStats();
        } catch (SQLException e) {
            System.err.println("Ошибка получения статистики выполнения по неделям: " + e.getMessage());
            return List.of();
        }
    }

    public double getNextWeekForecast() {
        try {
            return taskDAO.getNextWeekForecast();
        } catch (SQLException e) {
            System.err.println("Ошибка получения прогноза: " + e.getMessage());
            return 0.0;
        }
    }

    public Map<String, Object> getTaskAnalytics() {
        try {
            return taskDAO.getTaskAnalytics();
        } catch (SQLException e) {
            System.err.println("Ошибка получения аналитики задач: " + e.getMessage());
            return Map.of();
        }
    }

    public Map<String, Object> getTaskEfficiency() {
        try {
            return taskDAO.getTaskEfficiency();
        } catch (SQLException e) {
            System.err.println("Ошибка получения эффективности задач: " + e.getMessage());
            return Map.of();
        }
    }
}