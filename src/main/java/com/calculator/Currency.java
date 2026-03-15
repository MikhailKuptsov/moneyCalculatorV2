package com.calculator;

public class Currency {
    private int id;
    private String name;
    private String symbol;
    private double rate;

    public Currency(int id, String name, String symbol, double rate) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.rate = rate;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}