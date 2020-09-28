package com.example.auctionhouse.ui.slideshow;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.auctionhouse.Prevalent.Prevalent;
import com.example.auctionhouse.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SlideshowFragment extends Fragment{

    private CircleImageView profilePic;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profil, container, false);

        root.setBackgroundColor(getResources().getColor(R.color.colorFragments));

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        TextView name = root.findViewById(R.id.textView01);
        name.setText(Prevalent.currentOnlineUser.getName());

        TextView address = root.findViewById(R.id.textPostalAddress);
        address.setText(Prevalent.currentOnlineUser.getAddress());

        TextView email = root.findViewById(R.id.textView04);
        String e = Prevalent.currentOnlineUser.getEmail().replace("-","@").replace("_",".");
        email.setText(e);

        TextView phone = root.findViewById(R.id.textView03);
        phone.setText(Prevalent.currentOnlineUser.getPhone());

        TextView surname = root.findViewById(R.id.textView02);
        surname.setText(Prevalent.currentOnlineUser.getSurname());

        TextView addedListings = root.findViewById(R.id.textView05);
        TextView boughtListings = root.findViewById(R.id.textView06);

        profilePic = root.findViewById(R.id.profile_picture);
        Picasso.get().load(Prevalent.currentOnlineUser.getImage()).noFade().placeholder(R.drawable.download).into(profilePic);

        new MyAsyncTask().execute();

        addedListings.setText(addedListings.getText() + " " + Prevalent.currentOnlineUser.getUploadedListings());
        boughtListings.setText(boughtListings.getText() + " " +  Prevalent.currentOnlineUser.getBoughtListings());

        return root;
    }

    class MyAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getEmail());
            userRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        if (snapshot.child("image").exists()) {
                            String image = snapshot.child("image").getValue().toString();
                            Picasso.get().load(image).into(profilePic);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        if (snapshot.child("image").exists()) {
                            String image = snapshot.child("image").getValue().toString();
                            Picasso.get().load(image).into(profilePic);
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");
            Query query = productsRef.orderByChild("userUpload").equalTo(Prevalent.currentOnlineUser.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot child : snapshot.getChildren()) {
                                Prevalent.currentOnlineUser.setBoughtListings(Prevalent.currentOnlineUser.getBoughtListings() + 1);
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