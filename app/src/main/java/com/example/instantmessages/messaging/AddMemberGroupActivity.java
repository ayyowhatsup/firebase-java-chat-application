package com.example.instantmessages.messaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.instantmessages.R;
import com.example.instantmessages.models.Group;
import com.example.instantmessages.models.User;
import com.example.instantmessages.models.UserSelectionAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddMemberGroupActivity extends AppCompatActivity {

    private String thisGroupID;
    private Group thisGroup;
    private ArrayList<User> userList;
    private UserSelectionAdapter userSelectionAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference ref;
    private ArrayList<String> memberIDs;
    private ArrayList<String> memberIDst;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_group);

        initValue();
        recyclerView.setAdapter(userSelectionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));


    }

    private void initValue() {
        thisGroupID = getIntent().getStringExtra("groupID");
        getSupportActionBar().setTitle("Thêm thành viên");
        ref = FirebaseDatabase.getInstance(getResources().getString(R.string.databaseURL)).getReference();
        userList = new ArrayList<>();
        memberIDs = new ArrayList<>();
        userSelectionAdapter = new UserSelectionAdapter(this,userList);
        recyclerView = (RecyclerView) findViewById(R.id.add_member_recycleview);
        ref.child("groups").child(thisGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                thisGroup = snapshot.getValue(Group.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.child("groups").child(thisGroupID).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberIDs = (ArrayList<String>) snapshot.getValue();
                ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            User user = dataSnapshot.getValue(User.class);
                            if(!memberIDs.contains(new String(user.getId()))){
                                userList.add(user);
                                userSelectionAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_member_group_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.confirm_add_member:
                List<User> selectedUsers = userSelectionAdapter.getSelectedUsers();
                if(selectedUsers.size()==0){
                    finish();
                }
                else{
                    String message = "";
                    memberIDst = new ArrayList<>();
                    memberIDst.addAll(memberIDs);
                    for(int i=0;i<selectedUsers.size();i++){
                        if(i!=selectedUsers.size()-1){
                            message+= selectedUsers.get(i).getName()+", ";
                        }
                        else{
                            message+= selectedUsers.get(i).getName();
                        }
                        memberIDst.add(selectedUsers.get(i).getId());
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Thêm thành viên")
                            .setMessage("Thêm "+ message +" vào nhóm?")
                            .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ref.child("groups").child(thisGroupID).child("members").setValue(memberIDst);
                                    for(User user : selectedUsers){
                                        ref.child("users").child(user.getId()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                ArrayList<String> groupAttendedIDs = new ArrayList<>();
                                                if(snapshot.exists()){
                                                    groupAttendedIDs = (ArrayList<String>) snapshot.getValue();
                                                }
                                                groupAttendedIDs.add(thisGroupID);
                                                ref.child("users").child(user.getId()).child("groups").setValue(groupAttendedIDs);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    finish();
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}