package com.example.auctionhouse.ui.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.auctionhouse.MainActivity;
import com.example.auctionhouse.MyListing;
import com.example.auctionhouse.Prevalent.Prevalent;
import com.example.auctionhouse.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class SettingsFragment extends Fragment {

    private CircleImageView profilePic;
    private TextView newPass, newPhone;
    private EditText newAddress;
    private Button updatePasswd;
    private Uri imageUri;
    private String myUrl = "";
    private StorageReference storgeProfilePic;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Switch nightMode;
    private Toolbar toolbar;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_settings, container, false);
        root.setBackgroundColor(getResources().getColor(R.color.colorFragments));

        nightMode = root.findViewById(R.id.night_mode);

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        final boolean switchCheck = sharedPreferences.getBoolean("isSwitchChecked", false);
        nightMode.setChecked(switchCheck);

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            nightMode.setText(R.string.disable_dark_mode);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            nightMode.setText(R.string.enable_dark_mode);
        }

        nightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(0).setChecked(true);

                if (isDarkModeOn) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("isDarkModeOn", false);
                    editor.putBoolean("isSwitchChecked", false);
                    editor.apply();
                    nightMode.setText(R.string.enable_dark_mode);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("isDarkModeOn", true);
                    editor.putBoolean("isSwitchChecked", true);
                    editor.apply();
                    nightMode.setText(R.string.disable_dark_mode);
                }
            }
        });

        FloatingActionButton fab = this.getActivity().findViewById(R.id.fab);
        fab.hide();

        storgeProfilePic = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        TextView nameView = root.findViewById(R.id.textView4);
        nameView.setText(Prevalent.currentOnlineUser.getName());

        swipeRefreshLayout = root.findViewById(R.id.refresh);

        newAddress = root.findViewById(R.id.editTextTextPostalAddress2);
        newPass = root.findViewById(R.id.textView6);
        newPass.setText("");
        newPhone = root.findViewById(R.id.textView5);

        profilePic = root.findViewById(R.id.profile_pic);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCropActivity();
            }
        });

        Picasso.get().load(Prevalent.currentOnlineUser.getImage()).noFade().placeholder(R.drawable.download).into(profilePic);

        root.findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() { // change phone
            @Override
            public void onClick(View view) { // change phone
                final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
                View dialogView = getLayoutInflater().inflate(R.layout.change_phone, null);

                final EditText confirm = dialogView.findViewById(R.id.edt_confirm_pass);
                Button button1 = dialogView.findViewById(R.id.buttonSubmit);
                Button button2 = dialogView.findViewById(R.id.buttonCancel);

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogBuilder.dismiss();
                        newAddress.setText("");
                    }
                });

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (confirm.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "You did not confirm the password", Toast.LENGTH_SHORT).show();
                        } else if (confirm.getText().toString().equals(Prevalent.currentOnlineUser.getPass())) {
                            Prevalent.currentOnlineUser.setPhone(newPhone.getText().toString());
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("phone", newPhone.getText().toString());
                            databaseReference.child(Prevalent.currentOnlineUser.getEmail()).updateChildren(map);
                            Toast.makeText(getActivity(), "Phone updated", Toast.LENGTH_SHORT).show();
                            newPhone.setText("");
                            dialogBuilder.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "The password is not ok", Toast.LENGTH_SHORT).show();
                            confirm.getText().clear();
                        }
                    }
                });

                dialogBuilder.setView(dialogView);
                dialogBuilder.show();
            }
        });

        updatePasswd = root.findViewById(R.id.button7);
        new MyAsyncTaskModifyPasswd().execute();

        root.findViewById(R.id.button8).setOnClickListener(new View.OnClickListener() { // change address
            @Override
            public void onClick(View view) {
                final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
                View dialogView = getLayoutInflater().inflate(R.layout.change_address, null);

                final EditText confirm = dialogView.findViewById(R.id.edt_confirm);
                Button button1 = dialogView.findViewById(R.id.buttonSubmit);
                Button button2 = dialogView.findViewById(R.id.buttonCancel);

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogBuilder.dismiss();
                        newAddress.setText("");
                    }
                });

                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (confirm.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "You did not confirm the password", Toast.LENGTH_SHORT).show();
                        } else if (confirm.getText().toString().equals(Prevalent.currentOnlineUser.getPass())) {
                            Prevalent.currentOnlineUser.setAddress(newAddress.getText().toString());
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("address", newAddress.getText().toString());
                            databaseReference.child(Prevalent.currentOnlineUser.getEmail()).updateChildren(map);
                            Toast.makeText(getActivity(), "Address updated", Toast.LENGTH_SHORT).show();
                            newAddress.setText("");
                            dialogBuilder.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "The password is not ok", Toast.LENGTH_SHORT).show();
                            confirm.getText().clear();
                        }
                    }
                });

                dialogBuilder.setView(dialogView);
                dialogBuilder.show();
            }
        });

        root.findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setMessage("Are you sure you want to delete your account?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes, delete my account", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        new MyAsyncTaskDeleteAcc().execute();

                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Picasso.get().load(Prevalent.currentOnlineUser.getImage()).placeholder(R.drawable.download).into(profilePic);
                ((MyListing) getActivity()).setPic();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return root;
    }

    public void startCropActivity() {
        CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();


            ProgressDialog progressBar = new ProgressDialog(getContext());
            progressBar.setTitle("Updating profile picture");
            progressBar.setMessage("Please wait...");
            progressBar.setCanceledOnTouchOutside(false);
            progressBar.show();

            new MyAsyncTaskUploadPic().execute();

            progressBar.dismiss();

            profilePic.setImageURI(imageUri);
            ((MyListing) getActivity()).setPic();
        } else {
            Toast.makeText(getContext(), "Error... Try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePic() {
        if (imageUri != null) {
            final StorageReference fileRef = storgeProfilePic.child(Prevalent.currentOnlineUser.getEmail() + ".jpg");
            StorageTask uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("image", myUrl);
                        databaseReference.child(Prevalent.currentOnlineUser.getEmail()).updateChildren(map);
                        Prevalent.currentOnlineUser.setImage(myUrl);

                    } else {
                        Toast.makeText(getActivity(), "Error on uploading the image...", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Image was not selected", Toast.LENGTH_LONG).show();
        }
    }

    class MyAsyncTaskDeleteAcc extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getEmail());
            reference.removeValue();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Products");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot product : snapshot.getChildren()) {
                            if (product.child("userUpload").getValue().equals(Prevalent.currentOnlineUser.getEmail())) {
                                product.getRef().removeValue();
                            }
                            storgeProfilePic.child(Prevalent.currentOnlineUser.getEmail() + ".jpg").delete();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Paper.book().destroy();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
            return null;
        }
    }

    class MyAsyncTaskUploadPic extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            uploadProfilePic();
            return null;
        }
    }

    class MyAsyncTaskModifyPasswd extends AsyncTask<LayoutInflater, Void, Void> {

        @Override
        protected Void doInBackground(final LayoutInflater... layoutInflaters) {
            updatePasswd.setOnClickListener(new View.OnClickListener() { // change pass
                @Override
                public void onClick(View view) {
                    final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
                    View dialogView = getLayoutInflater().inflate(R.layout.change_password, null);

                    final EditText confirm_new = dialogView.findViewById(R.id.edt_confirm_new);
                    final EditText confirm_old = dialogView.findViewById(R.id.edt_confirm_old);
                    Button button1 = dialogView.findViewById(R.id.buttonSubmit);
                    Button button2 = dialogView.findViewById(R.id.buttonCancel);

                    button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogBuilder.dismiss();
                            newPass.setText("");
                        }
                    });
                    button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (confirm_new.getText().toString().isEmpty()) {
                                Toast.makeText(getActivity(), "You did not confirm the new password", Toast.LENGTH_SHORT).show();
                            } else if (confirm_old.getText().toString().isEmpty()) {
                                Toast.makeText(getActivity(), "You did not confirm the old password", Toast.LENGTH_SHORT).show();
                            } else if (confirm_new.getText().toString().equals(newPass.getText().toString())) {
                                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getEmail());
                                databaseReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.child("pass").getValue().toString().equals(confirm_old.getText().toString())) {
                                            databaseReference.child("pass").setValue(confirm_new.getText().toString());
                                            Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                                            newPass.setText("");
                                            dialogBuilder.dismiss();
                                        } else {
                                            Toast.makeText(getActivity(), "Old password is not correct", Toast.LENGTH_SHORT).show();
                                            confirm_old.getText().clear();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                Toast.makeText(getActivity(), "New passwords don't match", Toast.LENGTH_SHORT).show();
                                confirm_new.getText().clear();
                                confirm_old.getText().clear();
                            }
                        }
                    });

                    dialogBuilder.setView(dialogView);
                    dialogBuilder.show();

                }
            });

            return null;
        }
    }
}
