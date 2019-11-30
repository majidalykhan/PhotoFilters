package com.example.photofilters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class Reg extends AppCompatActivity {

    ImageButton register;
    TextView loginhere;
    EditText name;
    EditText username;
    EditText email;
    EditText password;

    ProgressBar pb;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        loginhere = (TextView) findViewById(R.id.loginhere);
        loginhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Login.class);
                startActivity(intent);
            }
        });

        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register =  findViewById(R.id.regbtn);
        pb = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });
    }

    private void saveUserData()
    {
        final String names = name.getText().toString().trim();
        final String usernames = username.getText().toString().trim();
        final String emails = email.getText().toString().trim();
        final String passwords = password.getText().toString().trim();

        pb.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(emails, passwords)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pb.setVisibility(View.GONE);

                        if (task.isSuccessful()) {

                            User user = new User(
                                    names,
                                    usernames,
                                    emails,
                                    passwords
                            );


                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(Reg.this, "Registeration Successful",
                                                Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Reg.this, Login.class);
                                        startActivity(intent);
                                    }
                                    else{
                                        Toast.makeText(Reg.this,"Signup Failed!",Toast.LENGTH_LONG).show();

                                    }
                                }
                            });


                        }
                        else{
                            Toast.makeText(Reg.this, task.getException()
                                    .getMessage(),Toast.LENGTH_LONG).show();

                        }
                    }
                });

    }

    private void validate(){
        if(name.length()==0){
            name.setError("Enter name");
        }
        else if(username.length()==0){
            username.setError("Enter username");

        }
        else if(email.length()==0){
            email.setError("Enter email");
        }
        else if(password.length()==0){
            password.setError("Enter password");
        }
        else{
            saveUserData();
        }
    }

}
