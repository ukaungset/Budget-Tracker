package com.kura.budgettracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewData extends AppCompatActivity {
    TextView month;
    private Spinner spinner;
    private ListView mTransactionList;
    private TransactionDAO transactionDAO;
    private List<Transaction> transactionList;
    private List<Transaction> filteredTransactions;
    private MyAdapter adapter;
    private static final int REQUEST_CODE_EDIT_TRANSACTION = 1;

    //working with database
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        //connecting to the views
        month = findViewById(R.id.date_textView);
        spinner = findViewById(R.id.spinner);
        mTransactionList = findViewById(R.id.monthlyTransactionList);

        transactionDAO = new TransactionDAO(this);
        transactionDAO.open();

        transactionList = transactionDAO.getAllTransactionsForCurrentMonth();
        filteredTransactions = new ArrayList<>(transactionList);
        adapter = new MyAdapter(this, filteredTransactions);
        mTransactionList.setAdapter(adapter);

        registerForContextMenu(mTransactionList);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,R.array.filter_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String type = (String) adapterView.getItemAtPosition(position);
                filterTransaction(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //nothing to do
            }
        });

        //creating calendar
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String monthString = simpleDateFormat.format(calendar.getTime()).toUpperCase(Locale.getDefault());
        //setting text
        month.setText(monthString);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        Transaction selectedTransaction = filteredTransactions.get(info.position);
//
//        if(selectedTransaction.getType() != TransactionType.Starting_Balance) {
//            MenuInflater inflater = getMenuInflater();
//            inflater.inflate(R.menu.context_menu, menu);
//        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Transaction selectedTransaction = filteredTransactions.get(position);

        switch (item.getItemId()) {
            case R.id.menu_edit:
                Intent intent = new Intent(ViewData.this, AddingIncome.class);
                intent.putExtra("TRANSACTION", (Serializable) selectedTransaction);
                startActivity(intent);
                Toast.makeText(this, "Item Editing", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_delete:
                transactionDAO.deleteTransaction(filteredTransactions.get(position).getId());
                filteredTransactions.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Item Deleted", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void filterTransaction(String filter) {
        filteredTransactions.clear();
        switch (filter) {
            case "Income":
                for (Transaction t : transactionList) {
                    if (t.getType() == TransactionType.Income) {
                        filteredTransactions.add(t);
                    }
                }
                break;

            case "Expense":
                for (Transaction t : transactionList) {
                    if (t.getType() == TransactionType.Expense) {
                        filteredTransactions.add(t);
                    }
                }
                break;

            case "All":
            default:
                filteredTransactions.addAll(transactionList);
                break;
        }
        adapter.notifyDataSetChanged();
    }

}