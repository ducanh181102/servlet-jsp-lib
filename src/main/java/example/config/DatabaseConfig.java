package example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfig {
    private static final Path DB_DIRECTORY = resolveDatabaseDirectory();
    private static boolean schemaInitialized;

    private DatabaseConfig() {
    }

    private static Path resolveDatabaseDirectory() {
        String configuredDirectory = System.getProperty("ducanh.library.database.directory");
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            return Paths.get(configuredDirectory);
        }
        return Paths.get(System.getProperty("user.home"), ".ducanh-library");
    }

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DB_DIRECTORY);
        } catch (IOException exception) {
            throw new SQLException("Không tạo được thư mục database.", exception);
        }

        try {
            // Tomcat's web-app classloader may not auto-register JDBC drivers from WEB-INF/lib.
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("Không tìm thấy SQLite JDBC trong WEB-INF/lib.", exception);
        }

        String url = "jdbc:sqlite:" + DB_DIRECTORY.resolve("library.db");
        Connection connection = DriverManager.getConnection(url);

        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema(connection);
            return connection;
        } catch (SQLException exception) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                exception.addSuppressed(closeException);
            }
            throw exception;
        }
    }

    private static synchronized void initializeSchema(Connection connection) throws SQLException {
        if (schemaInitialized) {
            return;
        }

        executeSqlResource(connection, "/db/schema.sql");
        executeSqlResource(connection, "/db/seed.sql");
        schemaInitialized = true;
    }

    private static void executeSqlResource(Connection connection, String resourcePath) throws SQLException {
        try (InputStream input = DatabaseConfig.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new SQLException("Không tìm thấy tệp SQL: " + resourcePath);
            }

            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement statement = connection.createStatement()) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql.trim());
                    }
                }
            }
        } catch (IOException exception) {
            throw new SQLException("Không đọc được tệp SQL: " + resourcePath, exception);
        }
    }
}
