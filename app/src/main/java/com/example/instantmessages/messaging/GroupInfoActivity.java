package com.example.instantmessages.messaging;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instantmessages.R;
import com.example.instantmessages.models.Group;
import com.example.instantmessages.models.User;
import com.example.instantmessages.models.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity {
    private String thisGroupID;
    private Group group;
    private ImageView groupImgView;
    private TextView groupNameTextView;
    private RecyclerView userRecycleView;
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://instantmessages-c52b8-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference ref = database.getReference("groups/");
    private DatabaseReference mRef = database.getReference("users/");
    private List<String> memberList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_show_group_info);
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Thông tin nhóm");
        actionBar.setDisplayHomeAsUpEnabled(true);
        groupImgView = (ImageView) findViewById(R.id.group_img_view);
        groupNameTextView = (TextView) findViewById(R.id.group_name_textview);
        userRecycleView = (RecyclerView) findViewById(R.id.members_recyvleview);
        UserAdapter userAdapter = new UserAdapter(userList,this,1);
        userRecycleView.setAdapter(userAdapter);
        userRecycleView.setLayoutManager(new LinearLayoutManager(this));
        userRecycleView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        thisGroupID = getIntent().getStringExtra("groupID");
        ref.child(thisGroupID).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList = (ArrayList<String>) snapshot.getValue();
                System.out.println(memberList);
                for(String member : memberList){
                    if(member != null){
                        mRef.child(member).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                userList.add(user);
                                userAdapter.notifyItemInserted(userList.size());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.child(thisGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(Group.class);
                Glide.with(GroupInfoActivity.this)
                        .load(group.getGroupImageURL())
                        .into(groupImgView);
                groupNameTextView.setText(group.getGroupName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
