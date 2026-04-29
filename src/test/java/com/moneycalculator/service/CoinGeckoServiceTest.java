package com.moneycalculator.service;

import com.moneycalculator.model.DatabaseManager;
import org.junit.jupiter.api.*;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoinGeckoServiceTest {

    private CoinGeckoService service;

    @BeforeEach
    void setUp() {
        service = new CoinGeckoService();
    }

    @Test
    @Order(1)
    void testUpdateCurrencies() throws IOException {
        // This test makes a real API call
        service.updateCurrencies();

        DatabaseManager dbManager = DatabaseManager.getInstance();
        var currencies = dbManager.getAllCurrencies();

        assertThat(currencies).isNotEmpty();
        assertThat(currencies.size()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    void testCurrencyHasValidRate() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        var currencies = dbManager.getAllCurrencies();

        assertThat(currencies).allMatch(c -> c.getRate() >= 0);
    }

    @Test
    @Order(3)
    void testCurrencyHasSymbol() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        var currencies = dbManager.getAllCurrencies();

        assertThat(currencies).allMatch(c -> c.getSymbol() != null && !c.getSymbol().isEmpty());
    }

    @Test
    @Order(4)
    void testCurrencyHasName() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        var currencies = dbManager.getAllCurrencies();

        assertThat(currencies).allMatch(c -> c.getName() != null && !c.getName().isEmpty());
    }
}