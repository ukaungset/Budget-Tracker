package com.kura.budgettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {

    //database information
    private static final String DATABASE_NAME = "Budget.db";
    private static final int DATABASE_VERSION = 1;

    //table name
    public static final String TABLE_TRACKER = "tracker";

    //Tracker Table Column
    public static final String KEY_ID = "id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AMOUNT = "amount";

    Calendar calendar = Calendar.getInstance();

    //SQL to create tracker table
    private static final String CREATE_TABLE_TRACKER =
            "CREATE TABLE " + TABLE_TRACKER + " (" +
                    KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TYPE + " TEXT, " +
                    KEY_DATE + " TEXT, " +
                    KEY_DESCRIPTION + " TEXT, " +
                    KEY_AMOUNT + " REAL)";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //create require table
        sqLiteDatabase.execSQL(CREATE_TABLE_TRACKER);
    }

    /** @noinspection CallToPrintStackTrace*/
    public static String reformDate(String dateStr){
        SimpleDateFormat fromUser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

        try{
            return inputFormat.format(fromUser.parse(dateStr));
        }catch(ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    //insert data
    public long insertData (TransactionType type, String date, String description, int amount){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TYPE,type.name());
        contentValues.put(KEY_DATE,reformDate(date));
        contentValues.put(KEY_DESCRIPTION,description);
        contentValues.put(KEY_AMOUNT,amount);
        long id = database.insert(TABLE_TRACKER,null,contentValues);
        database.close();
        return id;
    }

    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT (*) FROM " + TABLE_TRACKER, null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count == 0;
            }
        } catch (SQLiteException e) {
            return true;
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return true;
    }
    //read IncomeData
    public Cursor readIncomeData(){
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TRACKER + " WHERE " + KEY_TYPE + " = \"Income\"";
        return database.rawQuery(query,null);
    }

    public Cursor readExpenseData(){
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TRACKER + " WHERE " + KEY_TYPE + " = 'Expense'";
        return database.rawQuery(query,null);
    }

    public Cursor filterExpenseByCurrentMonth(){
        SQLiteDatabase database = this.getReadableDatabase();
        String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        String currentMonth = String.format("%02d",calendar.get(Calendar.MONTH) + 1);

        String query = "SELECT * FROM " + TABLE_TRACKER + " WHERE " + KEY_TYPE + " = 'Expense' AND strftime('%Y-%m'," + KEY_DATE  + ")=? ";
        String monthFilter = currentYear + "-"+ currentMonth;
        return database.rawQuery(query,new String[]{monthFilter});
    }

    public Cursor dataFromCurrentMonth(TransactionType keyType){
        SQLiteDatabase database = this.getReadableDatabase();
        String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        String currentMonth = String.format("%02d",calendar.get(Calendar.MONTH) + 1);

        String query = "SELECT * FROM " + TABLE_TRACKER + " WHERE " + KEY_TYPE + " = ?  AND strftime('%Y-%m', " + KEY_DATE  + ")=? ";
        String monthFilter = currentYear + "-"+ currentMonth;
        return database.rawQuery(query, new String[]{String.valueOf(keyType), monthFilter});
    }

    public void updateFinalStatementForPreviousMonth(){
        SQLiteDatabase database = this.getWritableDatabase();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        String checkQuery = "SELECT COUNT (*) FROM " + TABLE_TRACKER + " WHERE " + KEY_TYPE + " = ? AND strftime ('%Y-%m' , " + KEY_DATE + ") = ?";
        Cursor checkCursor = database.rawQuery(checkQuery, new String[]{String.valueOf(TransactionType.Starting_Balance),currentYear+ "-"+ currentMonth});
        checkCursor.moveToFirst();
        int count = checkCursor.getInt(0);
        checkCursor.close();

        //to get last month
        if (count== 0) {
            int lastMonth = currentMonth - 1;
            int lastYear = currentYear;
            if(lastMonth == 0){
                lastMonth = 12;
                lastYear -= 1;
            }
            //Format the month for SQL
            String lastMonthTwoDigitFormatted = String.format("%02d",lastMonth);
            //calculating last month balance
            String query = "SELECT (SELECT COALESCE(SUM(" + KEY_AMOUNT + "),0) FROM " + TABLE_TRACKER +
                    " WHERE " + KEY_TYPE + " = 'Income' AND strftime ('%Y-%m', " + KEY_DATE + ") = ?) -"
                    + "(SELECT COALESCE(SUM(" + KEY_AMOUNT + "),0) FROM " + TABLE_TRACKER +
            " WHERE " + KEY_TYPE + " = 'Expense' AND strftime ('%Y-%m' ," + KEY_DATE + ") = ?)";
            Cursor cursor = database.rawQuery(query,new String[]{lastYear+"-"+lastMonthTwoDigitFormatted, lastYear+"-"+lastMonthTwoDigitFormatted});

            int lastMonthBalance = 0;
            if(cursor.moveToFirst()){
                lastMonthBalance = cursor.getInt(0);
            }
            cursor.close();

            //adding starting Balance
            SimpleDateFormat smp = new SimpleDateFormat("MMMM",Locale.getDefault());
            String lastMonthTextFormatted = smp.format(calendar.getTime());
            String startingDescription = "Starting Balance of " + lastMonthTextFormatted;

//        String startingDate = "01-"+lastMonth+"-"+lastYear;
            calendar.set(Calendar.DAY_OF_MONTH,1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
            String dateFormat = simpleDateFormat.format(calendar.getTime());
            insertData(TransactionType.Starting_Balance,dateFormat,startingDescription,lastMonthBalance);
        }

    }

    // update data
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //on upgrade drop older table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKER);
        //create new table
        onCreate(sqLiteDatabase);
    }
}
