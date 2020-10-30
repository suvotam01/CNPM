package com.example.bai2lab8;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
public class NewStudentActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;

    private Button btnSave;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_student_activity);

        // bind ui controls
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_phone);
        etPhone = findViewById(R.id.et_email);

        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String studentName = etName.getText().toString();
                final String studentEmail = etEmail.getText().toString();
                final String studentPhone = etPhone.getText().toString();

                if (studentName.trim().isEmpty()) {
                    // do something
                } else {
                    Student newStudent = new Student(studentName, studentEmail, studentPhone);
                    createStudent(newStudent);
                }
            }
        });
    }

    private void createStudent(final Student newStudent) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constants.URL_POST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                Toast.makeText(getBaseContext(), jsonObject.getString("message"),
                                        Toast.LENGTH_SHORT).show();

                                Intent studentsIntent = new Intent();
                                setResult(Activity.RESULT_OK, studentsIntent);
                                finish();
                            } else {
                                Toast.makeText(getBaseContext(), "Failed: " + jsonObject.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), "Failed: " + error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", newStudent.getName());
                params.put("email", newStudent.getEmail());
                params.put("phone", newStudent.getPhone());

                return params;
            }
        };
        RequestHandler.getInstance(getBaseContext()).addToRequestQueue(postRequest);
    }

}
