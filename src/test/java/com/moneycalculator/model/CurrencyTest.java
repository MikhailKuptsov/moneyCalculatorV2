package com.moneycalculator.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyTest {

    private Currency currency;

    @BeforeEach
    void setUp() {
        currency = new Currency("Bitcoin", "BTC", 50000.0);
    }

    @Test
    void testCurrencyCreation() {
        assertThat(currency).isNotNull();
        assertThat(currency.getName()).isEqualTo("Bitcoin");
        assertThat(currency.getSymbol()).isEqualTo("BTC");
        assertThat(currency.getRate()).isEqualTo(50000.0);
    }

    @Test
    void testSetName() {
        currency.setName("Ethereum");
        assertThat(currency.getName()).isEqualTo("Ethereum");
    }

    @Test
    void testSetSymbol() {
        currency.setSymbol("ETH");
        assertThat(currency.getSymbol()).isEqualTo("ETH");
    }

    @Test
    void testSetRate() {
        currency.setRate(3000.0);
        assertThat(currency.getRate()).isEqualTo(3000.0);
    }

    @Test
    void testToString() {
        assertThat(currency.toString()).isEqualTo("Bitcoin (BTC)");
    }

    @Test
    void testNegativeRate() {
        currency.setRate(-100.0);
        assertThat(currency.getRate()).isEqualTo(-100.0);
    }

    @Test
    void testZeroRate() {
        currency.setRate(0.0);
        assertThat(currency.getRate()).isEqualTo(0.0);
    }
}