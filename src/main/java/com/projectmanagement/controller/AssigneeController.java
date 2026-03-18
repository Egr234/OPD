package com.projectmanagement.controller;

import com.projectmanagement.model.Assignee;
import com.projectmanagement.model.Task;
import com.projectmanagement.service.AssigneeService;
import com.projectmanagement.service.TaskService;
import com.projectmanagement.util.AlertUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class AssigneeController {

    @FXML
    private TableView<Assignee> assigneeTable;

    @FXML
    private TableColumn<Assignee, Integer> colAssigneeId;

    @FXML
    private TableColumn<Assignee, String> colFullName;

    @FXML
    private TableColumn<Assignee, String> colPosition;

    @FXML
    private TableColumn<Assignee, String> colDepartment;

    @FXML
    private TableColumn<Assignee, String> colEmail;

    @FXML
    private TableColumn<Assignee, Integer> colTaskCount;

    @FXML
    private TableColumn<Assignee, String> colStatus;

    @FXML
    private TextField tfSearchAssignee;

    @FXML
    private TextField tfFullName;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfPosition;

    @FXML
    private TextField tfDepartment;

    @FXML
    private TextField tfPhone;

    @FXML
    private ComboBox<String> cbActiveStatus;

    @FXML
    private Button btnAddAssignee;

    @FXML
    private Button btnUpdateAssignee;

    @FXML
    private Button btnDeleteAssignee;

    @FXML
    private Button btnClearAssignee;

    @FXML
    private Label lblAssigneeStats;

    @FXML
    private VBox assigneeStatsBox;

    private final AssigneeService assigneeService;
    private final TaskService taskService;
    private final ObservableList<Assignee> assigneeList;
    private Assignee selectedAssignee;

    public AssigneeController() {
        this.assigneeService = new AssigneeService();
        this.taskService = new TaskService();
        this.assigneeList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupAssigneeTable();
        setupStatusComboBox();
        loadAssignees();
        setupSearch();
        enableActionButtons(false);
    }

    private void setupAssigneeTable() {
        colAssigneeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            return new javafx.beans.property.SimpleStringProperty(
                    isActive ? "✅ Активен" : "❌ Неактивен"
            );
        });

        colTaskCount.setCellValueFactory(cellData -> {
            int taskCount = assigneeService.getTaskCount(cellData.getValue().getId());
            return new SimpleIntegerProperty(taskCount).asObject();
        });

        assigneeTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedAssignee = newSelection;
                        loadAssigneeDetails(newSelection);
                        loadAssigneeStats(newSelection.getId());
                        enableActionButtons(true);
                    }
                });
    }

    private void setupStatusComboBox() {
        cbActiveStatus.setItems(FXCollections.observableArrayList(
                "✅ Активен", "❌ Неактивен"
        ));
        cbActiveStatus.setValue("✅ Активен");
    }

    private void loadAssignees() {
        try {
            assigneeList.setAll(assigneeService.getAllAssignees());
            assigneeTable.setItems(assigneeList);
            updateAssigneeStats();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки", e.getMessage());
        }
    }

    private void loadAssigneeDetails(Assignee assignee) {
        tfFullName.setText(assignee.getFullName());
        tfEmail.setText(assignee.getEmail());
        tfPosition.setText(assignee.getPosition());
        tfDepartment.setText(assignee.getDepartment());
        tfPhone.setText(assignee.getPhone());
        cbActiveStatus.setValue(assignee.isActive() ? "✅ Активен" : "❌ Неактивен");
    }

    private void loadAssigneeStats(int assigneeId) {
        assigneeStatsBox.getChildren().clear();

        try {
            List<Task> tasks = taskService.getTasksByAssignee(assigneeId);
            int totalTasks = tasks.size();
            int inProgressTasks = (int) tasks.stream()
                    .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                    .count();
            int completedTasks = (int) tasks.stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .count();
            int overdueTasks = (int) tasks.stream()
                    .filter(t -> t.getDeadline() != null &&
                            t.getDeadline().isBefore(java.time.LocalDate.now()) &&
                            !"COMPLETED".equals(t.getStatus()))
                    .count();

            Label statsTitle = new Label("📊 Статистика задач:");
            statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label totalLabel = new Label("• Всего задач: " + totalTasks);
            Label inProgressLabel = new Label("• В работе: " + inProgressTasks);
            Label completedLabel = new Label("• Завершено: " + completedTasks);
            Label overdueLabel = new Label("• Просрочено: " + overdueTasks);

            if (overdueTasks > 0) {
                overdueLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }

            assigneeStatsBox.getChildren().addAll(
                    statsTitle, totalLabel, inProgressLabel, completedLabel, overdueLabel
            );

            if (totalTasks > 0) {
                Label tasksTitle = new Label("\n📋 Активные задачи:");
                tasksTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 0 5 0;");
                assigneeStatsBox.getChildren().add(tasksTitle);

                for (Task task : tasks) {
                    if (!"COMPLETED".equals(task.getStatus())) {
                        String statusIcon = "🟡";
                        if ("IN_PROGRESS".equals(task.getStatus())) statusIcon = "🔵";

                        String taskText = String.format("%s %s | %s",
                                statusIcon,
                                task.getTitle(),
                                task.getPriority());

                        Label taskLabel = new Label(taskText);
                        taskLabel.setWrapText(true);
                        assigneeStatsBox.getChildren().add(taskLabel);
                    }
                }
            }

        } catch (Exception e) {
            Label errorLabel = new Label("❌ Ошибка загрузки статистики");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            assigneeStatsBox.getChildren().add(errorLabel);
        }
    }

    private void setupSearch() {
        tfSearchAssignee.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadAssignees();
            } else {
                searchAssignees(newValue.trim());
            }
        });
    }

    private void searchAssignees(String searchText) {
        try {
            List<Assignee> assignees = assigneeService.searchAssignees(searchText);
            assigneeList.setAll(assignees);
            assigneeTable.setItems(assigneeList);
            updateAssigneeStats();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка поиска", e.getMessage());
        }
    }

    @FXML
    private void handleAddAssignee() {
        if (!validateAssigneeForm()) {
            return;
        }

        try {
            Assignee assignee = createAssigneeFromForm();
            if (assigneeService.addAssignee(assignee)) {
                AlertUtil.showInfoAlert("Успешно", "Разработчик успешно добавлен");
                loadAssignees();
                clearForm();
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateAssignee() {
        if (selectedAssignee == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите разработчика для обновления");
            return;
        }

        if (!validateAssigneeForm()) {
            return;
        }

        try {
            Assignee updatedAssignee = createAssigneeFromForm();
            updatedAssignee.setId(selectedAssignee.getId());

            if (assigneeService.updateAssignee(updatedAssignee)) {
                AlertUtil.showInfoAlert("Успешно", "Разработчик успешно обновлен");
                loadAssignees();
                clearForm();
                enableActionButtons(false);
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAssignee() {
        if (selectedAssignee == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите разработчика для удаления");
            return;
        }

        boolean confirm = AlertUtil.showConfirmationAlert(
                "Подтверждение удаления",
                "Вы уверены, что хотите удалить разработчика '" + selectedAssignee.getFullName() + "'?\n" +
                        "Все задачи, назначенные этому разработчику, станут без исполнителя."
        );

        if (confirm) {
            try {
                if (assigneeService.deleteAssignee(selectedAssignee.getId())) {
                    AlertUtil.showInfoAlert("Успешно", "Разработчик успешно удален");
                    loadAssignees();
                    clearForm();
                    enableActionButtons(false);
                }
            } catch (Exception e) {
                AlertUtil.showErrorAlert("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        assigneeTable.getSelectionModel().clearSelection();
        enableActionButtons(false);
    }

    @FXML
    private void handleRefresh() {
        loadAssignees();
        if (selectedAssignee != null) {
            loadAssigneeStats(selectedAssignee.getId());
        }
    }

    private Assignee createAssigneeFromForm() {
        Assignee assignee = new Assignee();
        assignee.setFullName(tfFullName.getText());
        assignee.setEmail(tfEmail.getText());
        assignee.setPosition(tfPosition.getText());
        assignee.setDepartment(tfDepartment.getText());
        assignee.setPhone(tfPhone.getText());
        assignee.setActive(cbActiveStatus.getValue().equals("✅ Активен"));
        return assignee;
    }

    private boolean validateAssigneeForm() {
        if (tfFullName.getText() == null || tfFullName.getText().trim().isEmpty()) {
            AlertUtil.showErrorAlert("Ошибка", "Введите ФИО разработчика");
            tfFullName.requestFocus();
            return false;
        }

        if (tfEmail.getText() == null || tfEmail.getText().trim().isEmpty()) {
            AlertUtil.showErrorAlert("Ошибка", "Введите email разработчика");
            tfEmail.requestFocus();
            return false;
        }

        if (!tfEmail.getText().contains("@")) {
            AlertUtil.showErrorAlert("Ошибка", "Введите корректный email");
            tfEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        tfFullName.clear();
        tfEmail.clear();
        tfPosition.clear();
        tfDepartment.clear();
        tfPhone.clear();
        cbActiveStatus.setValue("✅ Активен");
        assigneeStatsBox.getChildren().clear();
        assigneeStatsBox.getChildren().add(
                new Label("Выберите разработчика для просмотра статистики")
        );
    }

    private void enableActionButtons(boolean enable) {
        btnUpdateAssignee.setDisable(!enable);
        btnDeleteAssignee.setDisable(!enable);
    }

    private void updateAssigneeStats() {
        if (lblAssigneeStats != null) {
            long totalAssignees = assigneeList.size();
            long activeAssignees = assigneeList.stream()
                    .filter(Assignee::isActive)
                    .count();

            lblAssigneeStats.setText(String.format(
                    "Всего разработчиков: %d | Активных: %d",
                    totalAssignees, activeAssignees
            ));
        }
    }
}