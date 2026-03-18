package com.projectmanagement.controller;

import com.projectmanagement.model.Project;
import com.projectmanagement.model.Task;
import com.projectmanagement.service.ProjectService;
import com.projectmanagement.service.TaskService;
import com.projectmanagement.service.AssigneeService;
import com.projectmanagement.util.AlertUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class ProjectController {

    @FXML
    private TableView<Project> projectTable;

    @FXML
    private TableColumn<Project, Integer> colId;

    @FXML
    private TableColumn<Project, String> colName;

    @FXML
    private TableColumn<Project, String> colManager;

    @FXML
    private TableColumn<Project, String> colStatus;

    @FXML
    private TableColumn<Project, Integer> colTaskCount;

    @FXML
    private TableColumn<Project, LocalDate> colStartDate;

    @FXML
    private TextField tfSearchProject;

    @FXML
    private TextField tfProjectName;

    @FXML
    private TextArea taProjectDescription;

    @FXML
    private TextField tfProjectManager;

    @FXML
    private ComboBox<String> cbProjectStatus;

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private TextField tfBudget;

    @FXML
    private Button btnAddProject;

    @FXML
    private Button btnUpdateProject;

    @FXML
    private Button btnDeleteProject;

    @FXML
    private Button btnClearProject;

    @FXML
    private TableView<Task> projectTasksTable;

    @FXML
    private TableColumn<Task, Integer> colTaskId;

    @FXML
    private TableColumn<Task, String> colTaskName;

    @FXML
    private TableColumn<Task, String> colTaskPriority;

    @FXML
    private TableColumn<Task, String> colTaskStatus;

    @FXML
    private TableColumn<Task, String> colTaskAssignee;

    @FXML
    private Label lblProjectStats;

    private final ProjectService projectService;
    private final TaskService taskService;
    private final AssigneeService assigneeService;
    private final ObservableList<Project> projectList;
    private final ObservableList<Task> taskList;
    private Project selectedProject;

    public ProjectController() {
        this.projectService = new ProjectService();
        this.taskService = new TaskService();
        this.assigneeService = new AssigneeService();
        this.projectList = FXCollections.observableArrayList();
        this.taskList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupProjectTableView();
        setupProjectTasksTableView();

        cbProjectStatus.setItems(FXCollections.observableArrayList(
                "PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"
        ));
        cbProjectStatus.setValue("PLANNING");

        loadProjects();
        setupSearch();
        enableUpdateDeleteButtons(false);
    }

    private void setupProjectTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colManager.setCellValueFactory(new PropertyValueFactory<>("manager"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        colTaskCount.setCellValueFactory(cellData -> {
            int taskCount = projectService.getTaskCount(cellData.getValue().getId());
            return new SimpleIntegerProperty(taskCount).asObject();
        });

        projectTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedProject = newSelection;
                        loadProjectDetails(newSelection);
                        loadProjectTasks(newSelection.getId());
                        enableUpdateDeleteButtons(true);
                    }
                });
    }

    private void setupProjectTasksTableView() {
        colTaskId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTaskName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTaskPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTaskAssignee.setCellValueFactory(new PropertyValueFactory<>("assigneeName"));
    }

    private void loadProjects() {
        try {
            projectList.setAll(projectService.getAllProjects());
            projectTable.setItems(projectList);
            updateProjectStats();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки", e.getMessage());
        }
    }

    private void loadProjectDetails(Project project) {
        tfProjectName.setText(project.getName());
        taProjectDescription.setText(project.getDescription());
        tfProjectManager.setText(project.getManager());
        cbProjectStatus.setValue(project.getStatus());
        dpStartDate.setValue(project.getStartDate());
        dpEndDate.setValue(project.getEndDate());

        if (project.getBudget() > 0) {
            tfBudget.setText(String.format("%.2f", project.getBudget()));
        } else {
            tfBudget.setText("");
        }
    }

    private void loadProjectTasks(int projectId) {
        try {
            List<Task> tasks = projectService.getProjectTasks(projectId);
            taskList.setAll(tasks);
            projectTasksTable.setItems(taskList);
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки задач", e.getMessage());
        }
    }

    private void setupSearch() {
        tfSearchProject.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadProjects();
            } else {
                searchProjects(newValue.trim());
            }
        });
    }

    private void searchProjects(String searchText) {
        try {
            List<Project> projects = projectService.searchProjectsByName(searchText);
            projectList.setAll(projects);
            projectTable.setItems(projectList);
            updateProjectStats();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка поиска", e.getMessage());
        }
    }

    @FXML
    private void handleAddProject() {
        if (!validateProjectForm()) {
            return;
        }

        try {
            Project project = createProjectFromForm();
            if (projectService.addProject(project)) {
                AlertUtil.showInfoAlert("Успешно", "Проект успешно добавлен");
                loadProjects();
                clearForm();
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateProject() {
        if (selectedProject == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите проект для обновления");
            return;
        }

        if (!validateProjectForm()) {
            return;
        }

        try {
            Project updatedProject = createProjectFromForm();
            updatedProject.setId(selectedProject.getId());

            if (projectService.updateProject(updatedProject)) {
                AlertUtil.showInfoAlert("Успешно", "Проект успешно обновлен");
                loadProjects();
                clearForm();
                enableUpdateDeleteButtons(false);
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProject() {
        if (selectedProject == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите проект для удаления");
            return;
        }

        boolean confirm = AlertUtil.showConfirmationAlert(
                "Подтверждение удаления",
                "Вы уверены, что хотите удалить проект '" + selectedProject.getName() + "'?\n" +
                        "Все задачи в этом проекте также будут удалены!"
        );

        if (confirm) {
            try {
                if (projectService.deleteProject(selectedProject.getId())) {
                    AlertUtil.showInfoAlert("Успешно", "Проект успешно удален");
                    loadProjects();
                    clearForm();
                    enableUpdateDeleteButtons(false);
                }
            } catch (Exception e) {
                AlertUtil.showErrorAlert("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        projectTable.getSelectionModel().clearSelection();
        enableUpdateDeleteButtons(false);
    }

    @FXML
    private void handleAddTaskToProject() {
        if (selectedProject == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите проект для добавления задачи");
            return;
        }

        try {
            // Создаем новую задачу
            Task task = new Task();
            task.setProjectId(selectedProject.getId());
            task.setPriority("MEDIUM");
            task.setStatus("NEW");

            // Открываем диалог для ввода названия задачи
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Добавление новой задачи");
            dialog.setHeaderText("Добавить задачу в проект: " + selectedProject.getName());
            dialog.setContentText("Введите название задачи:");

            dialog.showAndWait().ifPresent(taskTitle -> {
                if (!taskTitle.trim().isEmpty()) {
                    task.setTitle(taskTitle);

                    try {
                        if (taskService.addTask(task)) {
                            AlertUtil.showInfoAlert("Успех", "Задача успешно добавлена");
                            loadProjectTasks(selectedProject.getId());
                            loadProjects(); // Обновляем счетчик задач
                        }
                    } catch (Exception e) {
                        AlertUtil.showErrorAlert("Ошибка", e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", "Не удалось добавить задачу: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = projectTasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите задачу для удаления");
            return;
        }

        boolean confirm = AlertUtil.showConfirmationAlert(
                "Подтверждение удаления",
                "Вы уверены, что хотите удалить задачу '" + selectedTask.getTitle() + "'?"
        );

        if (confirm) {
            try {
                if (taskService.deleteTask(selectedTask.getId())) {
                    AlertUtil.showInfoAlert("Успешно", "Задача успешно удалена");
                    loadProjectTasks(selectedProject.getId());
                    loadProjects(); // Обновляем счетчик задач
                }
            } catch (Exception e) {
                AlertUtil.showErrorAlert("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadProjects();
        if (selectedProject != null) {
            loadProjectTasks(selectedProject.getId());
        }
    }

    private Project createProjectFromForm() {
        Project project = new Project();
        project.setName(tfProjectName.getText());
        project.setDescription(taProjectDescription.getText());
        project.setManager(tfProjectManager.getText());
        project.setStatus(cbProjectStatus.getValue());
        project.setStartDate(dpStartDate.getValue());
        project.setEndDate(dpEndDate.getValue());

        if (!tfBudget.getText().isEmpty()) {
            try {
                project.setBudget(Double.parseDouble(tfBudget.getText()));
            } catch (NumberFormatException e) {
                project.setBudget(0);
            }
        }

        return project;
    }

    private boolean validateProjectForm() {
        if (tfProjectName.getText() == null || tfProjectName.getText().trim().isEmpty()) {
            AlertUtil.showErrorAlert("Ошибка", "Введите название проекта");
            tfProjectName.requestFocus();
            return false;
        }

        if (tfProjectManager.getText() == null || tfProjectManager.getText().trim().isEmpty()) {
            AlertUtil.showErrorAlert("Ошибка", "Введите руководителя проекта");
            tfProjectManager.requestFocus();
            return false;
        }

        if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
            if (dpStartDate.getValue().isAfter(dpEndDate.getValue())) {
                AlertUtil.showErrorAlert("Ошибка", "Дата начала не может быть позже даты окончания");
                dpStartDate.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        tfProjectName.clear();
        taProjectDescription.clear();
        tfProjectManager.clear();
        cbProjectStatus.setValue("PLANNING");
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        tfBudget.clear();

        if (projectTasksTable != null) {
            projectTasksTable.getItems().clear();
        }
    }

    private void enableUpdateDeleteButtons(boolean enable) {
        btnUpdateProject.setDisable(!enable);
        btnDeleteProject.setDisable(!enable);
    }

    private void updateProjectStats() {
        if (lblProjectStats != null) {
            long totalProjects = projectList.size();
            long activeProjects = projectList.stream()
                    .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                    .count();
            long completedProjects = projectList.stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()))
                    .count();

            lblProjectStats.setText(String.format(
                    "Всего проектов: %d | Активных: %d | Завершенных: %d",
                    totalProjects, activeProjects, completedProjects
            ));
        }
    }
}