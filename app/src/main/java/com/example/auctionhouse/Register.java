package com.example.auctionhouse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private ProgressDialog loadingBar;
    private CheckBox Box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        loadingBar = new ProgressDialog(this);

        String checkBoxText = "Check to Agree to the <a href='https://www.termsandconditionsgenerator.com/'> Terms and Conditions</a>";
        Box = findViewById(R.id.checkBox);

        Box.setText(Html.fromHtml(checkBoxText));
        Box.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void registerButton(View view) {
        loadingBar.setTitle("Create Account");
        loadingBar.setMessage("Please wait, while we check the credentials.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        new MyAsyncTask().execute();
    }

    private void createAccount(final String name, final String surname, final String phone, final String emailId, final String pass, final String address) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.child("Users").child(emailId).exists()) {
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("email", emailId);
                    userdataMap.put("phone", phone);
                    userdataMap.put("name", name);
                    userdataMap.put("surname", surname);
                    userdataMap.put("pass", pass);
                    userdataMap.put("address", address);
                    userdataMap.put("uploadedListings", 0);
                    userdataMap.put("boughtListings", 0);

                    RootRef.child("Users").child(emailId).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Your account has been created!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(Register.this,MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(Register.this, "Network Error: Please try again...", Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                }
                            });

                } else {
                    Toast.makeText(Register.this, "This credentials have already been used. Please try again.",Toast.LENGTH_LONG).show();
                    EditText Email = findViewById(R.id.editTextTextEmailAddress2);
                    EditText pass = findViewById(R.id.editTextPassword);
                    EditText confirm_pass = findViewById(R.id.editTextPassword2);
                    Email.getText().clear();
                    pass.getText().clear();
                    confirm_pass.getText().clear();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void terms_cond(View view) {
        Button button = findViewById(R.id.button3);
        CheckBox checkBox = findViewById(R.id.checkBox);

        if (checkBox.isChecked()) {
            button.setEnabled(true);
        } else if (!checkBox.isChecked()) {
            button.setEnabled(false);
        }
    }

    public void cancelButton(View view) {
        Intent intent = new Intent(Register.this,MainActivity.class);
        startActivity(intent);
    }

    class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {


            EditText Name = findViewById(R.id.editTextTextPersonName);
            EditText Surname = findViewById(R.id.editTextTextPersonName2);
            EditText Phone = findViewById(R.id.editTextTextPersonName3);
            EditText emailId = findViewById(R.id.editTextTextEmailAddress2);
            TextInputEditText pass = findViewById(R.id.editTextPasswordE);
            TextInputEditText confirm_pass = findViewById(R.id.editTextPassword2E);
            EditText address = findViewById(R.id.editTextTextPostalAddress);

            if (Name.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't complete the name", Toast.LENGTH_SHORT).show();
            } else if (Surname.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't complete the surname", Toast.LENGTH_SHORT).show();
            } else if (Phone.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't complete the phone number", Toast.LENGTH_SHORT).show();
            } else if (emailId.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't complete the email", Toast.LENGTH_SHORT).show();
            } else if (pass.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't complete the password", Toast.LENGTH_SHORT).show();
            } else if (confirm_pass.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't confirm the password", Toast.LENGTH_SHORT).show();
            } else if (!pass.getText().toString().isEmpty() && !confirm_pass.getText().toString().isEmpty() && !pass.getText().toString().equals(confirm_pass.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_LONG).show();
            } else if (address.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "You didn't complete the address", Toast.LENGTH_SHORT).show();
            } else if (!Box.isChecked()) {
                Toast.makeText(getApplicationContext(), "You didn't accept the Terms and Conditions", Toast.LENGTH_SHORT).show();
            } else {

                String email = emailId.getText().toString().replace(".","_");
                email = email.replace("@","-");
                createAccount(Name.getText().toString(), Surname.getText().toString(), Phone.getText().toString(), email,
                        pass.getText().toString(), address.getText().toString());
            }
            return null;
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Register.this,MainActivity.class);
        startActivity(intent);
    }
}
