package com.example.auctionhouse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.auctionhouse.Model.Users;
import com.example.auctionhouse.Prevalent.Prevalent;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private EditText emailId;
    private TextInputEditText passB;
    private CheckBox checkBox;
    private ProgressDialog loadingBar;
    private TextView resetPasswd;
    private String parentDbName = "Users", UserEmailKey, UserPassKey, email;
    private AlertDialog alert;
    private AlertDialog.Builder builder;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailId = findViewById(R.id.editTextTextEmailAddress);
        passB = findViewById(R.id.oldPasswordE);
        checkBox = findViewById(R.id.checkBox);
        resetPasswd = findViewById(R.id.textView2);
        loadingBar = new ProgressDialog(this);

        Paper.init(this);

        showDialog(this);

        UserEmailKey = Paper.book().read(Prevalent.UserEmailKey);
        UserPassKey = Paper.book().read(Prevalent.UserPassKey);

        if (UserEmailKey != null && UserPassKey != null) {
            if (!TextUtils.isEmpty(UserEmailKey) && !TextUtils.isEmpty(UserPassKey)) {

                loadingBar.setTitle("Logged in already");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                new MyAsyncTaskAllowAcces().execute();

                loadingBar.dismiss();
            }
        }

        resetPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,ResetPasswdActivity.class);
                startActivity(i);
            }
        });
    }

    private void AllowAccess(final String email, final String pass) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(parentDbName).child(email).exists()) {
                    Users userData = snapshot.child(parentDbName).child(email).getValue(Users.class);

                    assert userData != null;
                    if (userData.getEmail().equals(email) && userData.getPass().equals(pass)) {
                        Intent intent = new Intent(MainActivity.this, MyListing.class);
                        Prevalent.currentOnlineUser = userData;
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Error connecting to local database", Toast.LENGTH_SHORT).show();
                        emailId.getText().clear();
                        passB.getText().clear();
                    }
                }
                loadingBar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void registerButton(View view) {
        Intent intent = new Intent(MainActivity.this,Register.class);
        startActivity(intent);
        finish();
    }

    public void signinButton(View view) {

        if (emailId.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "You didn't complete the email address...", Toast.LENGTH_SHORT);
            toast.show();
        } else if (passB.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "You didn't complete the password...", Toast.LENGTH_SHORT);
            toast.show();
        } else {

            email = emailId.getText().toString().replace(".","_");
            email = email.replace("@","-");
            hideKeyboard(this);

            loadingBar.setTitle("Logging into account");
            loadingBar.setMessage("Please wait, while we check the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            new MyAsyncTaskLogin().execute();

            loadingBar.dismiss();
        }

    }

    private void loginUser(final String email, final String pass) {
        if (checkBox.isChecked()) {
            Paper.book().write(Prevalent.UserEmailKey, email);
            Paper.book().write(Prevalent.UserPassKey, pass);
        }

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(parentDbName).child(email).exists()) {
                    Users userData = snapshot.child(parentDbName).child(email).getValue(Users.class);

                    assert userData != null;
                    if (userData.getEmail().equals(email)) {
                        if (userData.getPass().equals(pass)) {
                            Toast.makeText(MainActivity.this, "Logged in successfully...", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                            Intent intent = new Intent(MainActivity.this,MyListing.class);
                            Prevalent.currentOnlineUser = userData;
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Password is incorrect!", Toast.LENGTH_SHORT).show();
                            passB.getText().clear();
                            loadingBar.dismiss();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Email is incorrect!", Toast.LENGTH_SHORT).show();
                        emailId.getText().clear();
                        passB.getText().clear();
                        loadingBar.dismiss();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Account with this credentials does not exists. Please try registering first.", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
            return true;
        } else {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");
                return !ipAddr.equals("");
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void showDialog(final Context context) {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to the internet!")
                .setCancelable(false)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (isConnected(context)) {
                            alert.dismiss();
                        }
                        else {
                            startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS));
                        }
                    }
                })
                .setNegativeButton("No, QUIT.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_HOME);
                        startActivity(i);
                    }
                });
        alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        if (!isConnected(context)) {
            alert.show();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    class MyAsyncTaskAllowAcces extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {
            AllowAccess(UserEmailKey, UserPassKey);
            return null;
        }
    }

    class MyAsyncTaskLogin extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            loginUser(email, passB.getText().toString());
            return null;
        }
    }
}