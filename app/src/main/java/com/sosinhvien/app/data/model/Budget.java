package com.sosinhvien.app.data.model;

public class Budget {
    private final String monthLabel;
    private final long totalBudget;
    private final long openingBalance;
    private final long spent;

    public Budget(String monthLabel, long totalBudget, long openingBalance, long spent) {
        this.monthLabel = monthLabel;
        this.totalBudget = totalBudget;
        this.openingBalance = openingBalance;
        this.spent = spent;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public long getTotalBudget() {
        return totalBudget;
    }

    public long getOpeningBalance() {
        return openingBalance;
    }

    public long getSpent() {
        return spent;
    }

    public long getRemaining() {
        return totalBudget - spent;
    }

    public int getUsagePercent() {
        if (totalBudget <= 0) return 0;
        return (int) Math.min(100, (spent * 100) / totalBudget);
    }

    public long getAvailableBalance() {
        return openingBalance - spent;
    }
}
