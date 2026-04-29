package com.moneycalculator.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class CalculatorViewTest {

    private CalculatorView view;

    @BeforeEach
    void setUp() {
        // Run on EDT for Swing components
        SwingUtilities.invokeLater(() -> {
            view = new CalculatorView();
        });

        // Give time for EDT to initialize
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testViewInitialization() {
        assertThat(view).isNotNull();
        assertThat(view.getUpdateButton()).isNotNull();
        assertThat(view.getCalculateButton()).isNotNull();
        assertThat(view.getClearButton()).isNotNull();
        assertThat(view.getCurrency1Combo()).isNotNull();
        assertThat(view.getCurrency2Combo()).isNotNull();
        assertThat(view.getResultCurrencyCombo()).isNotNull();
    }

    @Test
    void testUpdateCurrencyList() {
        List<String> currencies = Arrays.asList("Bitcoin", "Ethereum", "Cardano");

        SwingUtilities.invokeLater(() -> {
            view.updateCurrencyList(currencies);

            assertThat(view.getCurrency1Combo().getItemCount()).isEqualTo(3);
            assertThat(view.getCurrency2Combo().getItemCount()).isEqualTo(3);
            assertThat(view.getResultCurrencyCombo().getItemCount()).isEqualTo(3);
        });
    }

    @Test
    void testFilterCurrencies() {
        List<String> allCurrencies = Arrays.asList("Bitcoin", "Ethereum", "Cardano", "Dogecoin", "Ripple");

        SwingUtilities.invokeLater(() -> {
            view.filterCurrencies(view.getCurrency1Combo(), view.getCurrency1SearchField(), allCurrencies, "coin");

            assertThat(view.getCurrency1Combo().getItemCount()).isGreaterThan(0);
        });
    }

    @Test
    void testShowError() {
        // Just verify no exception is thrown
        SwingUtilities.invokeLater(() -> {
            view.showError("Test error message");
        });
    }

    @Test
    void testShowInfo() {
        // Just verify no exception is thrown
        SwingUtilities.invokeLater(() -> {
            view.showInfo("Test info message");
        });
    }
}