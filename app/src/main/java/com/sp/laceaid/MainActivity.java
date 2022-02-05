package com.sp.laceaid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sp.laceaid.login.screen.LoginOptionsActivity;
import com.sp.laceaid.uiNavDrawer.AboutFragment;
import com.sp.laceaid.uiNavDrawer.ManualFragment;
import com.sp.laceaid.uiNavDrawer.ProfileFragment;
import com.sp.laceaid.uiNavDrawer.SettingFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar, toolbar2;
    private ImageView arrow;

    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String userID;

    private ConstraintLayout constraintLayout, constraintLayout2;
    private boolean isDrawerFragOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // hooks
        constraintLayout = findViewById(R.id.constraintMain);
        constraintLayout2 = findViewById(R.id.constraintMain2);
        arrow = findViewById(R.id.arrow1);

        arrow.setOnClickListener(v->{
            isDrawerFragOpen = false;
            constraintLayout2.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
        });


        // <<< BOTTOM NAVIGATION BAR >>>
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations (no (back?) up-arrow) (?)
        // only needed if app has toolbar / actionbar
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.arFragment, R.id.runRecorderFragment)
                .build();

        // mainActivity fragment layout
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);



        // <<< NAVIGATION DRAWER >>>
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toolbar2 = findViewById(R.id.toolbar2);

        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        userID = user.getUid();

        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar2);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); // hide title

        // navigation drawer menu
        navigationView.bringToFront();
        View headerView = navigationView.getHeaderView(0);
        TextView nvName = headerView.findViewById(R.id.tvnh_name);
        TextView nvEmail = headerView.findViewById(R.id.tvnh_email);

        // update name and email in nav drawer when user load (first time + only once)
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userprofile = snapshot.getValue(User.class);

                if(userprofile != null) {
                    String name = userprofile.getFirstName();
                    String email = userprofile.getEmail();
                    nvName.setText(name);
                    nvEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Cannot update profile", Toast.LENGTH_LONG).show();
            }
        });

        // set toggle fn when burger icon is clicked
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            /** Called when a drawer has settled in a completely closed state.*/
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //Toast.makeText(MainActivity.this, "drawer closed", Toast.LENGTH_LONG).show();
            }

            /** Called when a drawer has settled in a completely open state.*/
            // When drawer is opened again, this function will check if there is a change of
            // email/name in db and will update them respectively
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Toast.makeText(MainActivity.this, "drawer opened", Toast.LENGTH_LONG).show();

                //databaseReference.child(userID).child(email)...?
                databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User userprofile = snapshot.getValue(User.class);

                        if(userprofile != null) {
                            String name = userprofile.getFirstName();
                            String email = userprofile.getEmail();
                            nvName.setText(name);
                            nvEmail.setText(email);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Cannot update profile", Toast.LENGTH_LONG).show();
                    }
                });

            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    // menu options in navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        constraintLayout.setVisibility(View.GONE);
        constraintLayout2.setVisibility(View.VISIBLE);
        isDrawerFragOpen = true;

        switch (item.getItemId()) {
            case R.id.nav_profile:
                // use .replace because there's already one fragment view on home (otherwise use .add)
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2, new ProfileFragment()).commit();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.nav_setting:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2, new SettingFragment()).commit();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.nav_manual:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2, new ManualFragment()).commit();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView2, new AboutFragment()).commit();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginOptionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            default:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }else if (isDrawerFragOpen) {
            isDrawerFragOpen = false;
            constraintLayout2.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}