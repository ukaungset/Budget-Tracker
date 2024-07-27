package com.kura.budgettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
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
        values.put(DatabaseHelper.KEY_TYPE, transaction.getType().name());
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
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AMOUNT));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DATE));
                String typeString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TYPE));
                TransactionType type = TransactionType.valueOf(typeString);
                transactions.add(new Transaction(id, title, amount, date, type));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return transactions;
    }

    public List<Transaction> getAllTransactionsForCurrentMonth() {
        List<Transaction> transactions = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        String currentMonth = String.format("%d-%02d-", year, month);

        String selection = DatabaseHelper.KEY_DATE + " LIKE ?";
        String[] selectionArgs = {currentMonth + "%"};
        String orderBy = DatabaseHelper.KEY_DATE + "," + DatabaseHelper.KEY_TYPE;

        Cursor cursor = db.query(DatabaseHelper.TABLE_TRACKER,null, selection, selectionArgs,null,null,orderBy);
        if(cursor != null){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DATE));
                String typeString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TYPE));
                TransactionType type = TransactionType.valueOf(typeString);
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_AMOUNT));

                transactions.add(new Transaction(id, title, amount, date, type));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return transactions;
    }

    public void deleteTransaction(int id){
        db.delete(DatabaseHelper.TABLE_TRACKER,DatabaseHelper.KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateTransaction(Transaction transaction){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_TYPE, transaction.getType().name());
        values.put(DatabaseHelper.KEY_DATE, transaction.getDate());
        values.put(DatabaseHelper.KEY_DESCRIPTION, transaction.getTitle());
        values.put(DatabaseHelper.KEY_AMOUNT, transaction.getAmount());
        db.update(DatabaseHelper.TABLE_TRACKER,values,DatabaseHelper.KEY_ID + " = ?", new String[]{String.valueOf(transaction.getId())});
    }
}
