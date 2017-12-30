package com.raaziatariq.reminderapp;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.raaziatariq.reminderapp.Adapter.ListItemAdapter;
import com.raaziatariq.reminderapp.Model.ToDo;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    List<ToDo> toDoList = new ArrayList<>();
    FirebaseFirestore db;
    RecyclerView listitem;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;

    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build();

    public MaterialEditText text_title;
    public MaterialEditText description;
    public boolean isUpdated = false;
    public String idUpdated ="";
    SpotsDialog dialog;

    ListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        db.setFirestoreSettings(settings);
        db = FirebaseFirestore.getInstance();

        dialog= new SpotsDialog(this);
        text_title=findViewById(R.id.title);
        description=findViewById(R.id.description);
        fab =findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isUpdated)
                {
                    setData(text_title.getText().toString(),description.getText().toString());
                }
                else {
                    updateData(text_title.getText().toString(),description.getText().toString());
                    isUpdated =!isUpdated;
                }

            }
        });
        listitem = findViewById(R.id.listTodo);
        listitem.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listitem.setLayoutManager(layoutManager);
        loadData();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Delete"))
        deleteItem(item.getOrder());
        return super.onContextItemSelected(item);
    }

    private void deleteItem(int index) {
        db.collection("ToDoList").document(toDoList.get(index).getId()).delete().
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                loadData();
            }
        });
    }

    private void updateData(String title, String description) {
        db.collection("ToDoList").document(idUpdated).update("title",title,"description",description).
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this,"Updated !",Toast.LENGTH_SHORT).show();
            }
        });

        db.collection("ToDoList").document(idUpdated).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                loadData();
            }
        });
    }

    private void setData(String title, String description) {
        String id = UUID.randomUUID().toString();
        Map<String,Object> todo = new HashMap<>();
        todo.put("id",id);
        todo.put("title",title);
        todo.put("description",description);

        db.collection("ToDoList").document(id).set(todo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                loadData();
            }
        });
    }

    private void loadData() {
        dialog.show();
        if(toDoList.size()>0)
            toDoList.clear();
        db.collection("ToDoList").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(DocumentSnapshot doc:task.getResult())
                {
                    ToDo toDo = new ToDo(doc.getString("id"),doc.getString("title"),doc.getString("description"));
                    toDoList.add(toDo);
                }
                adapter = new ListItemAdapter(MainActivity.this,toDoList);
                listitem.setAdapter(adapter);
                dialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
