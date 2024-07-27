package com.kura.budgettracker;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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

        List<Transaction> transactions = transactionDAO.getAllTransactionsForCurrentMonth();
        adapter = new TransactionAdapter(transactions, new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Toast.makeText(ViewTransaction.this, "Edit Clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(int position) {
                transactionDAO.deleteTransaction(transactions.get(position).getId());
                transactions.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(ViewTransaction.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if(direction == ItemTouchHelper.LEFT){
                    View itemView = viewHolder.itemView;
                    itemView.findViewById(R.id.btnEdit).setVisibility(View.VISIBLE);
                    itemView.findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onDestroy() {
        transactionDAO.close();
        super.onDestroy();
    }
}