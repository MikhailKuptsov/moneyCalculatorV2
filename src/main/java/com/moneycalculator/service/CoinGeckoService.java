package com.moneycalculator.service;

import com.google.gson.*;
import com.moneycalculator.model.Currency;
import com.moneycalculator.model.DatabaseManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class CoinGeckoService {
    private final OkHttpClient client;
    private final Gson gson;
    private final DatabaseManager dbManager;

    public CoinGeckoService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.dbManager = DatabaseManager.getInstance();
    }

    public void updateCurrencies() throws IOException {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "MoneyCalculatorV3/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonData = response.body().string();
            JsonArray coins = JsonParser.parseString(jsonData).getAsJsonArray();

            for (JsonElement coinElement : coins) {
                JsonObject coin = coinElement.getAsJsonObject();
                String name = coin.get("name").getAsString();
                String symbol = coin.get("symbol").getAsString().toUpperCase();
                double currentPrice = coin.has("current_price") && !coin.get("current_price").isJsonNull()
                        ? coin.get("current_price").getAsDouble()
                        : 0.0;

                if (currentPrice > 0) {
                    Currency currency = new Currency(name, symbol, currentPrice);
                    dbManager.insertOrUpdateCurrency(currency);
                }
            }
        }
    }
}