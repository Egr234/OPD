package com.projectmanagement.dao;

import com.projectmanagement.model.Assignee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssigneeDAO {

    public boolean add(Assignee assignee) throws SQLException {
        String sql = "INSERT INTO assignees (full_name, email, position, department, phone) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, assignee.getFullName());
            pstmt.setString(2, assignee.getEmail());
            pstmt.setString(3, assignee.getPosition());
            pstmt.setString(4, assignee.getDepartment());
            pstmt.setString(5, assignee.getPhone());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    assignee.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        }
    }

    public List<Assignee> getAll() throws SQLException {
        List<Assignee> assignees = new ArrayList<>();
        String sql = "SELECT * FROM assignees WHERE is_active = TRUE ORDER BY full_name";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                assignees.add(resultSetToAssignee(rs));
            }
        }
        return assignees;
    }

    public Assignee getById(int id) throws SQLException {
        String sql = "SELECT * FROM assignees WHERE assignee_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultSetToAssignee(rs);
            }
        }
        return null;
    }

    public List<Assignee> searchByName(String keyword) throws SQLException {
        List<Assignee> assignees = new ArrayList<>();
        String sql = "SELECT * FROM assignees WHERE full_name LIKE ? AND is_active = TRUE ORDER BY full_name";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                assignees.add(resultSetToAssignee(rs));
            }
        }
        return assignees;
    }

    public boolean update(Assignee assignee) throws SQLException {
        String sql = "UPDATE assignees SET full_name = ?, email = ?, position = ?, " +
                "department = ?, phone = ?, is_active = ? WHERE assignee_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, assignee.getFullName());
            pstmt.setString(2, assignee.getEmail());
            pstmt.setString(3, assignee.getPosition());
            pstmt.setString(4, assignee.getDepartment());
            pstmt.setString(5, assignee.getPhone());
            pstmt.setBoolean(6, assignee.isActive());
            pstmt.setInt(7, assignee.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM assignees WHERE assignee_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int id) throws SQLException {
        String sql = "UPDATE assignees SET is_active = FALSE WHERE assignee_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int getTaskCount(int assigneeId) throws SQLException {
        String sql = "SELECT COUNT(*) as task_count FROM tasks WHERE assignee_id = ? AND status != 'COMPLETED'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assigneeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("task_count");
            }
        }
        return 0;
    }

    public List<Object[]> getAssigneesWithTaskCount() throws SQLException {
        List<Object[]> result = new ArrayList<>();

        String sql = """
            SELECT 
                a.assignee_id,
                a.full_name,
                a.position,
                a.department,
                COUNT(t.task_id) as task_count,
                SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_count
            FROM assignees a
            LEFT JOIN tasks t ON a.assignee_id = t.assignee_id
            WHERE a.is_active = TRUE
            GROUP BY a.assignee_id, a.full_name, a.position, a.department
            ORDER BY task_count DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(new Object[]{
                        rs.getInt("assignee_id"),
                        rs.getString("full_name"),
                        rs.getString("position"),
                        rs.getString("department"),
                        rs.getInt("task_count"),
                        rs.getInt("in_progress_count")
                });
            }
        }
        return result;
    }

    private Assignee resultSetToAssignee(ResultSet rs) throws SQLException {
        Assignee assignee = new Assignee();
        assignee.setId(rs.getInt("assignee_id"));
        assignee.setFullName(rs.getString("full_name"));
        assignee.setEmail(rs.getString("email"));
        assignee.setPosition(rs.getString("position"));
        assignee.setDepartment(rs.getString("department"));
        assignee.setPhone(rs.getString("phone"));
        assignee.setActive(rs.getBoolean("is_active"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            assignee.setCreatedAt(ts.toLocalDateTime());
        }

        return assignee;
    }
}