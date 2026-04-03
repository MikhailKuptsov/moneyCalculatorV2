package com.moneycalculator.model;

public class Currency {
    private String name;
    private String symbol;
    private double rate;

    public Currency(String name, String symbol, double rate) {
        this.name = name;
        this.symbol = symbol;
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}