package com.example.photofilters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    ImageButton login;
    TextView registerhere;

    EditText email;
    EditText password;

    ProgressBar pb;

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerhere = (TextView) findViewById(R.id.registerhere);

        registerhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Reg.class);
                startActivity(intent);
            }
        });


        email = findViewById(R.id.emaillogin);
        password = findViewById(R.id.passwordlogin);
        login =  findViewById(R.id.loginbtn);
        pb = findViewById(R.id.progressBar2);

        mAuth = FirebaseAuth.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

    }

    private void userLogin(){

        pb.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pb.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    Intent intent = new Intent (Login.this, Test.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(Login.this, "Email or Password Incorrect!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void validate(){
        if(!EmailValidator.getInstance().validate(email.getText().toString().trim())){
            email.setError("Invalid email address");
        }
        else if(password.length()==0 )
        {
            password.setError("Enter password");
        }
        else{
            userLogin();
        }
    }
}
