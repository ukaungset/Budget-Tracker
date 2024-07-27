package com.kura.budgettracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>
{
    private List<Transaction> transactionList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder
    {
        public TextView title, date, amount, type;
        public Button btnEdit, btnDelete;

        public TransactionViewHolder(View itemView, final OnItemClickListener listener){
            super (itemView);
            title = itemView.findViewById(R.id.tvTransactionTitle);
            date = itemView.findViewById(R.id.tvTransactionDate);
            amount = itemView.findViewById(R.id.tvTransactionAmount);
            type = itemView.findViewById(R.id.tvTransactionType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onEditClick(position);
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }

    public TransactionAdapter(List<Transaction> transactions, OnItemClickListener listener)
    {
        this.transactionList = transactions;
        this.onItemClickListener = listener;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction,parent,false);
        return new TransactionViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.title.setText(transaction.getTitle());
        holder.amount.setText(String.valueOf(transaction.getAmount()));
        holder.date.setText(transaction.getDate());
        holder.type.setText(transaction.getType().name());

        switch(transaction.getType()){
            case Income:
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;

            case Expense:
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                break;

            case Starting_Balance:
                holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                break;

            case Saving:
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
