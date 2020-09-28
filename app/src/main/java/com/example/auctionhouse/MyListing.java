package com.example.auctionhouse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.auctionhouse.Prevalent.Prevalent;
import com.example.auctionhouse.ui.about.AboutFragment;
import com.example.auctionhouse.ui.add.AddFragment;
import com.example.auctionhouse.ui.bought.BidListFragment;
import com.example.auctionhouse.ui.gallery.GalleryFragment;
import com.example.auctionhouse.ui.home.HomeFragment;
import com.example.auctionhouse.ui.settings.SettingsFragment;
import com.example.auctionhouse.ui.slideshow.SlideshowFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class MyListing extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private boolean viewIsAtHome = false;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listing);

        SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayView(R.id.nav_add);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.open,R.string.close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.userName);
        TextView emailAddr = headerView.findViewById(R.id.userEmail);
        profileImage = headerView.findViewById(R.id.profile_image);

        userName.setText(Prevalent.currentOnlineUser.getName());
        String emailAdr = Prevalent.currentOnlineUser.getEmail();
        emailAdr = emailAdr.replace("-","@");
        emailAdr = emailAdr.replace("_",".");
        emailAddr.setText(emailAdr);

        setPic();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SlideshowFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, fragment);
                ft.commit();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Profile");
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        displayView(R.id.nav_home);

    }

    public void setPic() {
        Picasso.get().load(Prevalent.currentOnlineUser.getImage()).placeholder(R.drawable.download).into(profileImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_listing, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        displayView(menuItem.getItemId());
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        if (!viewIsAtHome) {
            displayView(R.id.nav_home);
        } else {
            moveTaskToBack(true);
        }
    }

    public void displayView(int viewId) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (viewId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                title = getString(R.string.menu_my_listing);
                viewIsAtHome = true;
                break;
            case R.id.nav_bids:
                fragment = new BidListFragment();
                title = getString(R.string.menu_bids);
                viewIsAtHome = false;
                break;
            case R.id.nav_add:
                fragment = new AddFragment();
                title = getString(R.string.menu_add);
                viewIsAtHome = false;
                break;
            case R.id.nav_browser:
                fragment = new GalleryFragment();
                title = getString(R.string.menu_browse_listings);
                viewIsAtHome = false;
                break;
            case R.id.nav_profil:
                fragment = new SlideshowFragment();
                title = getString(R.string.menu_profil);
                viewIsAtHome = false;
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                title = getString(R.string.menu_settings);
                viewIsAtHome = false;
                break;
            case R.id.nav_about:
                fragment = new AboutFragment();
                title = getString(R.string.menu_about);
                viewIsAtHome = false;
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, fragment);
            ft.commit();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void logout(MenuItem item) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage("Are you sure you want to log out?");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes, log out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Paper.book().destroy();

                Intent intent = new Intent(MyListing.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                MyListing.this.finish();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MyListing.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}