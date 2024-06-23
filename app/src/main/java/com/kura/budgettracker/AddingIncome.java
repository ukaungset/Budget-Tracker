package com.kura.budgettracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddingIncome extends AppCompatActivity {

    EditText dateText,descriptionText, amountText;
    Button addData;
    TextView incomeData,inputDate;
    int  amount;
    String date, description;
    String type;

    //about dataBase
    DatabaseHelper databaseHelper = new DatabaseHelper(this);


    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_income);

        Calendar calendar = Calendar.getInstance();

        //connecting views
        dateText = findViewById(R.id.editTextDate);
        descriptionText = findViewById(R.id.editTextDescription);
        amountText = findViewById(R.id.editTextAmount);
        addData = findViewById(R.id.buttonAdd);
        Spinner dataSpinner = findViewById(R.id.input_data_type);
        inputDate = findViewById(R.id.input_date);
        dataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        type = "Income";
                        break;

                    case 1:
                        type = "Expense";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type = "Income";
            }
        });

        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        databaseHelper.getWritableDatabase();

        //clicking button
        addData.setOnClickListener(view -> {
            //getting resources
            date = dateText.getText().toString();
            description = descriptionText.getText().toString();
            amount = Integer.parseInt(amountText.getText().toString());
            //insertData();
            long trackerID = databaseHelper.insertData(type, date, description, amount);
            String id = String.valueOf(trackerID);
            Toast.makeText(this, id, Toast.LENGTH_LONG).show();

        });
    }
    private void showDatePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        String selectedDate = formatDate(year,month,day);
                        inputDate.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private String formatDate(int year, int month, int day)
    {
        return day + "-" + month + "-" + year;
    }

}