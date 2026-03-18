package com.projectmanagement.service;

import com.projectmanagement.dao.ProjectDAO;
import com.projectmanagement.model.Project;
import com.projectmanagement.model.Task;

import java.sql.SQLException;
import java.util.List;

public class ProjectService {
    private final ProjectDAO projectDAO;
    private final TaskService taskService;

    public ProjectService() {
        this.projectDAO = new ProjectDAO();
        this.taskService = new TaskService();
    }

    public boolean addProject(Project project) {
        try {
            return projectDAO.add(project);
        } catch (SQLException e) {
            System.err.println("Ошибка добавления проекта: " + e.getMessage());
            return false;
        }
    }

    public List<Project> getAllProjects() {
        try {
            return projectDAO.getAll();
        } catch (SQLException e) {
            System.err.println("Ошибка получения проектов: " + e.getMessage());
            return List.of();
        }
    }

    public Project getProjectById(int id) {
        try {
            return projectDAO.getById(id);
        } catch (SQLException e) {
            System.err.println("Ошибка получения проекта по ID: " + e.getMessage());
            return null;
        }
    }

    public boolean updateProject(Project project) {
        try {
            return projectDAO.update(project);
        } catch (SQLException e) {
            System.err.println("Ошибка обновления проекта: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProject(int id) {
        try {
            return projectDAO.delete(id);
        } catch (SQLException e) {
            System.err.println("Ошибка удаления проекта: " + e.getMessage());
            return false;
        }
    }

    public List<Project> searchProjectsByName(String keyword) {
        try {
            return projectDAO.searchByName(keyword);
        } catch (SQLException e) {
            System.err.println("Ошибка поиска проектов: " + e.getMessage());
            return List.of();
        }
    }

    public int getTaskCount(int projectId) {
        List<Task> tasks = taskService.getTasksByProject(projectId);
        return tasks.size();
    }

    public List<Task> getProjectTasks(int projectId) {
        return taskService.getTasksByProject(projectId);
    }
}