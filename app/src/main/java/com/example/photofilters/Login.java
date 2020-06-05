package com.example.photofilters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

public class Login extends AppCompatActivity {

    ImageButton login;
    TextView registerhere;
    TextView forgotpass;

    EditText email;
    EditText password;

    private String prefEmail,prefPass;

    ProgressBar pb;

    FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerhere = (TextView) findViewById(R.id.registerhere);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();


        registerhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Reg.class);
                startActivity(intent);
            }
        });

        forgotpass = findViewById(R.id.forgotpass);

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ForgotPass.class);
                startActivity(intent);
            }
        });


        email = findViewById(R.id.emaillogin);
        password = findViewById(R.id.passwordlogin);
        login =  findViewById(R.id.loginbtn);
        pb = findViewById(R.id.progressBar2);

        firebaseAuth = FirebaseAuth.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

    }

    private void userLogin(){

        pb.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pb.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    if(firebaseAuth.getCurrentUser().isEmailVerified()){
                        Intent intent = new Intent (Login.this, Dashboard.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    else{
                        //Toast.makeText(Login.this, "Please verify your email address", Toast.LENGTH_LONG).show();
                        Toasty.info(Login.this,"Please verify your email address",Toast.LENGTH_SHORT).show();

                    }

                }
                else{
                    //Toast.makeText(Login.this, "Email or Password Incorrect!",Toast.LENGTH_LONG).show();
                    Toasty.error(Login.this,"Email or Password Incorrect!",Toast.LENGTH_SHORT).show();
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
