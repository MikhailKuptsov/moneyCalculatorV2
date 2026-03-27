package com.calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CurrencyCalculatorGUI extends JFrame {
    private Database database;

    private JComboBox<Currency> currencyFromCombo;
    private JTextField amountFromField;
    private JComboBox<String> operationCombo;
    private JComboBox<Currency> currencyToCombo;
    private JTextField amountToField;
    private JComboBox<Currency> resultCurrencyCombo;
    private JTextField resultField;
    private JButton calculateButton;
    private JButton resetCacheButton;
    private boolean hasPopularEntry;

    public CurrencyCalculatorGUI() {
        database = new Database();
        initUI();
        loadCurrencies();
    }

    private void initUI() {
        setTitle("Денежный калькулятор");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Создание компонентов
        currencyFromCombo = new JComboBox<>();
        amountFromField = new JTextField(10);
        operationCombo = new JComboBox<>(new String[]{"+", "-", "*", "/"});
        currencyToCombo = new JComboBox<>();
        amountToField = new JTextField(10);
        JLabel equalsLabel = new JLabel("=", SwingConstants.CENTER);
        resultCurrencyCombo = new JComboBox<>();
        resultField = new JTextField(10);
        resultField.setEditable(false);
        resultField.setBackground(Color.WHITE);

        PopularCurrencyRenderer renderer = new PopularCurrencyRenderer();
        currencyFromCombo.setRenderer(renderer);
        currencyToCombo.setRenderer(renderer);
        resultCurrencyCombo.setRenderer(renderer);

        calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(new CalculateListener());

        resetCacheButton = new JButton("Сброс кэша");
        resetCacheButton.addActionListener(e -> {
            database.clearCurrencyUsage();
            refreshCurrenciesPreserveSelection();
        });

        // Добавление компонентов на форму
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Валюта 1:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        add(currencyFromCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        add(amountFromField, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        add(operationCombo, gbc);

        gbc.gridx = 4; gbc.gridy = 0;
        add(new JLabel("Валюта 2:"), gbc);

        gbc.gridx = 5; gbc.gridy = 0;
        add(currencyToCombo, gbc);

        gbc.gridx = 6; gbc.gridy = 0;
        add(amountToField, gbc);

        gbc.gridx = 7; gbc.gridy = 0;
        add(equalsLabel, gbc);

        gbc.gridx = 8; gbc.gridy = 0;
        add(new JLabel("Валюта рез:"), gbc);

        gbc.gridx = 9; gbc.gridy = 0;
        add(resultCurrencyCombo, gbc);

        gbc.gridx = 10; gbc.gridy = 0;
        add(resultField, gbc);

        gbc.gridx = 5; gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(calculateButton, gbc);

        gbc.gridx = 7; gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(resetCacheButton, gbc);

        setSize(1100, 150);
        setLocationRelativeTo(null);
    }

    private void loadCurrencies() {
        loadCurrencies(null, null, null);
    }

    private void loadCurrencies(Integer fromId, Integer toId, Integer resultId) {
        Currency preferredCurrency = database.getPreferredCurrency();
        List<Currency> currencies = database.getCurrenciesForSelection();

        currencyFromCombo.removeAllItems();
        currencyToCombo.removeAllItems();
        resultCurrencyCombo.removeAllItems();

        hasPopularEntry = preferredCurrency != null;
        if (preferredCurrency != null) {
            currencyFromCombo.addItem(preferredCurrency);
            currencyToCombo.addItem(preferredCurrency);
            resultCurrencyCombo.addItem(preferredCurrency);
        }

        for (Currency currency : currencies) {
            currencyFromCombo.addItem(currency);
            currencyToCombo.addItem(currency);
            resultCurrencyCombo.addItem(currency);
        }

        selectCurrencyById(currencyFromCombo, fromId);
        selectCurrencyById(currencyToCombo, toId);
        selectCurrencyById(resultCurrencyCombo, resultId);
    }

    private void refreshCurrenciesPreserveSelection() {
        Integer fromId = getSelectedCurrencyId(currencyFromCombo);
        Integer toId = getSelectedCurrencyId(currencyToCombo);
        Integer resultId = getSelectedCurrencyId(resultCurrencyCombo);
        loadCurrencies(fromId, toId, resultId);
    }

    private Integer getSelectedCurrencyId(JComboBox<Currency> combo) {
        Currency currency = (Currency) combo.getSelectedItem();
        return currency == null ? null : currency.getId();
    }

    private void selectCurrencyById(JComboBox<Currency> combo, Integer currencyId) {
        if (currencyId == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Currency currency = combo.getItemAt(i);
            if (currency != null && currency.getId() == currencyId) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private class PopularCurrencyRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == 1 && hasPopularEntry) {
                label.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
            } else {
                label.setBorder(null);
            }
            return label;
        }
    }

    private class CalculateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Получаем выбранные валюты
                Currency currencyFrom = (Currency) currencyFromCombo.getSelectedItem();
                Currency currencyTo = (Currency) currencyToCombo.getSelectedItem();
                Currency resultCurrency = (Currency) resultCurrencyCombo.getSelectedItem();

                // Получаем значения
                double amount1 = Double.parseDouble(amountFromField.getText());
                double amount2 = Double.parseDouble(amountToField.getText());
                String operation = (String) operationCombo.getSelectedItem();

                // Конвертируем все в базовую валюту (рубли)
                double amount1InRub = amount1 * currencyFrom.getRate();
                double amount2InRub = amount2 * currencyTo.getRate();

                // Выполняем операцию
                double resultInRub = 0;
                switch (operation) {
                    case "+":
                        resultInRub = amount1InRub + amount2InRub;
                        break;
                    case "-":
                        resultInRub = amount1InRub - amount2InRub;
                        break;
                    case "*":
                        resultInRub = amount1InRub * amount2InRub;
                        break;
                    case "/":
                        if (amount2InRub == 0) {
                            JOptionPane.showMessageDialog(CurrencyCalculatorGUI.this,
                                    "Деление на ноль невозможно!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        resultInRub = amount1InRub / amount2InRub;
                        break;
                }

                // Конвертируем результат в выбранную валюту
                double result = resultInRub / resultCurrency.getRate();

                // Форматируем результат
                resultField.setText(String.format("%.2f %s", result, resultCurrency.getSymbol()));

                Set<Integer> usedCurrencyIds = new HashSet<>();
                if (currencyFrom != null) {
                    usedCurrencyIds.add(currencyFrom.getId());
                }
                if (currencyTo != null) {
                    usedCurrencyIds.add(currencyTo.getId());
                }
                if (resultCurrency != null) {
                    usedCurrencyIds.add(resultCurrency.getId());
                }
                for (Integer currencyId : usedCurrencyIds) {
                    database.recordCurrencyUsage(currencyId);
                }

                refreshCurrenciesPreserveSelection();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CurrencyCalculatorGUI.this,
                        "Пожалуйста, введите корректные числа!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(CurrencyCalculatorGUI.this,
                        "Произошла ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
