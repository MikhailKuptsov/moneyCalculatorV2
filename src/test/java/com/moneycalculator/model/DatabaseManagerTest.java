package com.moneycalculator.model;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseManagerTest {

    private static DatabaseManager dbManager;

    @BeforeAll
    static void setUp() {
        dbManager = DatabaseManager.getInstance();
        dbManager.initializeDatabase();
    }

    @Test
    @Order(1)
    void testInsertCurrency() {
        Currency currency = new Currency("TestCoin", "TST", 100.0);
        dbManager.insertOrUpdateCurrency(currency);

        Currency retrieved = dbManager.getCurrencyByName("TestCoin");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("TestCoin");
        assertThat(retrieved.getSymbol()).isEqualTo("TST");
        assertThat(retrieved.getRate()).isEqualTo(100.0);
    }

    @Test
    @Order(2)
    void testUpdateCurrency() {
        Currency currency = new Currency("TestCoin", "TST", 200.0);
        dbManager.insertOrUpdateCurrency(currency);

        Currency retrieved = dbManager.getCurrencyByName("TestCoin");
        assertThat(retrieved.getRate()).isEqualTo(200.0);
    }

    @Test
    @Order(3)
    void testGetAllCurrencies() {
        List<Currency> currencies = dbManager.getAllCurrencies();

        assertThat(currencies).isNotEmpty();
        assertThat(currencies.stream().anyMatch(c -> c.getName().equals("TestCoin"))).isTrue();
    }

    @Test
    @Order(4)
    void testGetCurrencyByNameNotFound() {
        Currency currency = dbManager.getCurrencyByName("NonExistentCurrency12345");
        assertThat(currency).isNull();
    }

    @Test
    @Order(5)
    void testInsertMultipleCurrencies() {
        dbManager.insertOrUpdateCurrency(new Currency("CoinA", "CA", 10.0));
        dbManager.insertOrUpdateCurrency(new Currency("CoinB", "CB", 20.0));
        dbManager.insertOrUpdateCurrency(new Currency("CoinC", "CC", 30.0));

        List<Currency> currencies = dbManager.getAllCurrencies();
        assertThat(currencies.stream().filter(c -> c.getName().startsWith("Coin")).count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @Order(6)
    void testCurrencyWithSpecialCharacters() {
        Currency currency = new Currency("Test-Coin_123", "TST", 150.0);
        dbManager.insertOrUpdateCurrency(currency);

        Currency retrieved = dbManager.getCurrencyByName("Test-Coin_123");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSymbol()).isEqualTo("TST");
    }
}