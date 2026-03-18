package com.projectmanagement.dao;

import com.projectmanagement.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    // ==================== СУЩЕСТВУЮЩИЕ МЕТОДЫ ====================

    public boolean add(Project project) throws SQLException {
        String sql = "INSERT INTO projects (project_name, description, start_date, end_date, " +
                "manager, budget, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());

            if (project.getStartDate() != null) {
                pstmt.setDate(3, Date.valueOf(project.getStartDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            if (project.getEndDate() != null) {
                pstmt.setDate(4, Date.valueOf(project.getEndDate()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.setString(5, project.getManager());
            pstmt.setDouble(6, project.getBudget());
            pstmt.setString(7, project.getStatus() != null ? project.getStatus() : "PLANNING");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    project.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        }
    }

    public List<Project> getAll() throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY project_id";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(resultSetToProject(rs));
            }
        }
        return projects;
    }

    public Project getById(int id) throws SQLException {
        String sql = "SELECT * FROM projects WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultSetToProject(rs);
            }
        }
        return null;
    }

    public boolean update(Project project) throws SQLException {
        String sql = "UPDATE projects SET project_name = ?, description = ?, start_date = ?, " +
                "end_date = ?, manager = ?, budget = ?, status = ? WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());

            if (project.getStartDate() != null) {
                pstmt.setDate(3, Date.valueOf(project.getStartDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            if (project.getEndDate() != null) {
                pstmt.setDate(4, Date.valueOf(project.getEndDate()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.setString(5, project.getManager());
            pstmt.setDouble(6, project.getBudget());
            pstmt.setString(7, project.getStatus());
            pstmt.setInt(8, project.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM projects WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Project> searchByName(String keyword) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE project_name LIKE ? ORDER BY project_id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                projects.add(resultSetToProject(rs));
            }
        }
        return projects;
    }

    public boolean projectExists(int projectId) throws SQLException {
        String sql = "SELECT 1 FROM projects WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    // ==================== НОВЫЕ МЕТОДЫ ДЛЯ ОТЧЕТОВ ====================

    /**
     * Получить статистику по проектам
     * @return массив [total, active, completed]
     */
    public int[] getProjectsStatistics() throws SQLException {
        int[] stats = new int[3];

        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as active,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed
            FROM projects
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("active");
                stats[2] = rs.getInt("completed");
            }
        }
        return stats;
    }

    /**
     * Получить все проекты с количеством задач
     * @return список массивов [id, name, status, manager, taskCount]
     */
    public List<Object[]> getAllProjectsWithTaskCount() throws SQLException {
        List<Object[]> result = new ArrayList<>();

        String sql = """
            SELECT 
                p.project_id,
                p.project_name,
                p.status,
                p.manager,
                COUNT(t.task_id) as task_count
            FROM projects p
            LEFT JOIN tasks t ON p.project_id = t.project_id
            GROUP BY p.project_id, p.project_name, p.status, p.manager
            ORDER BY p.project_id
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Object[]{
                        rs.getInt("project_id"),
                        rs.getString("project_name"),
                        rs.getString("status"),
                        rs.getString("manager"),
                        rs.getInt("task_count")
                });
            }
        }
        return result;
    }

    /**
     * Получить проекты с бюджетной статистикой
     */
    public List<Object[]> getProjectsWithBudgetStats() throws SQLException {
        List<Object[]> result = new ArrayList<>();

        String sql = """
            SELECT 
                p.project_name,
                p.budget,
                p.status,
                COUNT(t.task_id) as task_count,
                SUM(t.estimated_hours) as total_hours,
                p.manager
            FROM projects p
            LEFT JOIN tasks t ON p.project_id = t.project_id
            GROUP BY p.project_id, p.project_name, p.budget, p.status, p.manager
            ORDER BY p.budget DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Object[]{
                        rs.getString("project_name"),
                        rs.getDouble("budget"),
                        rs.getString("status"),
                        rs.getInt("task_count"),
                        rs.getInt("total_hours"),
                        rs.getString("manager")
                });
            }
        }
        return result;
    }

    /**
     * Получить проекты с самыми срочными задачами
     */
    public List<Object[]> getProjectsWithUrgentTasks() throws SQLException {
        List<Object[]> result = new ArrayList<>();

        String sql = """
            SELECT 
                p.project_name,
                COUNT(t.task_id) as urgent_tasks,
                MIN(t.deadline) as nearest_deadline
            FROM projects p
            JOIN tasks t ON p.project_id = t.project_id
            WHERE t.priority = 'HIGH' 
              AND t.status != 'COMPLETED'
              AND t.deadline IS NOT NULL
            GROUP BY p.project_id, p.project_name
            HAVING COUNT(t.task_id) > 0
            ORDER BY urgent_tasks DESC, nearest_deadline ASC
            LIMIT 5
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Object[]{
                        rs.getString("project_name"),
                        rs.getInt("urgent_tasks"),
                        rs.getDate("nearest_deadline")
                });
            }
        }
        return result;
    }

    private Project resultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("project_id"));
        project.setName(rs.getString("project_name"));
        project.setDescription(rs.getString("description"));
        project.setManager(rs.getString("manager"));
        project.setStatus(rs.getString("status"));
        project.setBudget(rs.getDouble("budget"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            project.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            project.setEndDate(endDate.toLocalDate());
        }

        return project;
    }
}