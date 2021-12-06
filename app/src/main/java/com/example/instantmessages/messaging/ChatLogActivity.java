package com.example.instantmessages.messaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instantmessages.R;
import com.example.instantmessages.models.Group;
import com.example.instantmessages.models.Message;
import com.example.instantmessages.models.MessageAdapter;
import com.example.instantmessages.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatLogActivity extends AppCompatActivity {
    private String messageTo;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database;
    private DatabaseReference ref,mRef;
    private Button sendMsg;
    private EditText msgEditText;
    private RecyclerView recyclerView;
    private List<Message> list = new ArrayList<>();
    private MessageAdapter adapter;
    private ActionBar actionBar;
    private ImageView attachImg;
    private StorageReference sRef;
    private Context context;
    private Uri uri;
    private String chatType;
    private User thisUser = new User();
    private Group thisGroup = new Group();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_log_activity);
        database = FirebaseDatabase.getInstance(getResources().getString(R.string.databaseURL));
        actionBar = getSupportActionBar();
        sendMsg = (Button) findViewById(R.id.send_button_chatlog);
        msgEditText = (EditText) findViewById(R.id.message_edittext_chatlog);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_chatlog);
        attachImg = (ImageView) findViewById(R.id.attach_image_chat_log);
        context = this;
        sRef = FirebaseStorage.getInstance().getReference();
        ref = database.getReference();
        messageTo = getIntent().getStringExtra("ID");
        chatType = getIntent().getStringExtra("chatType");

        database.getReference().child(chatType).child(messageTo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(chatType.equals("groups")){
                    adapter = new MessageAdapter(list,context,"groupChat");
                    thisGroup = snapshot.getValue(Group.class);;
                    actionBar.setTitle(thisGroup.getGroupName());
                    mRef = database.getReference("groups").child(thisGroup.getGroupID()).child("messages");
                }
                else{
                    adapter = new MessageAdapter(list,context,"singleChat");
                    thisUser = snapshot.getValue(User.class);
                    actionBar.setTitle(thisUser.getName());
                    mRef = database.getReference("messages/" + mAuth.getUid() + "/" + messageTo);
                }

                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                mRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message msg = snapshot.getValue(Message.class);
                        list.add(msg);
                        adapter.notifyItemInserted(list.size());
                        recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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




        attachImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,0);
            }
        });

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = msgEditText.getText().toString();
                if (!content.equals("")) {
                    if(chatType.equals("users")){
                        String key = database.getReference("messages/" + mAuth.getUid() + "/" + messageTo).push().getKey();
                        DatabaseReference mRef1 = database.getReference("messages/" + mAuth.getUid() + "/" + messageTo + "/" + key);
                        DatabaseReference mRef2 = database.getReference("messages/" + messageTo + "/" + mAuth.getUid() + "/" + key);
                        Message msg = new Message(content, mAuth.getUid(),messageTo,"singleChat");
                        mRef1.setValue(msg);
                        mRef2.setValue(msg);
                        msgEditText.setText("");
                        ref.child("latestMessage").child(mAuth.getUid()).child(messageTo).setValue(msg);
                        ref.child("latestMessage").child(messageTo).child(mAuth.getUid()).setValue(msg);
                    }
                    else{
                        String key = ref.child("groups").child(messageTo).child("messages").push().getKey();
                        DatabaseReference mRef = ref.child("groups").child(messageTo).child("messages").child(key);
                        Message msg = new Message(content, mAuth.getUid(),messageTo,"groupChat");
                        mRef.setValue(msg);
                        msgEditText.setText("");
                        ref.child("groups").child(messageTo).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> memberIDs = (ArrayList<String>) snapshot.getValue();
                                for(String memberID : memberIDs){
                                    if (memberID!=null){
                                        ref.child("latestMessage").child(memberID).child(messageTo).setValue(msg);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null){
            uri = data.getData();
            String fileName = UUID.randomUUID().toString();
            sRef.child("images").child(fileName).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.child("images").child(fileName).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(chatType.equals("users")){
                                String key = database.getReference("messages/" + mAuth.getUid() + "/" + messageTo).push().getKey();
                                DatabaseReference mRef1 = database.getReference("messages/" + mAuth.getUid() + "/" + messageTo + "/" + key);
                                DatabaseReference mRef2 = database.getReference("messages/" + messageTo + "/" + mAuth.getUid() + "/" + key);
                                Message msg = new Message(true,task.getResult().toString(),mAuth.getUid(),messageTo,"singleChat");
                                mRef1.setValue(msg);
                                mRef2.setValue(msg);
                                ref.child("latestMessage").child(mAuth.getUid()).child(messageTo).setValue(msg);
                                ref.child("latestMessage").child(messageTo).child(mAuth.getUid()).setValue(msg);
                            }
                            else{
                                String key = database.getReference().child("groups").child(messageTo).child("messages").push().getKey();
                                DatabaseReference mRef1 = database.getReference().child("groups").child(messageTo).child("messages").child(key);
                                Message msg = new Message(true,task.getResult().toString(),mAuth.getUid(),messageTo,"groupChat");
                                mRef1.setValue(msg);
                                ref.child("groups").child(messageTo).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<String> memberIDs = (ArrayList<String>) snapshot.getValue();
                                        for(String memberID : memberIDs){
                                            if (memberID!=null){
                                                ref.child("latestMessage").child(memberID).child(messageTo).setValue(msg);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(chatType.equals("groups")){
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.group_chat_menu,menu);
        }
        return true;
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.manage_group_menu:
                if(!thisGroup.getCreatedBy().equals(mAuth.getUid())){
                    Toast.makeText(ChatLogActivity.this,"Bạn không phải là trưởng nhóm!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(ChatLogActivity.this,EditGroupActivity.class);
                    intent.putExtra("groupID",thisGroup.getGroupID());
                    startActivity(intent);
                }
                return true;
            case R.id.show_info_group_menu:
                Intent intent = new Intent(ChatLogActivity.this,GroupInfoActivity.class);
                intent.putExtra("groupID",thisGroup.getGroupID());
                startActivity(intent);
                return true;
            case R.id.leave_group_menu:
                leaveGroup();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void leaveGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(thisGroup.getGroupName())
                .setMessage("Rời khỏi nhóm "+thisGroup.getGroupName()+"?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ref.child("groups").child(thisGroup.getGroupID()).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> memberIDs = (ArrayList<String>) snapshot.getValue();
                                if(thisGroup.getCreatedBy().equals(mAuth.getUid())){
                                    for(String member : memberIDs){
                                        if(member!=null && !member.equals(mAuth.getUid())){
                                            ref.child("groups").child(thisGroup.getGroupID()).child("createdBy").setValue(member);
                                        }
                                    }
                                }
                                int pos = memberIDs.indexOf(new String(mAuth.getUid()));
                                ref.child("groups").child(thisGroup.getGroupID()).child("members").child(pos+"").removeValue();
                                ref.child("users").child(mAuth.getUid()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<String> groupIDs = (ArrayList<String>) snapshot.getValue();
                                        int pos = groupIDs.indexOf(new String(thisGroup.getGroupID()));
                                        ref.child("users").child(mAuth.getUid()).child("groups").child(pos+"").removeValue();
                                        ref.child("latestMessage").child(mAuth.getUid()).child(messageTo).removeValue();
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
                        finish();
                    }
                })
                .setNegativeButton("Hủy bỏ",null)
                .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(chatType.equals("groups")){
            ref.child("groups").child(messageTo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    thisGroup = snapshot.getValue(Group.class);
                    actionBar.setTitle(thisGroup.getGroupName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}
