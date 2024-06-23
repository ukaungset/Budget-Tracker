package com.kura.budgettracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>
{
    private List<Transaction> transactionList;

    public static class TransactionViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title, date, amount, type;

        public TransactionViewHolder(View itemView){
            super (itemView);
            title = itemView.findViewById(R.id.tvTransactionTitle);
            date = itemView.findViewById(R.id.tvTransactionDate);
            amount = itemView.findViewById(R.id.tvTransactionAmount);
            type = itemView.findViewById(R.id.tvTransactionType);

        }
    }

    public TransactionAdapter(List<Transaction> transactions)
    {
        this.transactionList = transactions;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction,parent,false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.title.setText(transaction.getTitle());
        holder.amount.setText(String.valueOf(transaction.getAmount()));
        holder.date.setText(transaction.getDate());
        holder.type.setText(transaction.getType());

        switch(transaction.getType()){
            case "income":
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;

            case "expense":
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                break;

            case "startingBalance":
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                break;

            case "saving":
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                break;

            default:
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void setTransactions(List<Transaction> transactions){
        this.transactionList = transactions;
        notifyDataSetChanged();
    }
}
