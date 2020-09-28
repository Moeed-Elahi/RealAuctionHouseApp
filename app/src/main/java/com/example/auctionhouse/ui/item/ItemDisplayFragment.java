package com.example.auctionhouse.ui.item;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.auctionhouse.Model.Products;
import com.example.auctionhouse.MyListing;
import com.example.auctionhouse.PopupFragment;
import com.example.auctionhouse.Prevalent.Prevalent;
import com.example.auctionhouse.R;
import com.example.auctionhouse.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mynameismidori.currencypicker.CurrencyPicker;
import com.mynameismidori.currencypicker.CurrencyPickerListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ItemDisplayFragment extends Fragment {

    private TextView chooseCurrency, productName, productDescription, productPrice, productTimeRemaining, lastBid;
    private EditText productNewBid;
    private String PID, timeDown, dateDown, userUpload;
    private ImageView productImage;
    private Button bidButton;
    private Button removeButton;
    private Toolbar toolbar;
    private Products products;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_item_display, container, false);

        root.setBackgroundColor(getResources().getColor(R.color.colorFragments));

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        toolbar = getActivity().findViewById(R.id.toolbar);

        productDescription = root.findViewById(R.id.product_description_details);
        productPrice = root.findViewById(R.id.product_price_details);
        productName = root.findViewById(R.id.product_name_details);
        productTimeRemaining = root.findViewById(R.id.time_remaining);
        productImage = root.findViewById(R.id.product_image_details);
        Button backButton = root.findViewById(R.id.button10);
        bidButton = root.findViewById(R.id.button11);
        productNewBid = root.findViewById(R.id.editTextNumberDecimal2);
        lastBid = root.findViewById(R.id.lastBid);
        lastBid.setTextColor(getResources().getColor(R.color.notUserBid));
        removeButton = root.findViewById(R.id.remove_product);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setTitle(R.string.menu_my_listing);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(ItemDisplayFragment.this);
                fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                fragmentTransaction.commit();
            }
        });

        Bundle bundle = this.getArguments();
        assert bundle != null;
        PID = bundle.getString("PID");

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Products").child(PID);

        chooseCurrency = root.findViewById(R.id.textView8);
        chooseCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CurrencyPicker picker = CurrencyPicker.newInstance("Select Currency");  // dialog title
                picker.setListener(new CurrencyPickerListener() {
                    @Override
                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                        chooseCurrency.setText(symbol);
                        picker.dismiss();
                    }
                });
                picker.show(getFragmentManager(), "CURRENCY_PICKER");
            }
        });

        new MyAsyncTaskProductDetails().execute();

        databaseReference.child("userUpload").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().toString().equals(Prevalent.currentOnlineUser.getEmail())) {
                    bidButton.setEnabled(false);
                    bidButton.setClickable(false);
                    bidButton.setVisibility(View.INVISIBLE);
                    bidButton.setVisibility(View.GONE);
                    removeButton.setClickable(true);
                    removeButton.setVisibility(View.VISIBLE);
                    chooseCurrency.setVisibility(View.INVISIBLE);
                    chooseCurrency.setVisibility(View.GONE);
                    productNewBid.setVisibility(View.INVISIBLE);
                    productNewBid.setVisibility(View.GONE);
                } else {
                    bidButton.setEnabled(true);
                    bidButton.setClickable(true);
                    bidButton.setVisibility(View.VISIBLE);
                    removeButton.setClickable(false);
                    removeButton.setVisibility(View.INVISIBLE);
                    removeButton.setVisibility(View.GONE);
                    chooseCurrency.setVisibility(View.VISIBLE);
                    productNewBid.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setMessage("Are you sure you want to remove your listing?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes, remove my listing", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        databaseReference.removeValue();
                        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Product Image").child(PID);
                        filePath.delete();
                        toolbar.setTitle(R.string.menu_my_listing);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.hide(ItemDisplayFragment.this);
                        fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                        fragmentTransaction.commit();

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

        bidButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("price", productNewBid.getText().toString());
                map.put("currency", chooseCurrency.getText().toString());
                map.put("userLastBid", Prevalent.currentOnlineUser.getEmail());
                databaseReference.updateChildren(map);

                Toast.makeText(getActivity(), "You bid on this item!", Toast.LENGTH_SHORT).show();
                productPrice.setText(productNewBid.getText().toString() + " " + chooseCurrency.getText().toString());
                productNewBid.getText().clear();

            }
        });

        lastBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Prevalent.currentOnlineUser.getEmail().equals(userUpload)) {
                    String name = lastBid.getText().toString().substring(13).replace(".", "_");
                    name = name.replace("@", "-");
                    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(name);
                    PopupFragment popup = new PopupFragment(dbref);
                    popup.show(getParentFragmentManager(), "PopUp");
                }
            }
        });

        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });

        return root;
    }

    private void setTimeRemaining() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        String dateString = dateDown;
        String timeString = timeDown;
        dateString = dateString.concat(" " + timeString);
        try {
            final Date date = sdf.parse(dateString);
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @SuppressLint("DefaultLocale")
                @Override
                public void run() {
                    handler.postDelayed(this, 1000);
                    try {
                        Date currentdate = new Date(); //2nd date taken as current date
                        if (!currentdate.after(date)) {
                            assert date != null;
                            long diff = date.getTime() - currentdate.getTime();
                            if (TimeUnit.MILLISECONDS.toDays(diff) == 0) {
                                if (TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff)) != 0) {
                                    productTimeRemaining.setText(String.format("Time remaining: %02d hours %02d minutes %02d seconds",
                                            TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff)),
                                            TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                                            TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                                } else {
                                    if (TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)) != 0) {
                                        productTimeRemaining.setText(String.format("Time remaining: %02d minutes %02d seconds",
                                                TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                                                TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                                    } else {
                                        productTimeRemaining.setText(String.format("Time remaining: %02d seconds",
                                                TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                                    }
                                }
                            } else {
                                productTimeRemaining.setText(String.format("Time remaining: %02d days %02d hours %02d minutes",
                                        TimeUnit.MILLISECONDS.toDays(diff),
                                        TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff)),
                                        TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff))));
                            }
                        } else {

                            if (!productTimeRemaining.getText().equals(R.string.auction_over)) {
                                productTimeRemaining.setText(R.string.auction_over);
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Products").child(PID);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("auction", "over");
                                databaseReference.updateChildren(map);
                                bidButton.setEnabled(false);

                                String[] chunks = lastBid.getText().toString().split(" ");

                                DatabaseReference productsUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
                                if (chunks[chunks.length - 1].equals(Prevalent.currentOnlineUser.getEmail()) && !chunks[chunks.length - 1].equals(userUpload)) {
                                    Prevalent.currentOnlineUser.setBoughtListings(Prevalent.currentOnlineUser.getBoughtListings() + 1);
                                }

                                productsUserRef.child(Prevalent.currentOnlineUser.getEmail());
                                HashMap<String, Object> mapU = new HashMap<>();
                                mapU.put("boughtListings", Prevalent.currentOnlineUser.getBoughtListings());
                                productsUserRef.child(Prevalent.currentOnlineUser.getEmail()).updateChildren(mapU);

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            handler.postDelayed(runnable, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void getProductDetails(String pid) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        productsRef.child(pid).addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "ResourceAsColor"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    products = snapshot.getValue(Products.class);

                    assert products != null;
                    productName.setText(products.getName());
                    toolbar.setTitle(products.getName());
                    productPrice.setText(products.getPrice() + " " + products.getCurrency());
                    productDescription.setText(products.getDescription());
                    timeDown = products.getTimeDown();
                    dateDown = products.getDateDown();
                    Picasso.get().load(products.getImage()).into(productImage);
                    userUpload = products.getUserUpload();
                    if (Prevalent.currentOnlineUser.getEmail().equals(products.getUserUpload())) {
                        String name = products.getUserLastBid().replace("_",".");
                        name = name.replace("-","@");
                        lastBid.setText("Last bid by: " + name);
                    } else {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(products.getUserUpload());
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String name = snapshot.child("surname").getValue().toString() + " " + snapshot.child("name").getValue().toString();
                                    lastBid.setText("Last bid: " + products.getPrice() + products.getCurrency() + ". Upload by: " +name);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    setTimeRemaining();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showImage() {
        final Dialog settingsDialog = new Dialog(getContext());
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.image_product
                , null));
        ImageView imageView = settingsDialog.findViewById(R.id.image_product);
        Picasso.get().load(products.getImage()).into(imageView);
        Button button = settingsDialog.findViewById(R.id.ok_button);
        settingsDialog.show();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsDialog.dismiss();
            }
        });
    }

    class MyAsyncTaskProductDetails extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            getProductDetails(PID);
            return null;
        }
    }
}
