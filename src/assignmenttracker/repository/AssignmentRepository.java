package assignmenttracker.repository;

import assignmenttracker.models.Assignment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AssignmentRepository {

    private static final String URL = "jdbc:sqlite:assignment_tracker.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public ObservableList<Assignment> getAssignmentsByUser(int userId) {
        ObservableList<Assignment> list = FXCollections.observableArrayList();

        String sql = "SELECT * FROM assignments WHERE user_id = ? ORDER BY due_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Assignment a = new Assignment(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("subject"),
                        rs.getString("due_date"),
                        rs.getString("status"),
                        rs.getInt("progress"),
                        rs.getInt("priority"),
                        rs.getInt("user_id")
                    );
                    list.add(a);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    
    public void insert(Assignment a) {
        String sql = """
            INSERT INTO assignments
            (title, description, subject, due_date, status, progress, priority, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getTitle());
            stmt.setString(2, a.getDescription());
            stmt.setString(3, a.getSubject());
            stmt.setString(4, a.getDueDate());
            stmt.setString(5, a.getStatus());
            stmt.setInt(6, a.getProgress());
            stmt.setInt(7, a.getPriority());
            stmt.setInt(8, a.getUserId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public void delete(int id) {
        String sql = "DELETE FROM assignments WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public void update(Assignment a) {
        String sql = """
            UPDATE assignments
            SET title = ?,
                subject = ?,
                due_date = ?,
                status = ?,
                progress = ?,
                priority = ?,
                user_id = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, a.getTitle());
            stmt.setString(2, a.getSubject());
            stmt.setString(3, a.getDueDate());
            stmt.setString(4, a.getStatus());
            stmt.setInt(5, a.getProgress());
            stmt.setInt(6, a.getPriority());
            stmt.setInt(7, a.getUserId());
            stmt.setInt(8, a.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}