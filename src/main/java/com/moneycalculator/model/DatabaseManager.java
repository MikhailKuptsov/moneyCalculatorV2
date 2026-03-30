package com.moneycalculator.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String dbUrl = "jdbc:sqlite:currencies.db";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS currencies (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE NOT NULL,symbol TEXT NOT NULL,rate REAL NOT NULL,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOrUpdateCurrency(Currency currency) {
        String sql = "INSERT INTO currencies (name, symbol, rate) VALUES (?, ?, ?) ON CONFLICT(name) DO UPDATE SET symbol = excluded.symbol,rate = excluded.rate,updated_at = CURRENT_TIMESTAMP";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, currency.getName());
            pstmt.setString(2, currency.getSymbol());
            pstmt.setDouble(3, currency.getRate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT name, symbol, rate FROM currencies ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Currency currency = new Currency(
                        rs.getString("name"),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                );
                currencies.add(currency);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencies;
    }

    public Currency getCurrencyByName(String name) {
        String sql = "SELECT name, symbol, rate FROM currencies WHERE name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Currency(
                        rs.getString("name"),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}