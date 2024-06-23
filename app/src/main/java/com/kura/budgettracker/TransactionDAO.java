package com.kura.budgettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private SQLiteDatabase db;
    private DatabaseHelper databaseHelper;

    public TransactionDAO(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    public void open(){
        db = databaseHelper.getWritableDatabase();
    }

    public void close(){
        db.close();
    }

    public void insertTransaction(Transaction transaction){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_DESCRIPTION,transaction.getTitle());
        values.put(DatabaseHelper.KEY_AMOUNT, transaction.getAmount());
        values.put(DatabaseHelper.KEY_DATE, transaction.getDate());
        values.put(DatabaseHelper.KEY_TYPE, transaction.getType());
        db.insert(DatabaseHelper.TABLE_TRACKER,null,values);
    }

    public List<Transaction> getAllTransactions(){
        List<Transaction> transactions = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRACKER,null,null,null,null,null,null);

        if(cursor != null){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DESCRIPTION));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AMOUNT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DATE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TYPE));
                transactions.add(new Transaction(id, title, amount, date, type));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return transactions;
    }
}
