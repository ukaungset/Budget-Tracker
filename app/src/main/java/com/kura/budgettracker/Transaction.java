package com.kura.budgettracker;

public class Transaction {
    private int id;
    private String title;
    private double amount;
    private String date;
    private String type;

    public Transaction(int id, String title, double amount, String date, String type){
        this.setId(id);
        this.setTitle(title);
        this.setAmount(amount);
        this.setDate(date);
        this.setType(type);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
