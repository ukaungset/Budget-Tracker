package com.kura.budgettracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button addButton, viewBtn, deleteBtn;
    TextView monthAndYear, dayLeft, incomeAmt, expenseAmt;
    int balance, income, expense, StartingBalance, daysOfMonth;
    DatabaseHelper dbHelper;
    TransactionDAO transactionDAO;
    Calendar calendar;
    Calendar weeklyCalendar;
    private LinearLayout weekContainer;
    private static final int REQUEST_CODE_ADD_TRANSACTION = 1;
    private static final int REQUEST_CODE_VIEW_TRANSACTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        dbHelper.getReadableDatabase();

        transactionDAO = new TransactionDAO(this);
        transactionDAO.open();

        incomeAmt = findViewById(R.id.incomeAmountText);
        expenseAmt = findViewById(R.id.expenseAmountText);
        weekContainer = findViewById(R.id.dayLayout);
        weeklyCalendar = Calendar.getInstance();
        renderWeek(weeklyCalendar);

        //check first time
        if (isFirstLunch()|| dbHelper.isDatabaseEmpty()) {
            //Intent intent = new Intent(MainActivity.this, StartingBalanceActivity.class);
            showStartingBalanceDialog();
        }

        //creating calendar for monthAndYear textView
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthYear = simpleDateFormat.format(calendar.getTime());

        //creating day Views
        daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = calendar.get(Calendar.DAY_OF_MONTH);

        //day left update
        dayLeft = findViewById(R.id.dayLeft);
        int dayLeftInt = daysOfMonth - today;
        dayLeft.setText(dayLeftInt + " days left");

        //balance update
        getTrackerData();
        updateBalance();

        //transition record update
        viewBtn = findViewById(R.id.viewButton);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ViewData.class);
                startActivityForResult(intent,REQUEST_CODE_VIEW_TRANSACTION);
            }
        });

        //month & year update
        monthAndYear = findViewById(R.id.monthAndYear);
        monthAndYear.setText(monthYear);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddingIncome.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_TRANSACTION);
        });

        scheduleMonthlyAlarm();
    } //end of OnCreate Method

    private void updateBalance() {
        balance = ( StartingBalance + income) - expense;
        TextView balanceText = findViewById(R.id.balance);
        balanceText.setText(String.format(Locale.US, "%,d", balance));
        incomeAmt.setText(String.format(getString(R.string.d_kyats), income));
        expenseAmt.setText(String.format(getString(R.string.d_kyats), expense));
        if(balance>0){
            balanceText.setTextColor(Color.GREEN);
        } else {
            balanceText.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == REQUEST_CODE_ADD_TRANSACTION || requestCode == REQUEST_CODE_VIEW_TRANSACTION) && resultCode == RESULT_OK) {
            getTrackerData();
            updateBalance();
        }
    }

    private void renderWeek(Calendar wCalendar) {
        weekContainer.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE\ndd", Locale.getDefault());

        Calendar weekStart = (Calendar) wCalendar.clone();
        weekStart.set(Calendar.DAY_OF_WEEK,weekStart.getFirstDayOfWeek());
        Calendar today = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd",Locale.getDefault());

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        for(int i = 0; i < 7; i++){
            TextView dayView = new TextView(this);
            dayView.setText(sdf.format(weekStart.getTime()));
            dayView.setPadding(8, 8, 8, 8);
            dayView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(90, 90);
            layoutParams.gravity = Gravity.CENTER;
            if(simpleDateFormat.format(weekStart.getTime()).equals(simpleDateFormat.format(today.getTime()))){
                dayView.setBackgroundColor(primaryColor);
            }
            weekContainer.addView(dayView,layoutParams);
            weekStart.add(Calendar.DAY_OF_MONTH,1);
        }
    }

    private void showStartingBalanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Starting Balance");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        Calendar startingCalendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String title = "Starting Balance of " + monthFormat.format(startingCalendar.getTime());
        startingCalendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = simpleDateFormat.format(startingCalendar.getTime());

        builder.setPositiveButton("OK", ((dialog, which) -> {
            String  amount = input.getText().toString();
            int startingBalance = 0;
            if(!amount.isEmpty()) {
                startingBalance = Integer.parseInt(amount);
            }
            Transaction transaction = new Transaction(title, startingBalance, date, TransactionType.Starting_Balance);
            transactionDAO.insertTransaction(transaction);
            Toast.makeText(this, "Starting Balance Added!", Toast.LENGTH_SHORT).show();
        }));
        builder.setNegativeButton("Skip", (dialog, which) -> {
            Transaction transaction = new Transaction(title,0,date,TransactionType.Starting_Balance);
            transactionDAO.insertTransaction(transaction);
            dialog.cancel();
        });
        builder.show();
    }

    public void getTrackerData() {
        income = 0;
        expense = 0;
        Cursor incomeCursor = dbHelper.dataFromCurrentMonth(TransactionType.Income);
        if (incomeCursor.moveToFirst())
        {
            do
            {
                income += incomeCursor.getInt(4);
            }while (incomeCursor.moveToNext());
        }
        incomeCursor.close();

        Cursor expenseCursor = dbHelper.dataFromCurrentMonth(TransactionType.Expense);
        if (expenseCursor.moveToFirst())
        {
            do
            {
                expense += expenseCursor.getInt(4);
            }while (expenseCursor.moveToNext());
        }
        expenseCursor.close();

        Cursor startingBalanceCursor = dbHelper.dataFromCurrentMonth(TransactionType.Starting_Balance);
        if (startingBalanceCursor.moveToFirst()) {
            StartingBalance = startingBalanceCursor.getInt(4);
        } else {
            StartingBalance = 0;
        }
        startingBalanceCursor.close();

    } // end of getTrackerData

    private void scheduleMonthlyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        //set the alarm to start 00:01 AM
        if(alarmIntent == null) {
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Calendar calendar = Calendar.getInstance();
            int daysOfTheMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MINUTE, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * daysOfTheMonth, alarmIntent);
        }
    }//end of scheduleMonthlyAlarm

    private boolean isFirstLunch() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);
        if(isFirstTime)
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
        }
        return isFirstTime;
    }
}