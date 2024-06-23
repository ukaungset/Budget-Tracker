package com.kura.budgettracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewTransaction extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private TransactionDAO transactionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionDAO = new TransactionDAO(this);
        transactionDAO.open();

        List<Transaction> transactions = transactionDAO.getAllTransactions();
        adapter = new TransactionAdapter(transactions);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        transactionDAO.close();
        super.onDestroy();
    }
}