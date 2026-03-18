package com.projectmanagement.model;

import java.time.LocalDateTime;

public class Assignee {
    private int id;
    private String fullName;
    private String email;
    private String position;
    private String department;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;

    public Assignee() {
        this.active = true;
    }

    public Assignee(String fullName, String email, String position, String department) {
        this.fullName = fullName;
        this.email = email;
        this.position = position;
        this.department = department;
        this.active = true;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + (department != null ? " (" + department + ")" : "");
    }
}