package com.sosinhvien.app.data.model;

public class Transaction {
    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";
    public static final String SOURCE_MANUAL = "Thủ công";
    public static final String SOURCE_OCR = "OCR";
    public static final String SOURCE_ASSISTANT = "Trợ lý";

    private final String id;
    private final String name;
    private final String categoryId;
    private final long amount;
    private final String type;
    private final String source;
    private final String timeLabel;

    public Transaction(String id, String name, String categoryId, long amount,
                       String type, String source, String timeLabel) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.timeLabel = timeLabel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public long getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public boolean isExpense() {
        return TYPE_EXPENSE.equals(type);
    }
}
