package com.example.instantmessages.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instantmessages.R;
import com.example.instantmessages.models.User;
import com.example.instantmessages.models.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewMessagesActivity extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://instantmessages-c52b8-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference ref = database.getReference("users/");
    private RecyclerView recyclerView;
    private Button addNewGroup;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message_activity);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setTitle("Gủi đến ...");
        recyclerView = (RecyclerView) findViewById(R.id.user_recycle_view);
        List<User> list = new ArrayList<User>();
        UserAdapter userAdapter = new UserAdapter(list, this,1);
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!mAuth.getUid().equals(user.getId())){
                        list.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
        addNewGroup = (Button) findViewById(R.id.add_new_group_btn);
        addNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewMessagesActivity.this,CreateNewGroupActivity.class);
                startActivity(intent);
            }
        });

    }
}
