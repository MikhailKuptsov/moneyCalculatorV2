package com.moneycalculator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class FavoriteCurrenciesTest {

    private FavoriteCurrencies favorites;
    private List<Currency> allCurrencies;

    @BeforeEach
    void setUp() {
        // Reset singleton for testing
        favorites = FavoriteCurrencies.getInstance();
        favorites.clear();

        allCurrencies = Arrays.asList(
                new Currency("Bitcoin", "BTC", 50000),
                new Currency("Ethereum", "ETH", 3000),
                new Currency("Cardano", "ADA", 1.2),
                new Currency("Dogecoin", "DOGE", 0.1),
                new Currency("Ripple", "XRP", 0.5)
        );
    }

    @Test
    void testAddFavorite() {
        favorites.addFavorite("Bitcoin");
        favorites.addFavorite("Ethereum");

        List<String> favs = favorites.getFavorites();
        assertThat(favs).containsExactly("Bitcoin", "Ethereum");
    }

    @Test
    void testAddDuplicateFavorite() {
        favorites.addFavorite("Bitcoin");
        favorites.addFavorite("Bitcoin");
        favorites.addFavorite("Bitcoin");

        assertThat(favorites.getFavorites()).hasSize(1);
    }

    @Test
    void testClearFavorites() {
        favorites.addFavorite("Bitcoin");
        favorites.addFavorite("Ethereum");
        assertThat(favorites.getFavorites()).hasSize(2);

        favorites.clear();
        assertThat(favorites.getFavorites()).isEmpty();
    }

    @Test
    void testSearchCurrenciesWithNoSearchText() {
        favorites.addFavorite("Bitcoin");
        favorites.addFavorite("Ethereum");

        List<String> results = favorites.searchCurrencies(allCurrencies, "");

        assertThat(results).containsExactly("Bitcoin", "Ethereum", "Cardano", "Dogecoin", "Ripple");
    }

    @Test
    void testSearchCurrenciesWithSearchText() {
        List<String> results = favorites.searchCurrencies(allCurrencies, "coin");

        assertThat(results).containsExactly("Bitcoin", "Dogecoin");
    }

    @Test
    void testSearchCurrenciesCaseInsensitive() {
        List<String> results = favorites.searchCurrencies(allCurrencies, "BITCOIN");

        assertThat(results).containsExactly("Bitcoin");
    }

    @Test
    void testSearchCurrenciesWithPartialMatch() {
        List<String> results = favorites.searchCurrencies(allCurrencies, "ada");

        assertThat(results).containsExactly("Cardano");
    }

    @Test
    void testSearchCurrenciesNoMatch() {
        List<String> results = favorites.searchCurrencies(allCurrencies, "xyzabc");

        assertThat(results).isEmpty();
    }

    @Test
    void testFavoriteOrdering() {
        favorites.addFavorite("Ripple");
        favorites.addFavorite("Bitcoin");
        favorites.addFavorite("Cardano");

        List<String> results = favorites.searchCurrencies(allCurrencies, "");

        assertThat(results.subList(0, 3)).containsExactly("Ripple", "Bitcoin", "Cardano");
    }
}