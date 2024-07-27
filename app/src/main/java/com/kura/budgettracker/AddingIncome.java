package com.kura.budgettracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddingIncome extends AppCompatActivity {

    EditText dateText, descriptionText, amountText;
    Button addData, cancleButton;
    TextView inputDate;
    int amount;
    String date, description;
    Spinner dataSpinner;
    TransactionType type;
    Calendar calendar;
    private Transaction transaction;
    private TransactionDAO transactionDAO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monthly_income);

        calendar = Calendar.getInstance();

        //connecting views
        dateText = findViewById(R.id.editTextDate);
        descriptionText = findViewById(R.id.editTextDescription);
        amountText = findViewById(R.id.editTextAmount);
        addData = findViewById(R.id.buttonAdd);
        dataSpinner = findViewById(R.id.input_data_type);
        cancleButton = findViewById(R.id.cancelButton);

        transactionDAO = new TransactionDAO(this);
        transactionDAO.open();

        TextWatcher textWatcher =new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValue();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        dateText.addTextChangedListener(textWatcher);
        descriptionText.addTextChangedListener(textWatcher);
        amountText.addTextChangedListener(textWatcher);

        dataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        type = TransactionType.Income;
                        break;

                    case 1:
                        type = TransactionType.Expense;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type = TransactionType.Income;
            }
        });

        Intent getIntent = getIntent();
        if (getIntent != null && getIntent.hasExtra("TRANSACTION")) {
            transaction = (Transaction) getIntent.getSerializableExtra("TRANSACTION");
            populateUI(transaction);
        }

        addData.setOnClickListener(v -> {
            saveTransaction();
        });

        cancleButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        dateText.setKeyListener(null);
        dateText.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddingIncome.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, day);
                            String myFormat = "yyyy-MM-dd";
                            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                            dateText.setText(sdf.format(calendar.getTime()));
                        }
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void checkFieldsForEmptyValue() {
        String description = descriptionText.getText().toString();
        String date = dateText.getText().toString();
        String amount = amountText.getText().toString();

        boolean allFieldsFilled = !description.isEmpty() && !date.isEmpty() && !amount.isEmpty();
        addData.setEnabled(allFieldsFilled);
    }

    private void populateUI(Transaction transaction) {
        descriptionText.setText(transaction.getTitle());
        dateText.setText(transaction.getDate());
        amountText.setText(String.valueOf(transaction.getAmount()));

        switch (transaction.getType()) {
            case Income:
                dataSpinner.setSelection(0);
                break;

            case Expense:
                dataSpinner.setSelection(1);
                break;
        }
    }

    private void saveTransaction() {
        String title = descriptionText.getText().toString();
        String date = dateText.getText().toString();
        int amount = Integer.parseInt(amountText.getText().toString());
        TransactionType type = TransactionType.valueOf(dataSpinner.getSelectedItem().toString());

        if(transaction == null) {
            transaction = new Transaction(title, amount, date, type);
            transactionDAO.insertTransaction(transaction);
            Toast.makeText(this, "New Transaction Added", Toast.LENGTH_SHORT).show();
        } else {
            transaction.setTitle(title);
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setDate(date);
            transactionDAO.updateTransaction(transaction);
            Toast.makeText(this, "Transaction is updated", Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        transactionDAO.close();
        super.onDestroy();
    }
}