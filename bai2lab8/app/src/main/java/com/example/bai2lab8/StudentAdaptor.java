package com.example.bai2lab8;

import static com.example.bai2lab8.MainActivity.INTENT_STUDENT_EMAIL;
import static com.example.bai2lab8.MainActivity.INTENT_STUDENT_ID;
import static com.example.bai2lab8.MainActivity.INTENT_STUDENT_NAME;
import static com.example.bai2lab8.MainActivity.INTENT_STUDENT_PHONE;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentAdaptor extends RecyclerView.Adapter<StudentAdaptor.MyViewHolder> {
    int mPosition;
    private Context context;
    private List<Student> mStudentList;

    public StudentAdaptor(Context context) {
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.students_row_layout, viewGroup, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.name.setText(mStudentList.get(i).getName());
        myViewHolder.email.setText(mStudentList.get(i).getEmail());
        myViewHolder.phone.setText(mStudentList.get(i).getPhone());

    }

    @Override
    public int getItemCount() {
        if (mStudentList == null) {
            return 0;
        }
        return mStudentList.size();

    }

    public void setEvents(List<Student> studentList) {
        mStudentList = studentList;
        notifyDataSetChanged();
    }

    public List<Student> getEvents() {

        return mStudentList;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements OnCreateContextMenuListener, OnItemTouchListener {

        TextView name;
        TextView email;
        TextView phone;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            email = itemView.findViewById(R.id.tv_email);
            phone = itemView.findViewById(R.id.tv_phone);

            itemView.setOnCreateContextMenuListener(this);
//      itemView.setOnClickListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenuInfo contextMenuInfo) {
            final Student selectedStudent = mStudentList.get(getAdapterPosition());
            contextMenu.setHeaderTitle(selectedStudent.getName());

            contextMenu.add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    startEditStudentActivity(selectedStudent);
                    return true;
                }
            });
            contextMenu.add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    showDeleteConfirmDialog(selectedStudent);
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            Student selectedStudent = mStudentList.get(getAdapterPosition());
            startEditStudentActivity(selectedStudent);
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void startEditStudentActivity(Student selectedStudent) {
        // create intent to call NewEventActivity
        Intent newEventActivityIntent = new Intent(context, EditStudentActivity.class);
        newEventActivityIntent.putExtra(INTENT_STUDENT_ID, selectedStudent.getId());
        newEventActivityIntent.putExtra(INTENT_STUDENT_NAME, selectedStudent.getName());
        newEventActivityIntent.putExtra(INTENT_STUDENT_EMAIL, selectedStudent.getEmail());
        newEventActivityIntent.putExtra(INTENT_STUDENT_PHONE, selectedStudent.getPhone());
        ((Activity) context).startActivityForResult(newEventActivityIntent, 100);
    }

    private void showDeleteConfirmDialog(final Student selectedStudent) {
        Builder builder = new Builder(context);
        builder.setTitle("Delete " + selectedStudent.getName());
        builder.setMessage("Are you sure to delete this student?");
        builder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mStudentList.remove(selectedStudent);
                deleteStudent(selectedStudent);
                setEvents(mStudentList);
            }
        });

        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    private void deleteStudent(final Student selectedStudent) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constants.URL_DELETE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                Toast.makeText(context, jsonObject.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed: " + jsonObject.getString("message"),
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
                        Toast.makeText(context, "Failed: " + error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", selectedStudent.getId());

                return params;
            }
        };
        RequestHandler.getInstance(context).addToRequestQueue(postRequest);
    }
}
