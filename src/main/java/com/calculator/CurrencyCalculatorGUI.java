package com.calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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

        calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(new CalculateListener());

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

        setSize(1100, 150);
        setLocationRelativeTo(null);
    }

    private void loadCurrencies() {
        List<Currency> currencies = database.getAllCurrencies();
        for (Currency currency : currencies) {
            currencyFromCombo.addItem(currency);
            currencyToCombo.addItem(currency);
            resultCurrencyCombo.addItem(currency);
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