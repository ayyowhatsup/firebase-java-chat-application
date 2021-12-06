package com.example.instantmessages.authentication;

import android.app.ProgressDialog;
import android.content.Context;
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

import com.example.instantmessages.MainActivity;
import com.example.instantmessages.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private String TAG ="LoginActivity";
    private EditText email_input,password_input;
    private Button login;
    private Context context;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email_input = (EditText) findViewById(R.id.email_edittext_log);
        password_input = (EditText) findViewById(R.id.pass_edittext_log);
        context  = this;

        login = (Button) findViewById(R.id.login_button_log);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = email_input.getText().toString();
                String pwd = password_input.getText().toString();
                if(!email.equals("")&&!pwd.equals("")){
                    mAuth.signInWithEmailAndPassword(email,pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG,"Login successfully!");
                                        ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setMessage("Đang đăng nhập");
                                        progressDialog.setProgress(1500);
                                        progressDialog.show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        progressDialog.dismiss();
                                        finish();
                                    }
                                    else{
                                        Log.d(TAG,task.getException().getMessage());
                                        Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(LoginActivity.this,"Vui lòng điền đẩy đủ thông tin!",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
