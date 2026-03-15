package com.calculator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:currency.db";

    public Database() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();

            // Создаем таблицу
            String sql = "CREATE TABLE IF NOT EXISTS currencies (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "symbol TEXT NOT NULL," +
                    "rate REAL NOT NULL" +
                    ");";
            stmt.execute(sql);

            // Проверяем, есть ли данные
            rs = stmt.executeQuery("SELECT COUNT(*) FROM currencies");
            if (rs.next() && rs.getInt(1) == 0) {
                // Закрываем предыдущие ресурсы перед вставкой
                rs.close();
                stmt.close();

                // Вставляем данные
                insertSampleData(conn);
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        } finally {
            // Всегда закрываем ресурсы
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    private void insertSampleData(Connection conn) {
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO currencies (name, symbol, rate) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            Object[][] currencies = {
                    {"Рубль", "₽", 1.0},
                    {"Доллар", "$", 91.5},
                    {"Евро", "€", 99.8},
                    {"Юань", "¥", 12.7}
            };

            for (Object[] currency : currencies) {
                pstmt.setString(1, (String) currency[0]);
                pstmt.setString(2, (String) currency[1]);
                pstmt.setDouble(3, (Double) currency[2]);
                pstmt.executeUpdate();
            }

            System.out.println("Sample currencies inserted successfully");
        } catch (SQLException e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        }
    }

    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM currencies ORDER BY name");

            while (rs.next()) {
                currencies.add(new Currency(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting currencies: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }

        return currencies;
    }

    public Currency getCurrencyById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            pstmt = conn.prepareStatement("SELECT * FROM currencies WHERE id = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Currency(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting currency by id: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }

        return null;
    }
}