package com.kura.budgettracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.kura.budgettracker.R;

import java.util.List;

class MyAdapter extends BaseAdapter {
    private Context context;
    private List<Transaction> transactions;

    public MyAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }
    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public Object getItem(int position) {
        return transactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.entry_view, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
        return convertView;
    }
    private static class ViewHolder{
        TextView date, title, amount;
        ViewHolder(View itemView) {
            date = itemView.findViewById(R.id.dateTextView);
            title = itemView.findViewById(R.id.descriptionTextView);
            amount = itemView.findViewById(R.id.amountTextView);
        }
        void bind(Transaction transaction) {
            int Amount = transaction.getAmount();
            String amountFormat = String.format("%,d Kyats", Amount);
            date.setText(transaction.getDate());
            title.setText(transaction.getTitle());
            amount.setText(amountFormat);

            switch (transaction.getType()) {
                case Income:
                    amount.setTextColor(amount.getContext().getResources().getColor(android.R.color.holo_green_dark));
                    break;

                case Expense:
                    amount.setTextColor(amount.getContext().getResources().getColor(android.R.color.holo_red_dark));
                    break;

                case Starting_Balance:
                    amount.setTextColor(amount.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    break;

                case Saving:
                    amount.setTextColor(amount.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                    break;

                default:
                    amount.setTextColor(amount.getContext().getResources().getColor(android.R.color.black));
                    break;
            }
        }
    }
}
