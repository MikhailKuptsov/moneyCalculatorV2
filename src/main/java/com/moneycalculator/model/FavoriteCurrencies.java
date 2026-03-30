package com.moneycalculator.model;

import java.util.*;
import java.util.stream.Collectors;

public class FavoriteCurrencies {
    private static FavoriteCurrencies instance;
    private Set<String> favorites;

    private FavoriteCurrencies() {
        favorites = new LinkedHashSet<>();
    }

    public static synchronized FavoriteCurrencies getInstance() {
        if (instance == null) {
            instance = new FavoriteCurrencies();
        }
        return instance;
    }

    public void addFavorite(String currencyName) {
        favorites.add(currencyName);
    }

    public List<String> getFavorites() {
        return new ArrayList<>(favorites);
    }

    public void clear() {
        favorites.clear();
    }

    public List<String> searchCurrencies(List<Currency> allCurrencies, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Return favorites first, then all currencies
            List<String> result = new ArrayList<>(favorites);
            for (Currency currency : allCurrencies) {
                if (!favorites.contains(currency.getName())) {
                    result.add(currency.getName());
                }
            }
            return result;
        }

        String searchLower = searchText.toLowerCase().trim();
        return allCurrencies.stream()
                .map(Currency::getName)
                .filter(name -> name.toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
    }
}