package com.projectmanagement.controller;

import com.projectmanagement.model.Assignee;
import com.projectmanagement.model.Project;
import com.projectmanagement.model.Task;
import com.projectmanagement.service.AssigneeService;
import com.projectmanagement.service.ProjectService;
import com.projectmanagement.service.TaskService;
import com.projectmanagement.util.AlertUtil;
import com.projectmanagement.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class TaskController {

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, Integer> colTaskId;

    @FXML
    private TableColumn<Task, String> colTaskName;

    @FXML
    private TableColumn<Task, String> colProjectName;

    @FXML
    private TableColumn<Task, String> colPriority;

    @FXML
    private TableColumn<Task, String> colStatus;

    @FXML
    private TableColumn<Task, String> colAssignee;

    @FXML
    private TableColumn<Task, LocalDate> colDeadline;

    @FXML
    private ComboBox<Project> cbProjectFilter;

    @FXML
    private ComboBox<String> cbPriorityFilter;

    @FXML
    private ComboBox<String> cbStatusFilter;

    @FXML
    private ComboBox<Assignee> cbAssigneeFilter;

    @FXML
    private TextField tfTaskName;

    @FXML
    private ComboBox<Project> cbProject;

    @FXML
    private TextArea taTaskDescription;

    @FXML
    private ComboBox<String> cbPriority;

    @FXML
    private ComboBox<String> cbStatus;

    @FXML
    private ComboBox<Assignee> cbAssignee;

    @FXML
    private TextField tfEstimatedHours;

    @FXML
    private TextField tfActualHours;

    @FXML
    private DatePicker dpDeadline;

    @FXML
    private Button btnAddTask;

    @FXML
    private Button btnUpdateTask;

    @FXML
    private Button btnDeleteTask;

    @FXML
    private Button btnClearTask;

    @FXML
    private Label lblTaskStats;

    @FXML
    private Button btnMarkComplete;

    @FXML
    private Button btnMarkInProgress;

    private final TaskService taskService;
    private final ProjectService projectService;
    private final AssigneeService assigneeService;
    private final ObservableList<Task> taskList;
    private final ObservableList<Project> projectList;
    private final ObservableList<Assignee> assigneeList;
    private Task selectedTask;

    public TaskController() {
        this.taskService = new TaskService();
        this.projectService = new ProjectService();
        this.assigneeService = new AssigneeService();
        this.taskList = FXCollections.observableArrayList();
        this.projectList = FXCollections.observableArrayList();
        this.assigneeList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupForm();
        loadData();
    }

    private void setupTableColumns() {
        colTaskId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTaskName.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAssignee.setCellValueFactory(new PropertyValueFactory<>("assigneeName"));
        colProjectName.setCellValueFactory(cellData -> {
            int projectId = cellData.getValue().getProjectId();
            Project project = projectService.getProjectById(projectId);
            return new javafx.beans.property.SimpleStringProperty(
                    project != null ? project.getName() : "Неизвестный проект"
            );
        });

        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colDeadline.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Task, LocalDate> call(TableColumn<Task, LocalDate> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(DateUtil.formatDate(item));
                            if (DateUtil.isOverdue(item, getTableRow().getItem().getStatus())) {
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            }
                        }
                    }
                };
            }
        });

        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedTask = newSelection;
                        loadTaskDetails(newSelection);
                        enableActionButtons(true);
                    }
                });
    }

    private void setupFilters() {
        cbPriorityFilter.setItems(FXCollections.observableArrayList("Все", "HIGH", "MEDIUM", "LOW"));
        cbPriorityFilter.setValue("Все");
        cbStatusFilter.setItems(FXCollections.observableArrayList("Все", "NEW", "IN_PROGRESS", "COMPLETED"));
        cbStatusFilter.setValue("Все");

        cbPriorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbProjectFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbAssigneeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupForm() {
        cbPriority.setItems(FXCollections.observableArrayList("HIGH", "MEDIUM", "LOW"));
        cbPriority.setValue("MEDIUM");
        cbStatus.setItems(FXCollections.observableArrayList("NEW", "IN_PROGRESS", "COMPLETED"));
        cbStatus.setValue("NEW");
        dpDeadline.setValue(LocalDate.now().plusDays(7));

        cbProject.setConverter(new StringConverter<Project>() {
            @Override
            public String toString(Project project) {
                return project != null ? project.getName() : "";
            }

            @Override
            public Project fromString(String string) {
                return null;
            }
        });

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
    }

    private void loadData() {
        loadProjects();
        loadAssignees();
        loadTasks();
    }

    private void loadProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            projectList.setAll(projects);

            Project allProjects = new Project("Все проекты", "");
            allProjects.setId(-1);
            ObservableList<Project> filterProjects = FXCollections.observableArrayList();
            filterProjects.add(allProjects);
            filterProjects.addAll(projects);

            cbProjectFilter.setItems(filterProjects);
            cbProjectFilter.setValue(allProjects);
            cbProjectFilter.setConverter(new StringConverter<Project>() {
                @Override
                public String toString(Project project) {
                    return project != null ? project.getName() : "";
                }

                @Override
                public Project fromString(String string) {
                    return null;
                }
            });

            cbProject.setItems(FXCollections.observableArrayList(projects));

        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки проектов", e.getMessage());
        }
    }

    private void loadAssignees() {
        try {
            List<Assignee> assignees = assigneeService.getAllAssignees();
            assigneeList.setAll(assignees);

            Assignee allAssignees = new Assignee("Все исполнители", "", "", "");
            allAssignees.setId(-1);
            ObservableList<Assignee> filterAssignees = FXCollections.observableArrayList();
            filterAssignees.add(allAssignees);
            filterAssignees.addAll(assignees);

            cbAssigneeFilter.setItems(filterAssignees);
            cbAssigneeFilter.setValue(allAssignees);
            cbAssigneeFilter.setConverter(new StringConverter<Assignee>() {
                @Override
                public String toString(Assignee assignee) {
                    return assignee != null ? assignee.getFullName() : "";
                }

                @Override
                public Assignee fromString(String string) {
                    return null;
                }
            });

            cbAssignee.setItems(FXCollections.observableArrayList(assignees));

        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки исполнителей", e.getMessage());
        }
    }

    private void loadTasks() {
        try {
            taskList.setAll(taskService.getAllTasks());
            taskTable.setItems(taskList);
            updateStats();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка загрузки задач", e.getMessage());
        }
    }

    private void loadTaskDetails(Task task) {
        tfTaskName.setText(task.getTitle());
        taTaskDescription.setText(task.getDescription());
        cbPriority.setValue(task.getPriority());
        cbStatus.setValue(task.getStatus());
        tfEstimatedHours.setText(String.valueOf(task.getEstimatedHours()));
        tfActualHours.setText(String.valueOf(task.getActualHours()));
        dpDeadline.setValue(task.getDeadline());

        Project project = projectService.getProjectById(task.getProjectId());
        cbProject.setValue(project);

        if (task.getAssigneeId() != null) {
            Assignee assignee = assigneeService.getAssigneeById(task.getAssigneeId());
            cbAssignee.setValue(assignee);
        } else {
            cbAssignee.setValue(null);
        }
    }

    private void applyFilters() {
        ObservableList<Task> filtered = FXCollections.observableArrayList(taskList);

        Project selectedProject = cbProjectFilter.getValue();
        if (selectedProject != null && selectedProject.getId() != -1) {
            filtered.removeIf(task -> task.getProjectId() != selectedProject.getId());
        }

        String priority = cbPriorityFilter.getValue();
        if (priority != null && !"Все".equals(priority)) {
            filtered.removeIf(task -> !priority.equals(task.getPriority()));
        }

        String status = cbStatusFilter.getValue();
        if (status != null && !"Все".equals(status)) {
            filtered.removeIf(task -> !status.equals(task.getStatus()));
        }

        Assignee selectedAssignee = cbAssigneeFilter.getValue();
        if (selectedAssignee != null && selectedAssignee.getId() != -1) {
            filtered.removeIf(task -> {
                if (task.getAssigneeId() == null) return true;
                return task.getAssigneeId() != selectedAssignee.getId();
            });
        }

        taskTable.setItems(filtered);
        updateStats();
    }

    @FXML
    private void handleAddTask() {
        if (!validateForm()) return;

        try {
            Task task = new Task();
            Project project = cbProject.getValue();
            if (project != null) task.setProjectId(project.getId());

            Assignee assignee = cbAssignee.getValue();
            if (assignee != null) task.setAssigneeId(assignee.getId());

            task.setTitle(tfTaskName.getText());
            task.setDescription(taTaskDescription.getText());
            task.setPriority(cbPriority.getValue());
            task.setStatus(cbStatus.getValue());

            try {
                task.setEstimatedHours(Integer.parseInt(tfEstimatedHours.getText()));
            } catch (NumberFormatException e) {
                task.setEstimatedHours(0);
            }

            try {
                task.setActualHours(Integer.parseInt(tfActualHours.getText()));
            } catch (NumberFormatException e) {
                task.setActualHours(0);
            }

            task.setDeadline(dpDeadline.getValue());

            if (taskService.addTask(task)) {
                AlertUtil.showInfoAlert("Успешно", "Задача добавлена");
                loadTasks();
                clearForm();
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTask() {
        if (selectedTask == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите задачу");
            return;
        }

        if (!validateForm()) return;

        try {
            Task task = new Task();
            task.setId(selectedTask.getId());
            task.setProjectId(selectedTask.getProjectId());

            Assignee assignee = cbAssignee.getValue();
            if (assignee != null) task.setAssigneeId(assignee.getId());

            task.setTitle(tfTaskName.getText());
            task.setDescription(taTaskDescription.getText());
            task.setPriority(cbPriority.getValue());
            task.setStatus(cbStatus.getValue());

            try {
                task.setEstimatedHours(Integer.parseInt(tfEstimatedHours.getText()));
            } catch (NumberFormatException e) {
                task.setEstimatedHours(0);
            }

            try {
                task.setActualHours(Integer.parseInt(tfActualHours.getText()));
            } catch (NumberFormatException e) {
                task.setActualHours(0);
            }

            task.setDeadline(dpDeadline.getValue());

            if (taskService.updateTask(task)) {
                AlertUtil.showInfoAlert("Успешно", "Задача обновлена");
                loadTasks();
                clearForm();
                enableActionButtons(false);
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            AlertUtil.showWarningAlert("Предупреждение", "Выберите задачу");
            return;
        }

        boolean confirm = AlertUtil.showConfirmationAlert(
                "Подтверждение удаления",
                "Удалить задачу '" + selectedTask.getTitle() + "'?"
        );

        if (confirm) {
            if (taskService.deleteTask(selectedTask.getId())) {
                AlertUtil.showInfoAlert("Успешно", "Задача удалена");
                loadTasks();
                clearForm();
                enableActionButtons(false);
            }
        }
    }

    @FXML
    private void handleMarkComplete() {
        if (selectedTask == null) return;

        selectedTask.setStatus("COMPLETED");
        if (taskService.updateTask(selectedTask)) {
            AlertUtil.showInfoAlert("Успешно", "Задача завершена");
            loadTasks();
        }
    }

    @FXML
    private void handleMarkInProgress() {
        if (selectedTask == null) return;

        selectedTask.setStatus("IN_PROGRESS");
        if (taskService.updateTask(selectedTask)) {
            AlertUtil.showInfoAlert("Успешно", "Задача в работе");
            loadTasks();
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        taskTable.getSelectionModel().clearSelection();
        enableActionButtons(false);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private boolean validateForm() {
        if (cbProject.getValue() == null) {
            AlertUtil.showErrorAlert("Ошибка", "Выберите проект");
            return false;
        }

        if (tfTaskName.getText() == null || tfTaskName.getText().trim().isEmpty()) {
            AlertUtil.showErrorAlert("Ошибка", "Введите название задачи");
            return false;
        }

        if (cbAssignee.getValue() == null) {
            AlertUtil.showErrorAlert("Ошибка", "Выберите исполнителя");
            return false;
        }

        return true;
    }

    private void clearForm() {
        tfTaskName.clear();
        taTaskDescription.clear();
        cbPriority.setValue("MEDIUM");
        cbStatus.setValue("NEW");
        tfEstimatedHours.clear();
        tfActualHours.clear();
        dpDeadline.setValue(LocalDate.now().plusDays(7));
        cbProject.setValue(null);
        cbAssignee.setValue(null);
    }

    private void enableActionButtons(boolean enable) {
        btnUpdateTask.setDisable(!enable);
        btnDeleteTask.setDisable(!enable);
        btnMarkComplete.setDisable(!enable);
        btnMarkInProgress.setDisable(!enable);
    }

    private void updateStats() {
        if (lblTaskStats != null) {
            long total = taskTable.getItems().size();
            long newTasks = taskTable.getItems().stream().filter(t -> "NEW".equals(t.getStatus())).count();
            long inProgress = taskTable.getItems().stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
            long completed = taskTable.getItems().stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();

            lblTaskStats.setText(String.format("Всего: %d | Новые: %d | В работе: %d | Завершено: %d",
                    total, newTasks, inProgress, completed));
        }
    }
}