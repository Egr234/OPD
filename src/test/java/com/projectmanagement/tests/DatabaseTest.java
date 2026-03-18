package com.projectmanagement.tests;

import com.projectmanagement.dao.DatabaseManager;
import com.projectmanagement.dao.ProjectDAO;
import com.projectmanagement.dao.TaskDAO;
import com.projectmanagement.model.Project;
import com.projectmanagement.model.Task;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest {

    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    private Project testProject;

    @BeforeAll
    void setup() throws Exception {
        // Инициализация базы данных
        DatabaseManager.initializeDatabase();
        projectDAO = new ProjectDAO();
        taskDAO = new TaskDAO();
    }

    @BeforeEach
    void createTestData() throws Exception {
        // Создание тестового проекта
        testProject = new Project();
        testProject.setName("JUnit Test Project");
        testProject.setManager("Test Manager");
        testProject.setDescription("Проект для тестирования");
        projectDAO.add(testProject);
    }

    @AfterEach
    void cleanup() throws Exception {
        // Удаление тестового проекта (задачи удалятся каскадно)
        if (testProject != null && testProject.getId() > 0) {
            projectDAO.delete(testProject.getId());
        }
    }

    @Test
    void testDatabaseConnection() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        assertNotNull(conn);
        assertFalse(conn.isClosed());

        // Проверка существования таблиц
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'projects'");
            assertTrue(rs.next(), "Таблица 'projects' должна существовать");

            rs = stmt.executeQuery("SHOW TABLES LIKE 'tasks'");
            assertTrue(rs.next(), "Таблица 'tasks' должна существовать");
        }
    }

    @Test
    void testProjectCRUD() throws Exception {
        // CREATE
        Project project = new Project();
        project.setName("Test Project CRUD");
        project.setManager("Test Manager");
        project.setDescription("Описание тестового проекта");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(30));
        project.setBudget(100000.50);
        project.setStatus("PLANNING");

        boolean added = projectDAO.add(project);
        assertTrue(added, "Проект должен быть добавлен");
        assertTrue(project.getId() > 0, "ID проекта должен быть присвоен");

        // READ
        Project retrieved = projectDAO.getById(project.getId());
        assertNotNull(retrieved, "Проект должен быть найден");
        assertEquals(project.getName(), retrieved.getName());
        assertEquals(project.getManager(), retrieved.getManager());

        // READ ALL
        List<Project> projects = projectDAO.getAll();
        assertFalse(projects.isEmpty(), "Список проектов не должен быть пустым");

        // UPDATE
        retrieved.setName("Updated Test Project");
        retrieved.setStatus("IN_PROGRESS");
        boolean updated = projectDAO.update(retrieved);
        assertTrue(updated, "Проект должен быть обновлен");

        Project updatedProject = projectDAO.getById(retrieved.getId());
        assertEquals("Updated Test Project", updatedProject.getName());
        assertEquals("IN_PROGRESS", updatedProject.getStatus());

        // SEARCH
        List<Project> searchResults = projectDAO.searchByName("Test");
        assertFalse(searchResults.isEmpty(), "Поиск должен найти проекты");

        // DELETE
        boolean deleted = projectDAO.delete(retrieved.getId());
        assertTrue(deleted, "Проект должен быть удален");

        Project deletedProject = projectDAO.getById(retrieved.getId());
        assertNull(deletedProject, "Удаленный проект не должен быть найден");
    }

    @Test
    void testTaskCRUD() throws Exception {
        // CREATE
        Task task = new Task();
        task.setProjectId(testProject.getId());
        task.setTitle("Test Task CRUD");
        task.setDescription("Описание тестовой задачи");
        task.setPriority("HIGH");
        task.setStatus("NEW");
        task.setAssignee("Test Assignee");
        task.setEstimatedHours(40);
        task.setActualHours(0);
        task.setDeadline(LocalDate.now().plusDays(7));

        boolean added = taskDAO.add(task);
        assertTrue(added, "Задача должна быть добавлена");
        assertTrue(task.getId() > 0, "ID задачи должен быть присвоен");

        // READ
        Task retrieved = taskDAO.getTaskById(task.getId());
        assertNotNull(retrieved, "Задача должна быть найдена");
        assertEquals(task.getTitle(), retrieved.getTitle());
        assertEquals(task.getAssignee(), retrieved.getAssignee());

        // READ BY PROJECT
        List<Task> projectTasks = taskDAO.getByProject(testProject.getId());
        assertFalse(projectTasks.isEmpty(), "Список задач проекта не должен быть пустым");

        // UPDATE
        retrieved.setTitle("Updated Test Task");
        retrieved.setStatus("IN_PROGRESS");
        retrieved.setActualHours(20);
        boolean updated = taskDAO.update(retrieved);
        assertTrue(updated, "Задача должна быть обновлена");

        Task updatedTask = taskDAO.getTaskById(retrieved.getId());
        assertEquals("Updated Test Task", updatedTask.getTitle());
        assertEquals("IN_PROGRESS", updatedTask.getStatus());
        assertEquals(20, updatedTask.getActualHours());

        // SEARCH
        List<Task> searchResults = taskDAO.searchByAssignee("Test");
        assertFalse(searchResults.isEmpty(), "Поиск должен найти задачи");

        List<Task> priorityResults = taskDAO.searchTasks("priority", "HIGH");
        assertFalse(priorityResults.isEmpty(), "Поиск по приоритету должен найти задачи");

        List<Task> statusResults = taskDAO.searchTasks("status", "IN_PROGRESS");
        assertFalse(statusResults.isEmpty(), "Поиск по статусу должен найти задачи");

        // DELETE
        boolean deleted = taskDAO.delete(retrieved.getId());
        assertTrue(deleted, "Задача должна быть удалена");

        Task deletedTask = taskDAO.getTaskById(retrieved.getId());
        assertNull(deletedTask, "Удаленная задача не должна быть найдена");
    }

    @Test
    void testCascadeDelete() throws Exception {
        // Создаем задачи для тестового проекта
        Task task1 = new Task();
        task1.setProjectId(testProject.getId());
        task1.setTitle("Task 1");
        task1.setAssignee("Assignee 1");
        task1.setPriority("MEDIUM");
        taskDAO.add(task1);

        Task task2 = new Task();
        task2.setProjectId(testProject.getId());
        task2.setTitle("Task 2");
        task2.setAssignee("Assignee 2");
        task2.setPriority("HIGH");
        taskDAO.add(task2);

        // Проверяем, что задачи созданы
        List<Task> tasksBeforeDelete = taskDAO.getByProject(testProject.getId());
        assertEquals(2, tasksBeforeDelete.size(), "Должно быть 2 задачи");

        // Удаляем проект
        boolean projectDeleted = projectDAO.delete(testProject.getId());
        assertTrue(projectDeleted, "Проект должен быть удален");

        // Проверяем, что задачи удалились каскадно
        List<Task> tasksAfterDelete = taskDAO.getByProject(testProject.getId());
        assertTrue(tasksAfterDelete.isEmpty(), "Задачи должны быть удалены каскадно");
    }

    @Test
    void testOverdueTasks() throws Exception {
        // Создаем просроченную задачу
        Task overdueTask = new Task();
        overdueTask.setProjectId(testProject.getId());
        overdueTask.setTitle("Overdue Task");
        overdueTask.setAssignee("Test Assignee");
        overdueTask.setPriority("HIGH");
        overdueTask.setDeadline(LocalDate.now().minusDays(1)); // Вчерашняя дата
        overdueTask.setStatus("IN_PROGRESS");
        taskDAO.add(overdueTask);

        // Создаем задачу с будущим дедлайном
        Task futureTask = new Task();
        futureTask.setProjectId(testProject.getId());
        futureTask.setTitle("Future Task");
        futureTask.setAssignee("Test Assignee");
        futureTask.setPriority("MEDIUM");
        futureTask.setDeadline(LocalDate.now().plusDays(1)); // Завтрашняя дата
        futureTask.setStatus("IN_PROGRESS");
        taskDAO.add(futureTask);

        // Создаем завершенную задачу с просроченным дедлайном
        Task completedTask = new Task();
        completedTask.setProjectId(testProject.getId());
        completedTask.setTitle("Completed Task");
        completedTask.setAssignee("Test Assignee");
        completedTask.setPriority("LOW");
        completedTask.setDeadline(LocalDate.now().minusDays(1)); // Вчерашняя дата
        completedTask.setStatus("COMPLETED");
        taskDAO.add(completedTask);

        List<Task> overdueTasks = taskDAO.getOverdueTasks();

        // Должна найтись только одна просроченная задача (не завершенная)
        assertEquals(1, overdueTasks.size(), "Должна быть найдена одна просроченная задача");
        assertEquals("Overdue Task", overdueTasks.get(0).getTitle());
    }

    @Test
    void testTasksWithProjects() throws Exception {
        List<String> results = taskDAO.getTasksWithProjects();
        assertNotNull(results, "Результаты не должны быть null");
        // Может быть пустым, если нет данных
    }
}