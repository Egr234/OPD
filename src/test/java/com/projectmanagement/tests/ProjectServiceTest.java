package com.projectmanagement.tests;

import com.projectmanagement.model.Project;
import com.projectmanagement.service.ProjectService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectServiceTest {

    private ProjectService projectService;
    private Project testProject;

    @BeforeAll
    void setup() {
        projectService = new ProjectService();
    }

    @BeforeEach
    void createTestProject() {
        testProject = new Project();
        testProject.setName("Service Test Project");
        testProject.setManager("Service Test Manager");
        testProject.setDescription("Проект для тестирования сервиса");
        projectService.addProject(testProject);
    }

    @AfterEach
    void cleanup() {
        if (testProject != null && testProject.getId() > 0) {
            try {
                projectService.deleteProject(testProject.getId());
            } catch (Exception e) {
                // Игнорируем ошибки при очистке
            }
        }
    }

    @Test
    void testAddProject() {
        Project newProject = new Project();
        newProject.setName("New Service Project");
        newProject.setManager("New Service Manager");
        newProject.setStartDate(LocalDate.now());
        newProject.setEndDate(LocalDate.now().plusMonths(6));
        newProject.setBudget(500000);
        newProject.setStatus("PLANNING");

        boolean result = projectService.addProject(newProject);
        assertTrue(result, "Проект должен быть добавлен");
        assertTrue(newProject.getId() > 0, "ID должен быть присвоен");

        // Очистка
        if (newProject.getId() > 0) {
            projectService.deleteProject(newProject.getId());
        }
    }

    @Test
    void testGetProjectById() {
        Project retrieved = projectService.getProjectById(testProject.getId());
        assertNotNull(retrieved, "Проект должен быть найден");
        assertEquals(testProject.getName(), retrieved.getName());
        assertEquals(testProject.getManager(), retrieved.getManager());
    }

    @Test
    void testGetAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        assertFalse(projects.isEmpty(), "Список проектов не должен быть пустым");
    }

    @Test
    void testUpdateProject() {
        Project projectToUpdate = projectService.getProjectById(testProject.getId());
        projectToUpdate.setName("Updated Service Project");
        projectToUpdate.setStatus("IN_PROGRESS");
        projectToUpdate.setBudget(750000);

        boolean result = projectService.updateProject(projectToUpdate);
        assertTrue(result, "Проект должен быть обновлен");

        Project updated = projectService.getProjectById(testProject.getId());
        assertEquals("Updated Service Project", updated.getName());
        assertEquals("IN_PROGRESS", updated.getStatus());
        assertEquals(750000, updated.getBudget());
    }

    @Test
    void testSearchProjectsByName() {
        List<Project> results = projectService.searchProjectsByName("Service");
        assertFalse(results.isEmpty(), "Поиск должен найти проекты");

        results = projectService.searchProjectsByName("Nonexistent");
        assertTrue(results.isEmpty(), "Поиск не должен найти несуществующие проекты");
    }

    @Test
    void testDeleteProject() {
        Project projectToDelete = new Project();
        projectToDelete.setName("Project To Delete");
        projectToDelete.setManager("Delete Manager");
        projectService.addProject(projectToDelete);

        boolean result = projectService.deleteProject(projectToDelete.getId());
        assertTrue(result, "Проект должен быть удален");

        Project deleted = projectService.getProjectById(projectToDelete.getId());
        assertNull(deleted, "Удаленный проект не должен быть найден");
    }

    @Test
    void testGetTaskCount() {
        int taskCount = projectService.getTaskCount(testProject.getId());
        assertEquals(0, taskCount, "Изначально должно быть 0 задач");
    }

    @Test
    void testProjectExists() {
        boolean exists = projectService.projectExists(testProject.getId());
        assertTrue(exists, "Проект должен существовать");

        exists = projectService.projectExists(-1);
        assertFalse(exists, "Несуществующий проект не должен существовать");
    }
}