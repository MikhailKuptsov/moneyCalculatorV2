package com.moneycalculator.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class CalculatorView extends JFrame {
    private JButton updateButton;
    private JComboBox<String> currency1Combo;
    private JTextField amount1Field;
    private JComboBox<String> operatorCombo;
    private JComboBox<String> currency2Combo;
    private JTextField amount2Field;
    private JLabel equalsLabel;
    private JComboBox<String> resultCurrencyCombo;
    private JTextField resultField;
    private JButton calculateButton;
    private JButton clearButton;

    private JTextField currency1SearchField;
    private JTextField currency2SearchField;
    private JTextField resultCurrencySearchField;

    public CalculatorView() {
        initUI();
    }

    private void initUI() {
        setTitle("Money Calculator V3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 0: Update button
        updateButton = new JButton("Обновить значения валют");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        mainPanel.add(updateButton, gbc);

        // Row 1: Currency 1
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Валюта 1:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        currency1SearchField = new JTextField(15);
        currency1SearchField.setToolTipText("Поиск валюты...");
        mainPanel.add(currency1SearchField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        currency1Combo = new JComboBox<>();
        currency1Combo.setEditable(false);
        mainPanel.add(currency1Combo, gbc);

        // Row 2: Amount 1
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Сумма 1:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        amount1Field = new JTextField(20);
        mainPanel.add(amount1Field, gbc);

        // Row 3: Operator
        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Операция:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        operatorCombo = new JComboBox<>(new String[]{"+", "-", "*", "/"});
        mainPanel.add(operatorCombo, gbc);

        // Row 4: Currency 2
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Валюта 2:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        currency2SearchField = new JTextField(15);
        currency2SearchField.setToolTipText("Поиск валюты...");
        mainPanel.add(currency2SearchField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        currency2Combo = new JComboBox<>();
        currency2Combo.setEditable(false);
        mainPanel.add(currency2Combo, gbc);

        // Row 5: Amount 2
        gbc.gridy = 5;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Сумма 2:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        amount2Field = new JTextField(20);
        mainPanel.add(amount2Field, gbc);

        // Row 6: Equals
        gbc.gridy = 6;
        gbc.gridx = 0;
        equalsLabel = new JLabel("=", SwingConstants.CENTER);
        equalsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(equalsLabel, gbc);

        // Row 7: Result Currency
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Валюта 3:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        resultCurrencySearchField = new JTextField(15);
        resultCurrencySearchField.setToolTipText("Поиск валюты...");
        mainPanel.add(resultCurrencySearchField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        resultCurrencyCombo = new JComboBox<>();
        resultCurrencyCombo.setEditable(false);
        mainPanel.add(resultCurrencyCombo, gbc);

        // Row 8: Result
        gbc.gridy = 8;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Результат:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        resultField = new JTextField(20);
        resultField.setEditable(false);
        resultField.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(resultField, gbc);

        // Row 9: Buttons
        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        calculateButton = new JButton("Рассчитать");
        mainPanel.add(calculateButton, gbc);

        gbc.gridx = 2;
        clearButton = new JButton("Очистить результат");
        mainPanel.add(clearButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        setSize(600, 500);
        setLocationRelativeTo(null);
    }

    public void updateCurrencyList(List<String> currencies) {
        SwingUtilities.invokeLater(() -> {
            String selected1 = (String) currency1Combo.getSelectedItem();
            String selected2 = (String) currency2Combo.getSelectedItem();
            String selectedResult = (String) resultCurrencyCombo.getSelectedItem();

            currency1Combo.removeAllItems();
            currency2Combo.removeAllItems();
            resultCurrencyCombo.removeAllItems();

            for (String currency : currencies) {
                currency1Combo.addItem(currency);
                currency2Combo.addItem(currency);
                resultCurrencyCombo.addItem(currency);
            }

            if (selected1 != null && currencies.contains(selected1)) {
                currency1Combo.setSelectedItem(selected1);
            }
            if (selected2 != null && currencies.contains(selected2)) {
                currency2Combo.setSelectedItem(selected2);
            }
            if (selectedResult != null && currencies.contains(selectedResult)) {
                resultCurrencyCombo.setSelectedItem(selectedResult);
            }
        });
    }

    public void filterCurrencies(JComboBox<String> comboBox, JTextField searchField, List<String> allCurrencies, String searchText) {
        SwingUtilities.invokeLater(() -> {
            String currentSelected = (String) comboBox.getSelectedItem();
            comboBox.removeAllItems();

            List<String> filtered = filterCurrenciesByText(allCurrencies, searchText);
            for (String currency : filtered) {
                comboBox.addItem(currency);
            }

            if (currentSelected != null && filtered.contains(currentSelected)) {
                comboBox.setSelectedItem(currentSelected);
            } else if (filtered.size() > 0) {
                comboBox.setSelectedIndex(0);
            }
        });
    }

    private List<String> filterCurrenciesByText(List<String> currencies, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return currencies;
        }
        String searchLower = searchText.toLowerCase().trim();
        return currencies.stream()
                .filter(c -> c.toLowerCase().contains(searchLower))
                .limit(50)
                .collect(java.util.stream.Collectors.toList());
    }

    // Getters for UI components
    public JButton getUpdateButton() { return updateButton; }
    public JComboBox<String> getCurrency1Combo() { return currency1Combo; }
    public JTextField getAmount1Field() { return amount1Field; }
    public JComboBox<String> getOperatorCombo() { return operatorCombo; }
    public JComboBox<String> getCurrency2Combo() { return currency2Combo; }
    public JTextField getAmount2Field() { return amount2Field; }
    public JComboBox<String> getResultCurrencyCombo() { return resultCurrencyCombo; }
    public JTextField getResultField() { return resultField; }
    public JButton getCalculateButton() { return calculateButton; }
    public JButton getClearButton() { return clearButton; }
    public JTextField getCurrency1SearchField() { return currency1SearchField; }
    public JTextField getCurrency2SearchField() { return currency2SearchField; }
    public JTextField getResultCurrencySearchField() { return resultCurrencySearchField; }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}