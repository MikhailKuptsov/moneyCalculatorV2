package com.calculator;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Устанавливаем Look and Feel для лучшего внешнего вида
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Запускаем приложение в потоке обработки событий
        SwingUtilities.invokeLater(() -> {
            new CurrencyCalculatorGUI().setVisible(true);
        });
    }
}