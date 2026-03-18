package com.projectmanagement.dao;

import com.projectmanagement.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskDAO {

    public boolean add(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (project_id, assignee_id, task_name, description, priority, " +
                "status, estimated_hours, actual_hours, deadline) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, task.getProjectId());

            if (task.getAssigneeId() != null) {
                pstmt.setInt(2, task.getAssigneeId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            pstmt.setString(3, task.getTitle());
            pstmt.setString(4, task.getDescription());
            pstmt.setString(5, task.getPriority());
            pstmt.setString(6, task.getStatus() != null ? task.getStatus() : "NEW");
            pstmt.setInt(7, task.getEstimatedHours());
            pstmt.setInt(8, task.getActualHours());

            if (task.getDeadline() != null) {
                pstmt.setDate(9, Date.valueOf(task.getDeadline()));
            } else {
                pstmt.setNull(9, Types.DATE);
            }

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        }
    }

    public List<Task> getByProject(int projectId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = """
        SELECT t.*, a.full_name as assignee_name 
        FROM tasks t 
        LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
        WHERE t.project_id = ? 
        ORDER BY t.task_id
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public Task getTaskById(int taskId) throws SQLException {
        String sql = """
            SELECT t.*, a.full_name as assignee_name 
            FROM tasks t 
            LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
            WHERE t.task_id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                return task;
            }
        }
        return null;
    }

    public boolean update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET task_name = ?, description = ?, priority = ?, " +
                "assignee_id = ?, status = ?, estimated_hours = ?, actual_hours = ?, " +
                "deadline = ? WHERE task_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getPriority());

            if (task.getAssigneeId() != null) {
                pstmt.setInt(4, task.getAssigneeId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setString(5, task.getStatus());
            pstmt.setInt(6, task.getEstimatedHours());
            pstmt.setInt(7, task.getActualHours());

            if (task.getDeadline() != null) {
                pstmt.setDate(8, Date.valueOf(task.getDeadline()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            pstmt.setInt(9, task.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE task_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Task> searchByAssigneeName(String assigneeName) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = """
            SELECT t.*, a.full_name as assignee_name 
            FROM tasks t 
            LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
            WHERE a.full_name LIKE ? 
            ORDER BY t.task_id
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + assigneeName + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> searchTasks(String criteria, String value) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "";

        switch (criteria.toLowerCase()) {
            case "priority":
                sql = """
                    SELECT t.*, a.full_name as assignee_name 
                    FROM tasks t 
                    LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
                    WHERE t.priority = ? 
                    ORDER BY t.task_id
                    """;
                break;
            case "status":
                sql = """
                    SELECT t.*, a.full_name as assignee_name 
                    FROM tasks t 
                    LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
                    WHERE t.status = ? 
                    ORDER BY t.task_id
                    """;
                break;
            default:
                return tasks;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = """
        SELECT t.*, a.full_name as assignee_name 
        FROM tasks t 
        LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
        ORDER BY t.task_id
        """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> getOverdueTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = """
            SELECT t.*, a.full_name as assignee_name 
            FROM tasks t 
            LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
            WHERE t.deadline < CURDATE() 
            AND t.status != 'COMPLETED' 
            ORDER BY t.deadline
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> getTasksByAssignee(int assigneeId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = """
            SELECT t.*, a.full_name as assignee_name 
            FROM tasks t 
            LEFT JOIN assignees a ON t.assignee_id = a.assignee_id 
            WHERE t.assignee_id = ? 
            ORDER BY t.task_id
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assigneeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = resultSetToTask(rs);
                task.setAssigneeName(rs.getString("assignee_name"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public int[] getTasksStatistics() throws SQLException {
        int[] stats = new int[5];

        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'NEW' THEN 1 ELSE 0 END) as new_count,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
                SUM(CASE WHEN deadline < CURDATE() AND status != 'COMPLETED' THEN 1 ELSE 0 END) as overdue
            FROM tasks
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("new_count");
                stats[2] = rs.getInt("in_progress");
                stats[3] = rs.getInt("completed");
                stats[4] = rs.getInt("overdue");
            }
        }
        return stats;
    }

    public int[] getPriorityDistribution() throws SQLException {
        int[] distribution = new int[3];

        String sql = """
            SELECT 
                SUM(CASE WHEN priority = 'HIGH' THEN 1 ELSE 0 END) as high,
                SUM(CASE WHEN priority = 'MEDIUM' THEN 1 ELSE 0 END) as medium,
                SUM(CASE WHEN priority = 'LOW' THEN 1 ELSE 0 END) as low
            FROM tasks
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                distribution[0] = rs.getInt("high");
                distribution[1] = rs.getInt("medium");
                distribution[2] = rs.getInt("low");
            }
        }
        return distribution;
    }

    public List<Object[]> getTasksPerProject() throws SQLException {
        List<Object[]> result = new ArrayList<>();

        String sql = """
            SELECT 
                p.project_name,
                COUNT(t.task_id) as task_count
            FROM projects p
            LEFT JOIN tasks t ON p.project_id = t.project_id
            GROUP BY p.project_id, p.project_name
            ORDER BY task_count DESC
            LIMIT 10
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Object[]{
                        rs.getString("project_name"),
                        rs.getInt("task_count")
                });
            }
        }
        return result;
    }

    public List<Object[]> getWeeklyCompletionStats() throws SQLException {
        List<Object[]> result = new ArrayList<>();

        String sql = """
            WITH weeks AS (
                SELECT 0 as week_offset UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 
                UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7
            )
            SELECT 
                DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL w.week_offset WEEK), '%Y-%U') as week_id,
                DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL w.week_offset WEEK), '%d.%m') as week_label,
                COUNT(t.task_id) as completed_tasks
            FROM weeks w
            LEFT JOIN tasks t ON 
                YEARWEEK(t.created_at, 1) = YEARWEEK(DATE_SUB(CURDATE(), INTERVAL w.week_offset WEEK), 1)
                AND t.status = 'COMPLETED'
            GROUP BY week_id, week_label
            ORDER BY week_id
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Object[]{
                        rs.getString("week_label"),
                        rs.getInt("completed_tasks")
                });
            }
        }
        return result;
    }

    public double getNextWeekForecast() throws SQLException {
        String sql = """
            SELECT 
                AVG(completed_tasks) as forecast
            FROM (
                SELECT 
                    YEARWEEK(created_at, 1) as week,
                    COUNT(*) as completed_tasks
                FROM tasks
                WHERE status = 'COMPLETED'
                    AND created_at >= DATE_SUB(CURDATE(), INTERVAL 8 WEEK)
                GROUP BY YEARWEEK(created_at, 1)
                ORDER BY week DESC
                LIMIT 4
            ) recent_weeks
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("forecast");
            }
        }
        return 0.0;
    }

    public Map<String, Object> getTaskAnalytics() throws SQLException {
        Map<String, Object> analytics = new HashMap<>();

        // Статистика выполнения
        List<Object[]> weeklyStats = getWeeklyCompletionStats();
        analytics.put("weeklyStats", weeklyStats);

        // Прогноз
        double forecast = getNextWeekForecast();
        analytics.put("forecast", forecast);

        // Распределение по статусам
        int[] statusStats = getTasksStatistics();
        analytics.put("statusStats", statusStats);

        // Распределение по приоритетам
        int[] priorityStats = getPriorityDistribution();
        analytics.put("priorityStats", priorityStats);

        // Эффективность
        Map<String, Object> efficiency = getTaskEfficiency();
        analytics.putAll(efficiency);

        // Рекомендации
        List<String> recommendations = generateRecommendations(analytics);
        analytics.put("recommendations", recommendations);

        return analytics;
    }

    public Map<String, Object> getTaskEfficiency() throws SQLException {
        Map<String, Object> efficiency = new HashMap<>();

        String sql = """
            SELECT 
                AVG(TIMESTAMPDIFF(HOUR, created_at, 
                    CASE WHEN status = 'COMPLETED' THEN updated_at ELSE NULL END)) as avg_completion_hours,
                COUNT(CASE WHEN status = 'COMPLETED' AND TIMESTAMPDIFF(DAY, created_at, updated_at) <= 7 THEN 1 END) as completed_within_week,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as total_completed,
                AVG(estimated_hours) as avg_estimated,
                AVG(actual_hours) as avg_actual
            FROM tasks
            WHERE status = 'COMPLETED'
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                efficiency.put("avgCompletionHours", rs.getDouble("avg_completion_hours"));
                efficiency.put("completedWithinWeek", rs.getInt("completed_within_week"));
                efficiency.put("totalCompleted", rs.getInt("total_completed"));
                efficiency.put("avgEstimated", rs.getDouble("avg_estimated"));
                efficiency.put("avgActual", rs.getDouble("avg_actual"));

                double onTimeRate = rs.getInt("total_completed") > 0 ?
                        (double) rs.getInt("completed_within_week") / rs.getInt("total_completed") * 100 : 0;
                efficiency.put("onTimeRate", onTimeRate);

                double estimationAccuracy = rs.getDouble("avg_estimated") > 0 ?
                        (rs.getDouble("avg_actual") / rs.getDouble("avg_estimated")) * 100 : 0;
                efficiency.put("estimationAccuracy", estimationAccuracy);
            }
        }
        return efficiency;
    }

    private List<String> generateRecommendations(Map<String, Object> analytics) {
        List<String> recommendations = new ArrayList<>();

        int[] statusStats = (int[]) analytics.get("statusStats");
        if (statusStats != null && statusStats.length >= 4) {
            int overdueTasks = statusStats[4];
            int totalTasks = statusStats[0];

            if (overdueTasks > 0) {
                recommendations.add("⚠️ Обратите внимание на просроченные задачи: " + overdueTasks);
                double overduePercentage = totalTasks > 0 ? (double) overdueTasks / totalTasks * 100 : 0;
                if (overduePercentage > 20) {
                    recommendations.add("🚨 Высокий процент просроченных задач (" + String.format("%.1f%%", overduePercentage) + ")");
                }
            }
        }

        double onTimeRate = (double) analytics.getOrDefault("onTimeRate", 0.0);
        if (onTimeRate < 50) {
            recommendations.add("📉 Низкий процент выполнения задач в срок: " + String.format("%.1f%%", onTimeRate));
        }

        double estimationAccuracy = (double) analytics.getOrDefault("estimationAccuracy", 100.0);
        if (estimationAccuracy < 80) {
            recommendations.add("⏰ Низкая точность оценки времени (разница " + String.format("%.1f%%", 100 - estimationAccuracy) + ")");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✅ Все показатели в норме! Продолжайте в том же духе.");
        }

        return recommendations;
    }

    private Task resultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("task_id"));
        task.setProjectId(rs.getInt("project_id"));

        int assigneeId = rs.getInt("assignee_id");
        if (!rs.wasNull()) {
            task.setAssigneeId(assigneeId);
        }

        task.setTitle(rs.getString("task_name"));
        task.setDescription(rs.getString("description"));
        task.setPriority(rs.getString("priority"));
        task.setStatus(rs.getString("status"));
        task.setEstimatedHours(rs.getInt("estimated_hours"));
        task.setActualHours(rs.getInt("actual_hours"));

        Date deadline = rs.getDate("deadline");
        if (deadline != null) {
            task.setDeadline(deadline.toLocalDate());
        }

        return task;
    }
}