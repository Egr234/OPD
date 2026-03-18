package com.projectmanagement.controller;

import com.projectmanagement.model.Assignee;
import com.projectmanagement.model.Task;
import com.projectmanagement.service.AssigneeService;
import com.projectmanagement.service.TaskService;
import com.projectmanagement.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class TaskDialogController {

    @FXML
    private Label lblProjectName;

    @FXML
    private TextField tfTaskTitle;

    @FXML
    private TextArea taTaskDescription;

    @FXML
    private ComboBox<String> cbPriority;

    @FXML
    private ComboBox<Assignee> cbAssignee;

    @FXML
    private TextField tfEstimatedHours;

    @FXML
    private DatePicker dpDeadline;

    private int projectId;
    private final TaskService taskService;
    private final AssigneeService assigneeService;

    public TaskDialogController() {
        this.taskService = new TaskService();
        this.assigneeService = new AssigneeService();
    }

    @FXML
    public void initialize() {
        cbPriority.getItems().addAll("HIGH", "MEDIUM", "LOW");
        cbPriority.setValue("MEDIUM");
        dpDeadline.setValue(LocalDate.now().plusDays(7));
        loadAssignees();
    }

    private void loadAssignees() {
        try {
            List<Assignee> assignees = assigneeService.getAllAssignees();
            cbAssignee.setItems(FXCollections.observableArrayList(assignees));
            cbAssignee.setConverter(new StringConverter<Assignee>() {
                @Override
                public String toString(Assignee assignee) {
                    return assignee != null ? assignee.getFullName() : "";
                }

                @Override
                public Assignee fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки исполнителей", e.getMessage());
        }
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setProjectName(String projectName) {
        lblProjectName.setText("Проект: " + projectName);
    }

    @FXML
    private void handleSaveTask() {
        if (!validateForm()) return;

        try {
            Task task = new Task();
            task.setProjectId(projectId);
            task.setTitle(tfTaskTitle.getText());
            task.setDescription(taTaskDescription.getText());
            task.setPriority(cbPriority.getValue());

            Assignee assignee = cbAssignee.getValue();
            if (assignee != null) {
                task.setAssigneeId(assignee.getId());
            }

            if (!tfEstimatedHours.getText().isEmpty()) {
                try {
                    task.setEstimatedHours(Integer.parseInt(tfEstimatedHours.getText()));
                } catch (NumberFormatException e) {
                    AlertUtil.showErrorAlert("Ошибка", "Введите число часов");
                    return;
                }
            }

            task.setDeadline(dpDeadline.getValue());

            if (taskService.addTask(task)) {
                AlertUtil.showInfoAlert("Успех", "Задача добавлена");
                closeDialog();
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateForm() {
        if (tfTaskTitle.getText() == null || tfTaskTitle.getText().trim().isEmpty()) {
            AlertUtil.showErrorAlert("Ошибка", "Введите название задачи");
            return false;
        }

        if (cbAssignee.getValue() == null) {
            AlertUtil.showErrorAlert("Ошибка", "Выберите исполнителя");
            return false;
        }

        if (dpDeadline.getValue() == null) {
            AlertUtil.showErrorAlert("Ошибка", "Выберите дедлайн");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Button btnCancel = (Button) tfTaskTitle.getScene().lookup("#btnCancel");
        if (btnCancel != null) {
            btnCancel.getScene().getWindow().hide();
        }
    }
}