package com.example.instantmessages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instantmessages.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ChangeUserInfoActivity extends AppCompatActivity {
    private TextView nameEditText;
    private ImageView profImageView;
    private String myID;
    private User me;
    private DatabaseReference ref;
    private Button saveInfo;
    private Uri uri;
    private boolean itemChosen;
    private StorageReference sRef;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);

        nameEditText = (TextView) findViewById(R.id.name_edit_info_activity);
        profImageView = (ImageView) findViewById(R.id.profImg_info_activity);
        myID = FirebaseAuth.getInstance().getUid();
        me = new User();
        saveInfo =(Button) findViewById(R.id.save_info_activity);
        ref= FirebaseDatabase.getInstance(getResources().getString(R.string.databaseURL)).getReference();
        itemChosen = false;
        context =this;
        sRef = FirebaseStorage.getInstance().getReference();
        ref.child("users").child(myID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                me = snapshot.getValue(User.class);
                Glide.with(context).load(me.getProfileURL())
                        .apply(new RequestOptions().override(160,160))
                        .into(profImageView);
                nameEditText.setText(me.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,0);
            }
        });


        saveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = nameEditText.getText().toString();
                ref.child("users").child(myID).child("name").setValue(newName);
                if(itemChosen){
                    ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("Tải lên ảnh đại diện");
                    progressDialog.show();
                    String imageId = UUID.randomUUID().toString();
                    sRef.child("images").child(imageId).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sRef.child("images").child(imageId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String profileURL = task.getResult().toString();
                                    ref.child("users").child(myID).child("profileURL").setValue(profileURL);
                                    finish();
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            long s = snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                            progressDialog.setMessage(s+" %");
                        }
                    });
                }else{
                    finish();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0 && resultCode == Activity.RESULT_OK &&data!=null){
            uri = data.getData();
            profImageView.setImageURI(uri);
            itemChosen = true;
        }
    }
}