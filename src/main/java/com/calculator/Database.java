package com.calculator;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Database {
    private static final String DB_FILENAME = "currency.db";
    private static final String DB_URL = buildDbUrl();
    private static final int EURO_CODEPOINT = 0x20AC;

    private static String buildDbUrl() {
        Path baseDir = locateProjectRoot();
        Path dbPath = baseDir.resolve(DB_FILENAME).toAbsolutePath();
        return "jdbc:sqlite:" + dbPath.toString();
    }

    private static Path locateProjectRoot() {
        Path start = getCodeSourcePath();
        if (start != null) {
            Path dir = Files.isRegularFile(start) ? start.getParent() : start;
            while (dir != null) {
                if (Files.exists(dir.resolve("pom.xml"))) {
                    return dir;
                }
                dir = dir.getParent();
            }
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    private static Path getCodeSourcePath() {
        try {
            return Paths.get(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

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

            String usageSql = "CREATE TABLE IF NOT EXISTS currency_usage (" +
                    "currency_id INTEGER PRIMARY KEY," +
                    "use_count INTEGER NOT NULL DEFAULT 0," +
                    "last_used INTEGER NOT NULL," +
                    "FOREIGN KEY(currency_id) REFERENCES currencies(id)" +
                    ");";
            stmt.execute(usageSql);

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

    public List<Currency> getCurrenciesForSelection() {
        List<Currency> currencies = getAllCurrencies();
        Map<Integer, UsageStats> usageStats = getCurrencyUsageStats();

        currencies.sort((a, b) -> {
            int popularCompare = Boolean.compare(isPopularCurrency(b), isPopularCurrency(a));
            if (popularCompare != 0) {
                return popularCompare;
            }

            long lastUsedA = usageStats.containsKey(a.getId()) ? usageStats.get(a.getId()).lastUsed : 0L;
            long lastUsedB = usageStats.containsKey(b.getId()) ? usageStats.get(b.getId()).lastUsed : 0L;
            if (lastUsedA != lastUsedB) {
                return Long.compare(lastUsedB, lastUsedA);
            }

            String nameA = a.getName() == null ? "" : a.getName();
            String nameB = b.getName() == null ? "" : b.getName();
            return nameA.compareToIgnoreCase(nameB);
        });

        return currencies;
    }

    public Currency getPreferredCurrency() {
        Currency mostUsed = getMostUsedCurrency();
        if (mostUsed != null) {
            return mostUsed;
        }

        Currency usd = getCurrencyBySymbol("$");
        if (usd != null) {
            return usd;
        }

        Currency eur = getCurrencyBySymbol(String.valueOf((char) EURO_CODEPOINT));
        if (eur != null) {
            return eur;
        }

        List<Currency> all = getAllCurrencies();
        return all.isEmpty() ? null : all.get(0);
    }

    public void recordCurrencyUsage(int currencyId) {
        Connection conn = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            long now = System.currentTimeMillis();

            updateStmt = conn.prepareStatement(
                    "UPDATE currency_usage SET use_count = use_count + 1, last_used = ? WHERE currency_id = ?"
            );
            updateStmt.setLong(1, now);
            updateStmt.setInt(2, currencyId);
            int updated = updateStmt.executeUpdate();

            if (updated == 0) {
                insertStmt = conn.prepareStatement(
                        "INSERT INTO currency_usage (currency_id, use_count, last_used) VALUES (?, 1, ?)"
                );
                insertStmt.setInt(1, currencyId);
                insertStmt.setLong(2, now);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error recording currency usage: " + e.getMessage());
        } finally {
            try { if (updateStmt != null) updateStmt.close(); } catch (SQLException e) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    public void clearCurrencyUsage() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM currency_usage");
        } catch (SQLException e) {
            System.err.println("Error clearing currency usage: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
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

    private Currency getCurrencyBySymbol(String symbol) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            pstmt = conn.prepareStatement("SELECT * FROM currencies WHERE symbol = ? LIMIT 1");
            pstmt.setString(1, symbol);
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
            System.err.println("Error getting currency by symbol: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }

        return null;
    }

    private Currency getMostUsedCurrency() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            pstmt = conn.prepareStatement(
                    "SELECT c.id, c.name, c.symbol, c.rate " +
                            "FROM currencies c " +
                            "JOIN currency_usage u ON c.id = u.currency_id " +
                            "ORDER BY u.use_count DESC, u.last_used DESC " +
                            "LIMIT 1"
            );
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
            System.err.println("Error getting most used currency: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }

        return null;
    }

    private Map<Integer, UsageStats> getCurrencyUsageStats() {
        Map<Integer, UsageStats> usageStats = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT currency_id, use_count, last_used FROM currency_usage");
            while (rs.next()) {
                usageStats.put(
                        rs.getInt("currency_id"),
                        new UsageStats(rs.getInt("use_count"), rs.getLong("last_used"))
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting currency usage: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }

        return usageStats;
    }

    private boolean isPopularCurrency(Currency currency) {
        if (currency == null) {
            return false;
        }
        String symbol = currency.getSymbol();
        if ("$".equals(symbol)) {
            return true;
        }
        return symbol != null && symbol.length() == 1 && symbol.charAt(0) == EURO_CODEPOINT;
    }

    private static class UsageStats {
        private final int useCount;
        private final long lastUsed;

        private UsageStats(int useCount, long lastUsed) {
            this.useCount = useCount;
            this.lastUsed = lastUsed;
        }
    }
}
