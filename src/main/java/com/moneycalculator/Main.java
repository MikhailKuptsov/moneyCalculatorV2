package com.moneycalculator;

import com.moneycalculator.controller.CalculatorController;
import com.moneycalculator.model.DatabaseManager;
import com.moneycalculator.view.CalculatorView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database
                DatabaseManager dbManager = DatabaseManager.getInstance();
                dbManager.initializeDatabase();

                // Create view and controller
                CalculatorView view = new CalculatorView();
                CalculatorController controller = new CalculatorController(view);

                // Make the view visible
                view.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}