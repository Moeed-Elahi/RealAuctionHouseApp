package com.example.auctionhouse.ui.bought;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auctionhouse.Model.Products;
import com.example.auctionhouse.Prevalent.Prevalent;
import com.example.auctionhouse.R;
import com.example.auctionhouse.ViewHolder.ProductViewHolder;
import com.example.auctionhouse.ui.item.ItemDisplayFragment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BidListFragment extends Fragment {
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference productsRef;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_bought, container, false);

        root.setBackgroundColor(getResources().getColor(R.color.colorFragments));

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.show();

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);

        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        recyclerView = root.findViewById(R.id.recycler_menu1);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        new MyAsyncTask().execute();
    }

    class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            final FirebaseRecyclerOptions<Products> options = new FirebaseRecyclerOptions.Builder<Products>()
                    .setQuery(productsRef.orderByChild("userLastBid").equalTo(Prevalent.currentOnlineUser.getEmail()), Products.class).build();

            FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options) {
                @SuppressLint("SetTextI18n")
                @Override
                protected void onBindViewHolder(@NonNull final ProductViewHolder holder, int position, @NonNull final Products model) {
                    holder.productName.setText(model.getName());
                    holder.productPrice.setText("Last bid : " + model.getPrice() + " " + model.getCurrency() + " by " + model.getUserLastBid());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                    String dateString = model.getDateDown();
                    String timeString = model.getTimeDown();
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
                                        long diff = date.getTime() - currentdate.getTime();
                                        if (TimeUnit.MILLISECONDS.toDays(diff) == 0) {
                                            if (TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff)) != 0) {
                                                holder.productTimeRemaining.setText(String.format("Time remaining: %02d hours %02d minutes %02d seconds",
                                                        TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff)),
                                                        TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                                                        TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                                            } else {
                                                if (TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)) != 0) {
                                                    holder.productTimeRemaining.setText(String.format("Time remaining: %02d minutes %02d seconds",
                                                            TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                                                            TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                                                } else {
                                                    holder.productTimeRemaining.setText(String.format("Time remaining: %02d seconds",
                                                            TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))));
                                                }
                                            }
                                        } else {
                                            holder.productTimeRemaining.setText(String.format("Time remaining: %02d days %02d hours %02d minutes",
                                                    TimeUnit.MILLISECONDS.toDays(diff),
                                                    TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff)),
                                                    TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff))));
                                        }
                                    } else {
                                        if (date.getTime() - currentdate.getTime() == 0) {
                                            holder.productTimeRemaining.setText("Auction is over!");
                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Products").child(model.getPid());
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("auction", "over");
                                            databaseReference.updateChildren(map);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        handler.postDelayed(runnable,0);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Picasso.get().load(model.getImage()).into(holder.productImageView);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle bundle = new Bundle();
                            bundle.putString("PID", model.getPid());
                            ItemDisplayFragment itemDisplayFragment = new ItemDisplayFragment();
                            itemDisplayFragment.setArguments(bundle);
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.addToBackStack("xyz");
                            fragmentTransaction.hide(BidListFragment.this);
                            fragmentTransaction.replace(R.id.nav_host_fragment, itemDisplayFragment);
                            fragmentTransaction.commit();
                        }
                    });
                }

                @NonNull
                @Override
                public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);

                    ProductViewHolder holder = new ProductViewHolder(view);
                    return holder;
                }
            };

            recyclerView.setAdapter(adapter);
            adapter.startListening();
            return null;
        }
    }
}
