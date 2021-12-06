package com.example.instantmessages.messaging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

import com.bumptech.glide.Glide;
import com.example.instantmessages.R;
import com.example.instantmessages.models.Group;
import com.example.instantmessages.models.User;
import com.example.instantmessages.models.UserAdapter;
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
import java.util.List;
import java.util.UUID;
import java.util.zip.Inflater;

public class EditGroupActivity extends AppCompatActivity {
    private String thisGroupID;
    private Group thisGroup;
    private ImageView groupPicImageView;
    private EditText groupNameEditText;
    private RecyclerView memberRecycleView;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private List<User> userList;
    private Context context;
    private UserAdapter userAdapter;
    private boolean imageChosen;
    private FirebaseStorage firebaseStorage;
    private StorageReference sRef;
    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        initValue();
        setRecycleView();
        getGroupInformation();
    }

    private void getGroupInformation() {
        ref.child("groups").child(thisGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                thisGroup = snapshot.getValue(Group.class);
                Glide.with(context).load(thisGroup.getGroupImageURL())
                        .into(groupPicImageView);
                groupNameEditText.setText(thisGroup.getGroupName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.child("groups").child(thisGroupID).child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String newAddedMemberID = snapshot.getValue(String.class);
                ref.child("users").child(newAddedMemberID).addListenerForSingleValueEvent(new ValueEventListener() {
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

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedMemberID = snapshot.getValue(String.class);
                for(User user : userList){
                    if(user.getId().equals(removedMemberID)){
                        int pos = userList.indexOf(user);
                        userList.remove(user);
                        userAdapter.notifyItemRemoved(pos);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setRecycleView() {
        userAdapter.addInterface(new UserAdapter.OnClickSet() {
            @Override
            public void giveGroupKey(User user) {
                giveUserAdmin(user);
            }

            @Override
            public void deleteUser(User user) {
                deleteUserFromGroup(user);
            }
        });
        memberRecycleView.setAdapter(userAdapter);
        memberRecycleView.setLayoutManager(new LinearLayoutManager(context));
        memberRecycleView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
    }

    private void deleteUserFromGroup(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(user.getName())
                .setMessage("Xóa "+user.getName()+" ra khỏi nhóm?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ref.child("groups").child(thisGroupID).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> memberIDs = (ArrayList<String>) snapshot.getValue();
                                int pos = memberIDs.indexOf(new String(user.getId()));
                                ref.child("groups").child(thisGroupID).child("members").child(pos+"").removeValue();
                                ref.child("latestMessage").child(user.getId()).child(thisGroupID).removeValue();
                                ref.child("users").child(user.getId()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<String> groupIDs = (ArrayList<String>) snapshot.getValue();
                                        int pos = groupIDs.indexOf(new String(thisGroupID));
                                        ref.child("users").child(user.getId()).child("groups").child(pos+"").removeValue();

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
                })
                .setNegativeButton("Hủy bỏ",null)
                .show();
    }

    private void initValue() {
        thisGroupID = getIntent().getStringExtra("groupID");
        database = FirebaseDatabase.getInstance(getResources().getString(R.string.databaseURL));
        ref = database.getReference();
        groupPicImageView = (ImageView) findViewById(R.id.img_edit_group);
        groupNameEditText = (EditText) findViewById(R.id.name_edit_group);
        memberRecycleView = (RecyclerView) findViewById(R.id.member_edit_group);
        imageChosen = false;
        firebaseStorage = FirebaseStorage.getInstance();
        sRef = firebaseStorage.getReference();
        getSupportActionBar().setTitle("Chỉnh sửa nhóm");
        userList = new ArrayList<>();
        context = this;
        userAdapter = new UserAdapter(userList,context,2);
        groupPicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.getData();
            groupPicImageView.setImageURI(uri);
            imageChosen = true;
        }
    }

    private void giveUserAdmin(User user){
        ref.child("groups").child(thisGroupID).child("createdBy").setValue(user.getId());
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(user.getName())
                .setMessage("Chuyển trưởng nhóm cho "+user.getName()+"?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ref.child("groups").child(thisGroupID).child("createdBy").setValue(user.getId());
                        finish();
                    }
                })
                .setNegativeButton("Hủy",null)
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_member_edit_group_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.add_member_menu:
                Intent intent = new Intent(EditGroupActivity.this,AddMemberGroupActivity.class);
                intent.putExtra("groupID",thisGroupID);
                startActivity(intent);
                return true;
            case R.id.save_new_group_info:
                saveGroupToDatabase();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveGroupToDatabase() {
        String newGroupName = groupNameEditText.getText().toString();
        ref.child("groups").child(thisGroupID).child("groupName").setValue(newGroupName);
        if(imageChosen){
            ProgressDialog uploadDialog = new ProgressDialog(context);
            uploadDialog.setTitle("Lưu hình ảnh");
            uploadDialog.show();
            String key = UUID.randomUUID().toString();
            sRef.child("images").child(key).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.child("images").child(key).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String groupImgUrl = task.getResult().toString();
                            ref.child("groups").child(thisGroupID).child("groupImageURL").setValue(groupImgUrl);
                            finish();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    long s = snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                    uploadDialog.setMessage(s+"%");
                }
            });
        }else{
            finish();
        }
    }


}