package com.kura.budgettracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.String.*;

public class ViewData extends AppCompatActivity {
    TextView month;
    LinearLayout dateLayout, categoryLayout, amountLayout;
    Spinner spinner;

    //working with database
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        //connecting to the views
        month = findViewById(R.id.date_textView);
        dateLayout = findViewById(R.id.date_layout);
        categoryLayout = findViewById(R.id.category_layout);
        amountLayout = findViewById(R.id.amount_layout);
        spinner = findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,R.array.budget_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String type = (String) adapterView.getItemAtPosition(position);
                switch (type){
                    case "Income":
                        showData("Income");
                        break;

                    case "Expense":
                        showData("Expense");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                showData("Expense");
            }
        });

        //creating calendar
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthString = simpleDateFormat.format(calendar.getTime());

        //setting text
        month.setText(monthString);
    }

    private void showData(String type) {
        Cursor cursor;
        if(type.endsWith("Income")) {
            cursor = databaseHelper.readIncomeData();
        } else {
            cursor = databaseHelper.filterExpenseByCurrentMonth();
        }
        dateLayout.removeAllViews();
        categoryLayout.removeAllViews();
        amountLayout.removeAllViews();
        //income total
        int income = 0;
        //reading cursor
        if(cursor.getCount()!=0){
            while (cursor.moveToNext()){
                //creating textView
                TextView dateTextView = new TextView(this);
                dateTextView.setText(valueOf(cursor.getString(2)));
                dateLayout.addView(dateTextView);

                TextView categoryTextView = new TextView(this);
                categoryTextView.setText(cursor.getString(3));
                categoryTextView.setGravity(Gravity.CENTER);
                categoryLayout.addView(categoryTextView);

                TextView amountTextView = new TextView(this);
                amountTextView.setText(valueOf(cursor.getInt(4)));
                income += cursor.getInt(4);
                amountTextView.setGravity(Gravity.RIGHT);
                amountLayout.addView(amountTextView);

            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_data, Toast.LENGTH_LONG).show();
        }
    }
}