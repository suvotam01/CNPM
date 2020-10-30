package com.example.bai2lab8;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    public static final String INTENT_STUDENT_ID = "STUDENT_ID";
    public static final String INTENT_STUDENT_NAME = "STUDENT_NAME";
    public static final String INTENT_STUDENT_EMAIL = "STUDENT_EMAIL";
    public static final String INTENT_STUDENT_PHONE = "STUDENT_PHONE";

    private RecyclerView mRecyclerView;
    private StudentAdaptor mAdapter;
    private List<Student> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind ui controls
        mRecyclerView = findViewById(R.id.recyclerView);

        listItems = new ArrayList<>();

        fetchStudents();

        // Initialize the adapter and attach it
        mAdapter = new StudentAdaptor(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL));

        registerForContextMenu(mRecyclerView);

        checkNetworkConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register broadcast receiver in onResume method of the activity.
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // UnRegister the receiver when ever you pause the activity to avoid leak of receiver.
        unregisterReceiver(broadcastReceiver);
    }


    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void fetchStudents() {
        listItems.clear();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.URL_LIST, (JSONObject) null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray jsonArray = response.getJSONArray("data");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject o = jsonArray.getJSONObject(i);
                                        Student item = new Student(
                                                o.getString("id"),
                                                o.getString("name"),
                                                o.getString("phone"),
                                                o.getString("email")
                                        );
                                        listItems.add(item);
                                        mAdapter.setEvents(listItems);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getBaseContext(), "error: " + error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        RequestHandler.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.miCreate) {
            openCreateNewEventActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCreateNewEventActivity() {
        // create intent to call NewEventActivity
        Intent newEventActivityIntent = new Intent(MainActivity.this, NewStudentActivity.class);
        startActivityForResult(newEventActivityIntent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            fetchStudents();
        }
    }


    private void checkNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Warning")
                    .setMessage("The application need wifi connection to use the application!")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            gotoWifiSetting();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void gotoWifiSetting() {
        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
    }

    // Create a receiver that has to run on receiving WiFi state change
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkConnection();
        }
    };
}