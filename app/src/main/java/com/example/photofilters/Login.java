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

    private PrefManager pref;

    String emailpref, passwordpref, typepref;

    private CheckBox keep_loggedin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = new PrefManager(this);

        registerhere = (TextView) findViewById(R.id.registerhere);

        keep_loggedin = findViewById(R.id.keeplogin_chkbox);

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

        if(pref.isDataSet()){userLogin();}


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

    }

    private void userLogin(){

        if(pref.isDataSet()){
            String[] loginData = pref.getLoginData();
            emailpref = loginData[0];
            passwordpref = loginData[1];
        } else {
            emailpref = email.getText().toString();
            passwordpref = password.getText().toString();
        }

        if (emailpref.isEmpty()) {
            Toast.makeText(this, "User Id cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordpref.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }



        pb.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(emailpref,passwordpref)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pb.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    if(firebaseAuth.getCurrentUser().isEmailVerified()){
                        String uid = firebaseAuth.getCurrentUser().getUid();
                        checkUserType(uid);
                    }
                    else{
                        //Toast.makeText(Login.this, "Please verify your email address", Toast.LENGTH_LONG).show();
                        Toasty.info(Login.this,"Please verify your email address",Toast.LENGTH_SHORT).show();

                    }

                }
                else{
                    //Toast.makeText(Login.this, "Email or Password Incorrect!",Toast.LENGTH_LONG).show();
                    Toasty.error(Login.this,"Email or Password Incorrect!",Toast.LENGTH_SHORT).show();
                    pref.resetData();
                }
            }
        });
    }


    private void checkUserType(String uid)
    {
        final boolean result = false;
        if(pref.isDataSet()){
            String[] loginData = pref.getLoginData();
            typepref = loginData[2];
        }
            typepref = "users";
            Intent i = new Intent(this, Dashboard.class);
            LoginAs("users", uid, i);

    }


    private void LoginAs(final String usertype, final String uid, final Intent i)
    {

        databaseReference.child(usertype).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    if(!pref.isDataSet() && keep_loggedin.isChecked()){
                        pref.setLoginData(emailpref,passwordpref,typepref);
                    }
                    startActivity(i);
                    finish();
                }
                else
                {
                    Toast.makeText(Login.this, "You do not have an account", Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    pref.resetData();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
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
