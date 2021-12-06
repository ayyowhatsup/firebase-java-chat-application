package com.example.instantmessages.messaging;

import static androidx.recyclerview.widget.LinearLayoutManager.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instantmessages.R;
import com.example.instantmessages.models.Group;
import com.example.instantmessages.models.User;
import com.example.instantmessages.models.UserSelectionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateNewGroupActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button create;
    private TextView name;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private List<User> member = new ArrayList<>();
    private List<User> selectedUsers;
    private DatabaseReference ref = FirebaseDatabase.getInstance("https://instantmessages-c52b8-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_group);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_user_choose);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Tạo một nhóm mới ...");
        create = (Button) findViewById(R.id.create_group_button);
        name = (TextView) findViewById(R.id.name_edittext_new_group);

        List<User> userList = new ArrayList<>();
        UserSelectionAdapter userSelectionAdapter = new UserSelectionAdapter(this,userList);
        recyclerView.setAdapter(userSelectionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, VERTICAL));


        ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getId().equals(mAuth.getUid())){
                        user.setChecked(false);
                        userList.add(user);
                    }
                    else if (user.getId().equals(mAuth.getUid())){
                        member.add(user);
                    }
                }
                userSelectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedUsers = userSelectionAdapter.getSelectedUsers();
                member.addAll(selectedUsers);
                if(member.size()<3){
                    Toast.makeText(CreateNewGroupActivity.this,"Nhóm của bạn cần ít nhất 3 thành viên...",Toast.LENGTH_SHORT).show();
                    member.removeAll(selectedUsers);
                }else{
                    String groupName = name.getText().toString();
                    if(!groupName.equals("")){
                        List<String> memberIDs = new ArrayList<>();
                        for(User user : member){
                            memberIDs.add(user.getId());
                        }
                        String key = ref.child("groups").push().getKey();
                        ref.child("groups").child(key).setValue(new Group(key,groupName,memberIDs));
                        for(int i=0;i<member.size();i++){
                            User user = member.get(i);
                            ref.child("users").child(user.getId()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<String> t = new ArrayList<>();
                                    if(snapshot.exists()){
                                        t = (ArrayList<String>) snapshot.getValue();
                                    }
                                    t.add(key);
                                    ref.child("users").child(user.getId()).child("groups").setValue(t);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                        Intent intent = new Intent(CreateNewGroupActivity.this,ChatLogActivity.class);
                        intent.putExtra("ID",key);
                        intent.putExtra("chatType","groups");
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(CreateNewGroupActivity.this,"Hãy đặt tên cho nhóm của bạn!",Toast.LENGTH_SHORT).show();
                        member.removeAll(selectedUsers);
                    }
                }
            }
        });
    }
}
