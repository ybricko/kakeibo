package com.example.xpense_tracker.data;

public enum Currency {
    IDR(1.0, "Rp"), // Indonesian Rupiah
    USD(0.0028, "$"), // USD
    EUR(0.0026, "€"), // Euro
    HUF(1.0, "Ft"); // Hungarian Forint

    private final double changingNum;
    private final String currencySymbol;

    Currency(double changingNum, String currencySymbol) {
        this.changingNum = changingNum;
        this.currencySymbol = currencySymbol;
    }

    public double getChangingNum() {
        return changingNum;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }
}
