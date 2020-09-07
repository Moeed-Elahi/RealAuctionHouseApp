package com.example.auctionhouse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopupFragment extends DialogFragment {
    private Button dismissButton;
    private CircleImageView profile_pic;
    private TextView name, surname, phone, email, address;
    private DatabaseReference databaseReference;

    public PopupFragment(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_profile, container, false);

        dismissButton = view.findViewById(R.id.dismissButton);
        name = view.findViewById(R.id.name);
        surname = view.findViewById(R.id.surname);
        email = view.findViewById(R.id.email);
        address = view.findViewById(R.id.address);
        phone = view.findViewById(R.id.phone);
        profile_pic = view.findViewById(R.id.profilePic);

        new MyAsyncTask().execute();

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        profile_pic.setClickable(false);

        return view;
    }

    class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name.setText(snapshot.child("name").getValue().toString());
                        surname.setText(snapshot.child("surname").getValue().toString());
                        email.setText(snapshot.child("email").getValue().toString());
                        address.setText(snapshot.child("address").getValue().toString());
                        phone.setText(snapshot.child("phone").getValue().toString());

                        if (snapshot.child("image").exists()) {
                            Picasso.get().load(snapshot.child("image").getValue().toString()).noFade().placeholder(R.drawable.download).into(profile_pic);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return null;
        }
    }
}
