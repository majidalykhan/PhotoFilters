package com.example.photofilters;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class Welcome extends AppCompatActivity {

    ImageButton signin, signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        PrefManager pref = new PrefManager(this);

        if(pref.isDataSet()){
            signin(findViewById(R.id.loginbtn));
        }

        signin = findViewById(R.id.signin);
        signup = findViewById(R.id.signup);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Welcome.this,Login.class);
                startActivity(i);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Welcome.this,Reg.class);
                startActivity(i);
            }
        });
    }

    private void signin(View viewById) {

    }
}
