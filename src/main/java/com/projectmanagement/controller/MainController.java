package com.projectmanagement.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane mainPane;

    @FXML
    private Button btnProjects;

    @FXML
    private Button btnTasks;

    @FXML
    private Button btnReports;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnAssignees; // Новая кнопка

    @FXML
    public void initialize() {
        btnDashboard.getStyleClass().add("active-btn");
        loadDashboard();
    }

    @FXML
    private void loadDashboard() {
        loadView("dashboard-view.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    private void loadProjects() {
        loadView("project-view.fxml");
        setActiveButton(btnProjects);
    }

    @FXML
    private void loadTasks() {
        loadView("task-view.fxml");
        setActiveButton(btnTasks);
    }

    @FXML
    private void loadReports() {
        loadView("reports-view.fxml");
        setActiveButton(btnReports);
    }

    @FXML
    private void loadAssignees() {
        loadView("assignees-view.fxml");
        setActiveButton(btnAssignees);
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/projectmanagement/view/" + fxmlFile));
            Parent view = loader.load();
            mainPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка загрузки", "Не удалось загрузить " + fxmlFile);
        }
    }

    private void setActiveButton(Button activeButton) {
        btnDashboard.getStyleClass().remove("active-btn");
        btnProjects.getStyleClass().remove("active-btn");
        btnTasks.getStyleClass().remove("active-btn");
        btnReports.getStyleClass().remove("active-btn");
        btnAssignees.getStyleClass().remove("active-btn");

        activeButton.getStyleClass().add("active-btn");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}