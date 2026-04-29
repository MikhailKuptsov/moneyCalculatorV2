package com.moneycalculator.controller;

import com.moneycalculator.model.Currency;
import com.moneycalculator.model.DatabaseManager;
import com.moneycalculator.model.FavoriteCurrencies;
import com.moneycalculator.service.CoinGeckoService;
import com.moneycalculator.view.CalculatorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.swing.*;
import javax.swing.text.Document;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculatorControllerTest {

    @Mock private CalculatorView view;
    @Mock private JComboBox<String> combo1, combo2, comboResult, comboOperator;
    @Mock private JTextField amount1Field, amount2Field, resultField;
    @Mock private JTextField searchField1, searchField2, searchFieldResult;
    @Mock private Document doc1, doc2, doc3;

    @Mock private DatabaseManager dbManager;
    @Mock private FavoriteCurrencies favorites;

    private CalculatorController controller;

    @BeforeEach
    void setUp() {
        // Мокируем компоненты View для предотвращения NPE при setupEventHandlers()
        when(view.getCurrency1Combo()).thenReturn(combo1);
        when(view.getCurrency2Combo()).thenReturn(combo2);
        when(view.getResultCurrencyCombo()).thenReturn(comboResult);
        when(view.getOperatorCombo()).thenReturn(comboOperator);
        when(view.getAmount1Field()).thenReturn(amount1Field);
        when(view.getAmount2Field()).thenReturn(amount2Field);
        when(view.getResultField()).thenReturn(resultField);

        when(view.getCurrency1SearchField()).thenReturn(searchField1);
        when(view.getCurrency2SearchField()).thenReturn(searchField2);
        when(view.getResultCurrencySearchField()).thenReturn(searchFieldResult);
        when(searchField1.getDocument()).thenReturn(doc1);
        when(searchField2.getDocument()).thenReturn(doc2);
        when(searchFieldResult.getDocument()).thenReturn(doc3);

        // Глушим добавление слушателей, чтобы не вызывать реальную Swing-логику
        doNothing().when(combo1).addActionListener(any());
        doNothing().when(combo2).addActionListener(any());
        doNothing().when(comboResult).addActionListener(any());
        doNothing().when(view.getUpdateButton()).addActionListener(any());
        doNothing().when(view.getCalculateButton()).addActionListener(any());
        doNothing().when(view.getClearButton()).addActionListener(any());
        doNothing().when(doc1).addDocumentListener(any());
        doNothing().when(doc2).addDocumentListener(any());
        doNothing().when(doc3).addDocumentListener(any());
    }

    /**
     * Инициализация контроллера с перехватом синглтонов и new CoinGeckoService()
     */
    private CalculatorController initController() {
        try (MockedStatic<DatabaseManager> dbStatic = mockStatic(DatabaseManager.class);
             MockedStatic<FavoriteCurrencies> favStatic = mockStatic(FavoriteCurrencies.class);
             MockedConstruction<CoinGeckoService> serviceCons = mockConstruction(CoinGeckoService.class)) {

            dbStatic.when(DatabaseManager::getInstance).thenReturn(dbManager);
            favStatic.when(FavoriteCurrencies::getInstance).thenReturn(favorites);

            return new CalculatorController(view);
        }
    }

    /** Вспомогательный метод для вызова приватных методов через рефлексию */
    private void invokePrivateMethod(String methodName) throws Exception {
        Method method = CalculatorController.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(controller);
    }

    // ================= ТЕСТЫ =================

    @Test
    void loadCurrencies_ShouldShowInfo_WhenDatabaseIsEmpty() throws Exception {
        when(dbManager.getAllCurrencies()).thenReturn(List.of());
        controller = initController();

        invokePrivateMethod("loadCurrencies");

        verify(view).showInfo("База данных пуста. Нажмите 'Обновить значения валют' для загрузки данных. ");
        verify(view, never()).updateCurrencyList(any());
    }

    @Test
    void loadCurrencies_ShouldUpdateList_WhenDatabaseHasData() throws Exception {
        Currency usd = mock(Currency.class);
        when(usd.getName()).thenReturn("US Dollar");
        Currency eur = mock(Currency.class);
        when(eur.getName()).thenReturn("Euro");

        when(dbManager.getAllCurrencies()).thenReturn(Arrays.asList(usd, eur));
        controller = initController();

        invokePrivateMethod("loadCurrencies");

        verify(view).updateCurrencyList(Arrays.asList("US Dollar", "Euro"));
        verify(view, never()).showInfo(any());
    }

    @Test
    void calculate_ShouldComputeCorrectAddition() throws Exception {
        controller = initController();

        // Настройка выбранных значений
        when(combo1.getSelectedItem()).thenReturn("US Dollar");
        when(combo2.getSelectedItem()).thenReturn("Euro");
        when(comboResult.getSelectedItem()).thenReturn("British Pound");
        when(comboOperator.getSelectedItem()).thenReturn("+");

        // Настройка полей ввода
        when(amount1Field.getText()).thenReturn("100.0");
        when(amount2Field.getText()).thenReturn("50.0");

        // Моки валют
        Currency usd = mock(Currency.class);
        when(usd.getRate()).thenReturn(1.0);
        when(usd.getSymbol()).thenReturn("$");

        Currency eur = mock(Currency.class);
        when(eur.getRate()).thenReturn(0.85);
        when(eur.getSymbol()).thenReturn("€");

        Currency gbp = mock(Currency.class);
        when(gbp.getRate()).thenReturn(0.75);
        when(gbp.getSymbol()).thenReturn("£");

        when(dbManager.getCurrencyByName("US Dollar")).thenReturn(usd);
        when(dbManager.getCurrencyByName("Euro")).thenReturn(eur);
        when(dbManager.getCurrencyByName("British Pound")).thenReturn(gbp);

        invokePrivateMethod("calculate");

        // Ожидаемый результат: (100/1.0 + 50/0.85) * 0.75
        double expected = (100.0 + 50.0 / 0.85) * 0.75;
        verify(resultField).setText(String.format("%.10f %s", expected, "£"));
    }

    @Test
    void calculate_ShouldShowError_OnDivisionByZero() throws Exception {
        controller = initController();
        when(combo1.getSelectedItem()).thenReturn("A");
        when(combo2.getSelectedItem()).thenReturn("B");
        when(comboResult.getSelectedItem()).thenReturn("C");
        when(comboOperator.getSelectedItem()).thenReturn("/");
        when(amount1Field.getText()).thenReturn("100.0");
        when(amount2Field.getText()).thenReturn("0.0");

        Currency c1 = mock(Currency.class); when(c1.getRate()).thenReturn(1.0);
        Currency c2 = mock(Currency.class); when(c2.getRate()).thenReturn(1.0); // amount2InUSD = 0
        Currency c3 = mock(Currency.class); when(c3.getRate()).thenReturn(1.0);

        when(dbManager.getCurrencyByName("A")).thenReturn(c1);
        when(dbManager.getCurrencyByName("B")).thenReturn(c2);
        when(dbManager.getCurrencyByName("C")).thenReturn(c3);

        invokePrivateMethod("calculate");

        verify(view).showError("Деление на ноль! ");
        verify(resultField, never()).setText(any());
    }

    @Test
    void calculate_ShouldShowError_OnInvalidNumberFormat() throws Exception {
        controller = initController();
        when(combo1.getSelectedItem()).thenReturn("USD");
        when(combo2.getSelectedItem()).thenReturn("EUR");
        when(comboResult.getSelectedItem()).thenReturn("GBP");
        when(amount1Field.getText()).thenReturn("not_a_number");
        when(amount2Field.getText()).thenReturn("100.0");

        invokePrivateMethod("calculate");

        verify(view).showError("Пожалуйста, введите корректные числовые значения");
    }

    @Test
    void calculate_ShouldShowError_OnNullCurrencySelection() throws Exception {
        controller = initController();
        when(combo1.getSelectedItem()).thenReturn(null); // Пропущен выбор
        when(combo2.getSelectedItem()).thenReturn("EUR");
        when(comboResult.getSelectedItem()).thenReturn("GBP");

        invokePrivateMethod("calculate");

        verify(view).showError("Пожалуйста, выберите все валюты ");
    }
}