package com.projectmanagement.service;

import com.projectmanagement.dao.AssigneeDAO;
import com.projectmanagement.model.Assignee;

import java.sql.SQLException;
import java.util.List;

public class AssigneeService {
    private final AssigneeDAO assigneeDAO;

    public AssigneeService() {
        this.assigneeDAO = new AssigneeDAO();
    }

    public List<Assignee> getAllAssignees() {
        try {
            return assigneeDAO.getAll();
        } catch (SQLException e) {
            System.err.println("Ошибка получения исполнителей: " + e.getMessage());
            return List.of();
        }
    }

    public Assignee getAssigneeById(int id) {
        try {
            return assigneeDAO.getById(id);
        } catch (SQLException e) {
            System.err.println("Ошибка получения исполнителя: " + e.getMessage());
            return null;
        }
    }

    public boolean addAssignee(Assignee assignee) {
        try {
            return assigneeDAO.add(assignee);
        } catch (SQLException e) {
            System.err.println("Ошибка добавления исполнителя: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAssignee(Assignee assignee) {
        try {
            return assigneeDAO.update(assignee);
        } catch (SQLException e) {
            System.err.println("Ошибка обновления исполнителя: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAssignee(int id) {
        try {
            return assigneeDAO.deactivate(id);
        } catch (SQLException e) {
            System.err.println("Ошибка удаления исполнителя: " + e.getMessage());
            return false;
        }
    }

    public List<Assignee> searchAssignees(String keyword) {
        try {
            return assigneeDAO.searchByName(keyword);
        } catch (SQLException e) {
            System.err.println("Ошибка поиска исполнителей: " + e.getMessage());
            return List.of();
        }
    }

    public int getTaskCount(int assigneeId) {
        try {
            return assigneeDAO.getTaskCount(assigneeId);
        } catch (SQLException e) {
            System.err.println("Ошибка получения количества задач: " + e.getMessage());
            return 0;
        }
    }

    public List<Object[]> getAssigneesWithTaskCount() {
        try {
            return assigneeDAO.getAssigneesWithTaskCount();
        } catch (SQLException e) {
            System.err.println("Ошибка получения статистики исполнителей: " + e.getMessage());
            return List.of();
        }
    }
}