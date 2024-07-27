package com.kura.budgettracker;


import java.io.Serializable;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String title;
    private int amount;
    private String date;
    private TransactionType type;

    public Transaction(int id, String title, int amount, String date, TransactionType type){
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public Transaction (String title, int amount, String date, TransactionType type) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.type = type;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
