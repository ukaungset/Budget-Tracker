package com.kura.budgettracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button addButton, viewBtn, deleteBtn;
    TextView monthAndYear, dayLeft;
    int balance, income, expense, StartingBalance, daysOfMonth;
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check first time
        if (isFirstLunch()|| dbHelper.isDatabaseEmpty()) {
            //Intent intent = new Intent(MainActivity.this, StartingBalanceActivity.class);
            showStartingBalanceDialog();
        }

        //creating calendar for monthAndYear textView
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        String monthYear = simpleDateFormat.format(calendar.getTime());

        //creating day Views
        daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        LinearLayout dayLayout = findViewById(R.id.dayLayout);
        for (int i = 1; i <= daysOfMonth; i++) {
            TextView textView = new TextView(this);

            //creating Layout Params
            int sizeInPixels = (int) (50 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInPixels, sizeInPixels);
            textView.setText(String.valueOf(i));
            int padding = (int) (8 * getResources().getDisplayMetrics().density);
            layoutParams.setMargins(padding, padding, padding, padding);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            if (i != today) {
                textView.setTextSize(18);
                if (i < today) {
                    textView.setBackgroundResource(R.drawable.done_fill);
                } else {
                    textView.setBackgroundResource(R.drawable.coming);
                }
            } else {
                textView.setTextSize(18);
                textView.setTextColor(Color.parseColor("#FFFFFF"));
                textView.setBackgroundResource(R.drawable.circle_fill);
            }
            dayLayout.addView(textView);
        }

        //day left update
        dayLeft = findViewById(R.id.dayLeft);
        int dayLeftInt = daysOfMonth - today;
        dayLeft.setText(dayLeftInt + " days left");

        //balance update
        getTrackerData();
        balance = income - expense;
        TextView balanceText = findViewById(R.id.balance);
        balanceText.setText(String.format(Locale.US, "%,d", balance));

        //transition record update
        viewBtn = findViewById(R.id.viewButton);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ViewTransaction.class);
                startActivity(intent);
            }
        });

        //month & year update
        monthAndYear = findViewById(R.id.monthAndYear);
        monthAndYear.setText(monthYear);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddingIncome.class);
            startActivity(intent);
        });

        scheduleMonthlyAlarm();
    } //end of OnCreate Method

    private void showStartingBalanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Starting Balance");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", ((dialog, which) -> {
            int StartingBalance = Integer.parseInt(input.getText().toString());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateFormat = simpleDateFormat.format(calendar.getTime());
            dbHelper.insertData("StartingBalance", dateFormat, "First Month Starting Balance", StartingBalance);
        }));
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

    public void getTrackerData() {
        income = 0;
        expense = 0;
        Cursor incomeCursor = dbHelper.dataFromCurrentMonth("Income");
        if (incomeCursor.moveToFirst())
        {
            do
            {
                income += incomeCursor.getInt(4);
            }while (incomeCursor.moveToNext());
        }
        incomeCursor.close();

        Cursor expenseCursor = dbHelper.dataFromCurrentMonth("Expense");
        if (expenseCursor.moveToFirst())
        {
            do
            {
                expense += expenseCursor.getInt(4);
            }while (expenseCursor.moveToNext());
        }
        expenseCursor.close();

//        Cursor startingBalanceCursor = dbHelper.dataFromCurrentMonth("Starting Balance");
//        if (startingBalanceCursor != null) {
//            StartingBalance = startingBalanceCursor.getInt(4);
//        } else
//            StartingBalance = 0;
//        startingBalanceCursor.close();

    } // end of getTrackerData

    private void scheduleMonthlyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE);

        //set the alarm to start 00:01 AM
        if(alarmIntent == null) {
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MINUTE, 1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * daysOfMonth, alarmIntent);
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