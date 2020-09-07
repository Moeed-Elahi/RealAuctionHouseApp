package com.example.auctionhouse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ResetPasswdActivity extends AppCompatActivity {
    private Button cancel, reset;
    private EditText email, oldPasswd;
    private TextInputEditText passwd, confirmPasswd;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
        loadingBar = new ProgressDialog(this);

        cancel = findViewById(R.id.cancel);
        reset = findViewById(R.id.reset);
        email = findViewById(R.id.typeEmailAddress);
        passwd = findViewById(R.id.typePasswordE);
        confirmPasswd = findViewById(R.id.confirmPasswordE);
        oldPasswd = findViewById(R.id.oldPassword);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ResetPasswdActivity.this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingBar.setTitle("Updating password");
                loadingBar.setMessage("Please wait, while we check your email.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                new MyAsyncTask().execute();
            }
        });
    }

    private void resetPasswd() {
        String emailId = email.getText().toString().replace(".","_");
        emailId = emailId.replace("@","-");
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailId);

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String oldPass = snapshot.child("pass").getValue().toString();

                    if (oldPass.equals(oldPasswd.getText().toString())) {
                        RootRef.child("pass").setValue(passwd.getText().toString());
                        loadingBar.dismiss();
                        Toast.makeText(ResetPasswdActivity.this, "Password reseted", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(ResetPasswdActivity.this,MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(ResetPasswdActivity.this, "Old password is wrong, try again!", Toast.LENGTH_SHORT).show();
                        email.getText().clear();
                        passwd.getText().clear();
                        oldPasswd.getText().clear();
                        confirmPasswd.getText().clear();
                    }
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(ResetPasswdActivity.this, "This email doesn't exists. Please try again.",Toast.LENGTH_LONG).show();
                    email.getText().clear();
                    passwd.getText().clear();
                    oldPasswd.getText().clear();
                    confirmPasswd.getText().clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            resetPasswd();
            return null;
        }
    }
}
