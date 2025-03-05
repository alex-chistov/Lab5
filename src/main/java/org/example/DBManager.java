package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private String url;
    private String user;
    private String password;

    public DBManager(String dbName, String user, String password) {
        this.url = "jdbc:postgresql://localhost:5432/" + dbName;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection(String dbName) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/" + dbName + "?options=-c%20search_path=public";
        return DriverManager.getConnection(dbUrl, user, password);
    }

    public void initProcedures() throws Exception {
        String dbName = getDatabaseName();
        String script = SQLScriptExecutor.readSQLScript("/stored_procedures.sql");
        SQLScriptExecutor.executeSQLScript(dbName, script, user, password);
    }

    private String getDatabaseName() {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public void createDatabase(String newDbName) throws SQLException {
        try (Connection conn = getConnection("postgres");
             CallableStatement stmt = conn.prepareCall("CALL sp_create_database(?, ?)")) {
            stmt.setString(1, newDbName);
            stmt.setString(2, password); // используем пароль, переданный при инициализации объекта DBManager
            stmt.execute();
        }
    }

    public void dropDatabase(String dbName) throws SQLException {
        try (Connection conn = getConnection("postgres");
             CallableStatement stmt = conn.prepareCall("CALL sp_drop_database(?, ?)")) {
            stmt.setString(1, dbName);
            stmt.setString(2, password);
            stmt.execute();
        }
    }

    public void createTable(String tableName) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_create_table(?)")) {
            stmt.setString(1, tableName);
            stmt.execute();
        }
    }

    public void clearTable(String tableName) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_clear_table(?)")) {
            stmt.setString(1, tableName);
            stmt.execute();
        }
    }

    public void addBook(String tableName, String title, String author, String publisher, int year) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_add_book(?, ?, ?, ?, ?)")) {
            stmt.setString(1, tableName);
            stmt.setString(2, title);
            stmt.setString(3, author);
            stmt.setString(4, publisher);
            stmt.setInt(5, year);
            stmt.execute();
        }
    }

    public List<Book> searchBookByTitle(String tableName, String title) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM sp_search_book_by_title(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, title);
            try (ResultSet rs = stmt.executeQuery()) {
                while(rs.next()){
                    Book book = new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            rs.getInt("year")
                    );
                    books.add(book);
                }
            }
        }
        return books;
    }

    public void updateBook(String tableName, int id, String title, String author, String publisher, int year) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_update_book(?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, tableName);
            stmt.setInt(2, id);
            stmt.setString(3, title);
            stmt.setString(4, author);
            stmt.setString(5, publisher);
            stmt.setInt(6, year);
            stmt.execute();
        }
    }

    public void deleteBookByTitle(String tableName, String title) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_delete_book_by_title(?, ?)")) {
            stmt.setString(1, tableName);
            stmt.setString(2, title);
            stmt.execute();
        }
    }


}
