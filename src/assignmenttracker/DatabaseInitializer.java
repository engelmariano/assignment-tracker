package assignmenttracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String URL = "jdbc:sqlite:assignment_tracker.db";

    public static void initialize() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                username    TEXT NOT NULL UNIQUE,
                password    TEXT NOT NULL,
                full_name   TEXT,
                created_at  TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """;
        
        String createAssignmentsTable = """
            CREATE TABLE IF NOT EXISTS assignments (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                title           TEXT NOT NULL,
                description     TEXT,
                subject         TEXT,
                due_date        TEXT NOT NULL,
                status          TEXT NOT NULL DEFAULT 'Pending',
                priority        INTEGER DEFAULT 2,           -- 1=High, 2=Normal, 3=Low (for example)
                progress        INTEGER DEFAULT 0,    
                user_id         INTEGER,                    -- owner / created_by
                created_at      TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at      TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
            );
            """;

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createAssignmentsTable);

            // Insert a default admin user if not exists
            String insertAdmin = """
                INSERT OR IGNORE INTO users (id, username, password, full_name)
                VALUES (1, 'John', '1234', 'John Smith');
                """;
             
            stmt.execute(insertAdmin);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
