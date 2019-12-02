package com.example.photofilters;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class Home extends AppCompatActivity {


    TextView user;
    TextView capturetext;
    ImageButton logout;
    ImageButton capture;
    ImageButton gallery;

    Camera camera;
    FrameLayout framerLayout;
    ShowCamera showCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //user = findViewById(R.id.user);
        logout = findViewById(R.id.logoutbtn);
        capturetext = findViewById(R.id.capturetext);
        capture = findViewById(R.id.capture);
        gallery = findViewById(R.id.gallerybtn);

        framerLayout = findViewById(R.id.framelayout);

        //Firebase instance
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //show which user is logged in
        //user.setText(firebaseUser.getEmail());

        //Email Verification
        if(firebaseUser!=null && firebaseUser.isEmailVerified()){
           // Toast.makeText(Home.this, "Welcome", Toast.LENGTH_LONG).show();
            Toasty.success(Home.this,"Welcome",
                    Toast.LENGTH_SHORT).show();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Home.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

        //Open Camera
        camera = Camera.open();
        showCamera = new ShowCamera(this,camera);
        framerLayout.addView(showCamera);
    }
}
