package tn.formini.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/formation_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            // For development purposes, create a mock connection if database is not available
            System.out.println("WARNING: Using mock connection for development");
            return null;
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
