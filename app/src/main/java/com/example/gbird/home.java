package com.example.gbird;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class home extends AppCompatActivity {

    BottomNavigationView nav;

    FloatingActionButton mapButton, menuButton;
    TextView today;
    private RelativeLayout mainLayout;  // Your RelativeLayout

    Button addsighting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nav = findViewById(R.id.nav);
        addsighting = findViewById(R.id.addsighting);
        mapButton = findViewById(R.id.maplauncher);
        menuButton = findViewById(R.id.menuButton);
        // Find your TextView by its ID
        today = findViewById(R.id.today);


        // Initialize your layout and switch
        mainLayout = findViewById(R.id.homepage);  // Replace with the actual ID
        loadFirebaseThemeSetting();


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentt = new Intent(home.this, MapHotspots.class);
                startActivity(intentt);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // menu button to launch settings activity
                showPopup(view);
            }

            public void showPopup(View v) {
                PopupMenu popup = new PopupMenu(home.this, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.settings) {
                            // Handle the "Settings" menu item click, for example, launch your settings activity
                            Intent intentt = new Intent(home.this, SettingsPreferences.class);
                            startActivity(intentt);
                            return true;
                        } else if (menuItem.getItemId() == R.id.logout) {
                            // Handle the "Logout" menu item click, for example, perform logout actions
                            Intent intentt = new Intent(home.this, MainActivity.class);
                            startActivity(intentt);
                            finish();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                popup.inflate(R.menu.home_menu);
                popup.show();
            }


        });




        // Get the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Set the current date to the TextView
        today.setText(currentDate);


        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    Toast.makeText(home.this, "Home", Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.birdentry) {
                    Toast.makeText(home.this, "Bird Sightings", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(home.this, myBirds.class);
                    startActivity(intentt);
                } else if (itemId == R.id.explore) {
                    Toast.makeText(home.this, "Discover Birds in your region", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(home.this, discovery.class);
                    startActivity(intentt);
                } else if (itemId == R.id.record) {
                    Toast.makeText(home.this, "Record Bird Sounds", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(home.this, RecordAudio.class);
                    startActivity(intentt);
                } else {
                    // Handle other cases if needed
                }

                return true;
            }
        });

        addsighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, Editor.class);
                startActivity(intent);
            }
        });


    }

    private void loadFirebaseThemeSetting() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userSettingsRef = FirebaseDatabase.getInstance().getReference("user_settings").child(userId);
            // Assuming you have a color resource defined in colors.xml
            int white = ContextCompat.getColor(this, R.color.white);
            userSettingsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String customTheme = dataSnapshot.child("customTheme").getValue(String.class);
                        if (customTheme != null && customTheme.equals(UserSettings.DARK_THEME)) {
                            mainLayout.setBackgroundResource(R.drawable.xhomeblack);
                            mapButton.setBackgroundTintList(ContextCompat.getColorStateList(home.this, R.color.white));
                            Drawable drawable = mapButton.getDrawable();
                            DrawableCompat.setTint(drawable, ContextCompat.getColor(home.this, R.color.white));
                            today.setTextColor(getResources().getColor(R.color.white));
                        } else {
                            mainLayout.setBackgroundResource(R.drawable.xhome);
                            mapButton.setBackgroundTintList(ContextCompat.getColorStateList(home.this, R.color.black));
                            Drawable drawable = mapButton.getDrawable();
                            DrawableCompat.setTint(drawable, ContextCompat.getColor(home.this, R.color.white));
                            today.setTextColor(getResources().getColor(R.color.black));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        // Handle the back press event
        // You can show a confirmation dialog or simply open MainActivity
        Intent intent = new Intent(home.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}