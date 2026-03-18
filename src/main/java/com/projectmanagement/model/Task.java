package com.projectmanagement.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private int projectId;
    private Integer assigneeId;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String assigneeName;
    private int estimatedHours;
    private int actualHours;
    private LocalDate deadline;

    public Task() {
        this.status = "NEW";
        this.actualHours = 0;
    }

    public Task(int projectId, String title, String priority, Integer assigneeId) {
        this.projectId = projectId;
        this.title = title;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.status = "NEW";
        this.actualHours = 0;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Integer getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(int estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public int getActualHours() {
        return actualHours;
    }

    public void setActualHours(int actualHours) {
        this.actualHours = actualHours;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', priority='%s', status='%s', assigneeId=%d}",
                id, title, priority, status, assigneeId);
    }
}