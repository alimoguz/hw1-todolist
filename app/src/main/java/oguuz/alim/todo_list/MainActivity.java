package oguuz.alim.todo_list;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mList;
    private EditText task;
    private ImageButton add_btn;
    private String result_task;
    private int count = 0;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private ArrayList<String>  arrayList= new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTaskDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.main_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        Utils utils=new Utils();


        mFirebaseDatabase=utils.getDatabase();




        mList=(ListView) findViewById(R.id.list);
        adapter= new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, arrayList);
        mList.setAdapter(adapter);
        task=(EditText) findViewById(R.id.task);
        add_btn=(ImageButton) findViewById(R.id.add_btn);

        mTaskDatabase=mFirebaseDatabase.getReference().child("TASKS");
        mTaskDatabase.keepSynced(true);



        mTaskDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String value= dataSnapshot.getValue(String.class);
                arrayList.add(value);
                adapter.notifyDataSetChanged();
                task.setText("");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String value= dataSnapshot.getKey().toString();
                arrayList.remove(value);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String[] value = new String[]{
                        "UPDATE",
                        "DELETE"
                };
                AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("MAKE YOUR CHOICE");
                alert.setItems(value, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Arrays.asList(value).get(which).equals("DELETE")) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            builder.setTitle("Confirm");
                            builder.setMessage("Are you sure?");

                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {

                                    mTaskDatabase.child(arrayList.get(position)).removeValue();
                                    Toast.makeText(MainActivity.this, "Delete Successful...", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });

                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // Do nothing
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                        else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setIcon(R.drawable.update);
                            builder.setTitle("UPDATE");
                            builder.setMessage("Enter new task");
                            final EditText input = new EditText(MainActivity.this);
                            input.setText(arrayList.get(position).toString() , EditText.BufferType.EDITABLE);
                            input.selectAll();
                            builder.setView(input);
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(input.getText().toString().trim().equals("")){
                                        Toast.makeText(MainActivity.this, "The task can not be empty ", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        mTaskDatabase.child(input.getText().toString()).setValue(input.getText().toString());
                                        mTaskDatabase.child(arrayList.get(position)).removeValue();
                                        Toast.makeText(MainActivity.this, "Update Successful...", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();


                        }
                    }
                });

                AlertDialog dialog= alert.create();
                dialog.show();

                return false;

            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                result_task= task.getText().toString().trim();



                if(result_task.equals("")){
                    Toast.makeText(MainActivity.this, "The task can not be empty ", Toast.LENGTH_SHORT).show();
                }
                else if(result_task.contains(".")||result_task.contains("#")||result_task.contains("/")||result_task.contains("$")){
                    Toast.makeText(MainActivity.this, "Task must not contain '.' , '#' , '/' , '$' ", Toast.LENGTH_SHORT).show();

                }
                else{

                    mTaskDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(result_task)){
                                Toast.makeText(MainActivity.this, "Previously Added...", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                mTaskDatabase.child(result_task).setValue(result_task);
                                Toast.makeText(MainActivity.this, "Insert Successful...", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });






                }

            }

        });





    }
}

class Utils {

    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

}


