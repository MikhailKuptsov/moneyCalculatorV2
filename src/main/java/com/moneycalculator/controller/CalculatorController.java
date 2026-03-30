package com.moneycalculator.controller;

import com.moneycalculator.model.*;
import com.moneycalculator.service.CoinGeckoService;
import com.moneycalculator.view.CalculatorView;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CalculatorController {
    private CalculatorView view;
    private DatabaseManager dbManager;
    private FavoriteCurrencies favorites;
    private CoinGeckoService coinGeckoService;
    private List<Currency> allCurrencies;

    public CalculatorController(CalculatorView view) {
        this.view = view;
        this.dbManager = DatabaseManager.getInstance();
        this.favorites = FavoriteCurrencies.getInstance();
        this.coinGeckoService = new CoinGeckoService();

        loadCurrencies();
        setupEventHandlers();
    }

    private void loadCurrencies() {
        allCurrencies = dbManager.getAllCurrencies();
        List<String> currencyNames = allCurrencies.stream()
                .map(Currency::getName)
                .collect(Collectors.toList());

        if (currencyNames.isEmpty()) {
            view.showInfo("База данных пуста. Нажмите 'Обновить значения валют' для загрузки данных.");
        } else {
            view.updateCurrencyList(currencyNames);
        }
    }

    private void setupEventHandlers() {
        // Update button
        view.getUpdateButton().addActionListener(e -> updateCurrencies());

        // Calculate button
        view.getCalculateButton().addActionListener(e -> calculate());

        // Clear button
        view.getClearButton().addActionListener(e -> view.getResultField().setText(""));

        // Search functionality
        setupSearchField(view.getCurrency1SearchField(), view.getCurrency1Combo());
        setupSearchField(view.getCurrency2SearchField(), view.getCurrency2Combo());
        setupSearchField(view.getResultCurrencySearchField(), view.getResultCurrencyCombo());

        // Add to favorites when currency is selected
        view.getCurrency1Combo().addActionListener(e -> addToFavorites());
        view.getCurrency2Combo().addActionListener(e -> addToFavorites());
        view.getResultCurrencyCombo().addActionListener(e -> addToFavorites());
    }

    private void setupSearchField(JTextField searchField, JComboBox<String> comboBox) {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterCurrencies(comboBox, searchField); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterCurrencies(comboBox, searchField); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterCurrencies(comboBox, searchField); }
        });
    }

    private void filterCurrencies(JComboBox<String> comboBox, JTextField searchField) {
        List<String> currencyNames = allCurrencies.stream()
                .map(Currency::getName)
                .collect(Collectors.toList());

        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            view.filterCurrencies(comboBox, searchField, currencyNames, "");
        } else {
            view.filterCurrencies(comboBox, searchField, currencyNames, searchText);
        }
    }

    private void addToFavorites() {
        String selected1 = (String) view.getCurrency1Combo().getSelectedItem();
        String selected2 = (String) view.getCurrency2Combo().getSelectedItem();
        String selectedResult = (String) view.getResultCurrencyCombo().getSelectedItem();

        if (selected1 != null) favorites.addFavorite(selected1);
        if (selected2 != null) favorites.addFavorite(selected2);
        if (selectedResult != null) favorites.addFavorite(selectedResult);
    }

    private void updateCurrencies() {
        view.getUpdateButton().setEnabled(false);
        view.getUpdateButton().setText("Обновление...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    coinGeckoService.updateCurrencies();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                loadCurrencies();
                view.getUpdateButton().setEnabled(true);
                view.getUpdateButton().setText("Обновить значения валют");
                view.showInfo("Курсы валют успешно обновлены!");
            }
        };
        worker.execute();
    }

    private void calculate() {
        try {
            String currency1Name = (String) view.getCurrency1Combo().getSelectedItem();
            String currency2Name = (String) view.getCurrency2Combo().getSelectedItem();
            String resultCurrencyName = (String) view.getResultCurrencyCombo().getSelectedItem();

            if (currency1Name == null || currency2Name == null || resultCurrencyName == null) {
                view.showError("Пожалуйста, выберите все валюты");
                return;
            }

            double amount1 = Double.parseDouble(view.getAmount1Field().getText());
            double amount2 = Double.parseDouble(view.getAmount2Field().getText());
            String operator = (String) view.getOperatorCombo().getSelectedItem();

            Currency currency1 = dbManager.getCurrencyByName(currency1Name);
            Currency currency2 = dbManager.getCurrencyByName(currency2Name);
            Currency resultCurrency = dbManager.getCurrencyByName(resultCurrencyName);

            if (currency1 == null || currency2 == null || resultCurrency == null) {
                view.showError("Не удалось найти курсы валют в базе данных");
                return;
            }

            // Convert to USD first (base currency)
            double amount1InUSD = amount1 / currency1.getRate();
            double amount2InUSD = amount2 / currency2.getRate();

            double resultInUSD;
            switch (operator) {
                case "+":
                    resultInUSD = amount1InUSD + amount2InUSD;
                    break;
                case "-":
                    resultInUSD = amount1InUSD - amount2InUSD;
                    break;
                case "*":
                    resultInUSD = amount1InUSD * amount2InUSD;
                    break;
                case "/":
                    if (amount2InUSD == 0) {
                        view.showError("Деление на ноль!");
                        return;
                    }
                    resultInUSD = amount1InUSD / amount2InUSD;
                    break;
                default:
                    view.showError("Неизвестная операция");
                    return;
            }

            double finalResult = resultInUSD * resultCurrency.getRate();
            view.getResultField().setText(String.format("%.4f %s", finalResult, resultCurrency.getSymbol()));

        } catch (NumberFormatException e) {
            view.showError("Пожалуйста, введите корректные числовые значения");
        } catch (Exception e) {
            view.showError("Ошибка при вычислении: " + e.getMessage());
        }
    }
}