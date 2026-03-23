package com.vault.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:vault_data.db";
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                String createUsersTable = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, master_hash TEXT NOT NULL, panic_hash TEXT NOT NULL);";
                String createVaultTable = "CREATE TABLE IF NOT EXISTS passwords (id INTEGER PRIMARY KEY AUTOINCREMENT, website TEXT NOT NULL, username TEXT NOT NULL, password TEXT NOT NULL, is_decoy BOOLEAN NOT NULL);";
                Statement stmt = connection.createStatement();
                stmt.execute(createUsersTable);
                stmt.execute(createVaultTable);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return connection;
    }
}