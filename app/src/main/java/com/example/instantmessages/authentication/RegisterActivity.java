package com.example.instantmessages.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instantmessages.R;
import com.example.instantmessages.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private String TAG ="RegisterActivity";
    private EditText name_input,email_input,pass_input;
    private Button reg;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name_input = (EditText) findViewById(R.id.name_edittext_reg);
        email_input = (EditText) findViewById(R.id.email_edittext_reg);
        pass_input = (EditText) findViewById(R.id.pass_edittext_reg);
        reg = (Button) findViewById(R.id.register_button_reg);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email_input.getText().toString();
                String name = name_input.getText().toString();
                String pwd = pass_input.getText().toString();
                if(!email.equals("")&&!name.equals("")&&!pwd.equals("")){
                    mAuth.createUserWithEmailAndPassword(email,pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG,"Register successfully!");
                                        User user = new User(FirebaseAuth.getInstance().getUid(), name,email);
                                        updateDatabase(user);
                                        Intent in = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(in);
                                    }else{
                                        Log.d(TAG,task.getException().getMessage());
                                        Toast.makeText(RegisterActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(RegisterActivity.this,"Vui lòng điền đẩy đủ thông tin!",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void updateDatabase(User user){
        //Add user to Realtime Database;
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://instantmessages-c52b8-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference ref = database.getReference("users/"+user.getId());
        Map<String,String> user1 = new HashMap<>();
        user1.put("name",user.getName());
        user1.put("profileURL",user.getProfileURL());
        user1.put("email",user.getEmail());
        user1.put("id",user.getId());
        ref.setValue(user1);
    }
}

