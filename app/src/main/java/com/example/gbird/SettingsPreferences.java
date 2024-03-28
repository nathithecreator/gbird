package com.example.gbird;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsPreferences extends AppCompatActivity {

    private View parentView;
    private SwitchMaterial themeSwitch, metricSwitch;
    private TextView themeTV, titleTV, Modelabel, metricTV, Metriclabel;

    private UserSettings settings;
    private DatabaseReference userSettingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_preferences);

        // Initialize Firebase components
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userSettingsRef = FirebaseDatabase.getInstance().getReference("user_settings").child(userId);
        }

        // Initialize UI components
        initWidgets();

        // Initialize UserSettings
        settings = (UserSettings) getApplication();

        // Load settings from Firebase
        loadFirebaseSettings();

        // Set listeners for UI components
        setListeners();
    }

    private void setListeners() {
        // Listener for the theme switch
        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                String theme = checked ? UserSettings.DARK_THEME : UserSettings.LIGHT_THEME;
                settings.setCustomTheme(theme);
                saveFirebaseSettings();
                updateView();
            }
        });

        // Listener for the metric switch
        metricSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                String units = checked ? UserSettings.MILES : UserSettings.KILOMETERS;
                settings.setUnitsPreference(units);
                saveFirebaseSettings();
                updateMetricView();
            }
        });

        // Listener for the goBackButton
        FloatingActionButton goBackButton = findViewById(R.id.gohome);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserSettingsToFirebase();
                Intent intent = new Intent(SettingsPreferences.this, home.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initWidgets() {
        themeTV = findViewById(R.id.themeTV);
        titleTV = findViewById(R.id.titleTV);
        parentView = findViewById(R.id.parentView);
        metricTV = findViewById(R.id.metricTV);
        Metriclabel = findViewById(R.id.Metriclabel);
        Modelabel = findViewById(R.id.Modelabel);
        themeSwitch = findViewById(R.id.themeSwitch);
        metricSwitch = findViewById(R.id.metricSwitch);
        //ImageView backgroundImageView = findViewById(R.id.xhome);

    }

    private void loadFirebaseSettings() {
        if (userSettingsRef != null) {
            userSettingsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String customTheme = dataSnapshot.child("customTheme").getValue(String.class);
                        String unitsPreference = dataSnapshot.child("unitsPreference").getValue(String.class);

                        if (customTheme != null) {
                            settings.setCustomTheme(customTheme);
                            updateView();
                        }

                        if (unitsPreference != null) {
                            settings.setUnitsPreference(unitsPreference);
                            updateMetricView();
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

    private void saveFirebaseSettings() {
        if (userSettingsRef != null) {
            userSettingsRef.child("customTheme").setValue(settings.getCustomTheme());
            userSettingsRef.child("unitsPreference").setValue(settings.getUnitsPreference());
        }
    }

    private void updateView() {
        final int black = ContextCompat.getColor(this, R.color.black);
        final int white = ContextCompat.getColor(this, R.color.white);

        if (settings.getCustomTheme().equals(UserSettings.DARK_THEME)) {
            titleTV.setTextColor(white);
            themeTV.setTextColor(white);
            Modelabel.setTextColor(white);
            metricTV.setTextColor(white);
            Metriclabel.setTextColor(white);
            themeTV.setText("Dark");
            parentView.setBackgroundColor(black);
            themeSwitch.setChecked(true);
        } else {
            titleTV.setTextColor(black);
            themeTV.setTextColor(black);
            Modelabel.setTextColor(black);
            metricTV.setTextColor(black);
            Metriclabel.setTextColor(black);
            themeTV.setText("Light");
            parentView.setBackgroundColor(white);
            themeSwitch.setChecked(false);
        }
    }

    private void updateMetricView() {
        if (settings.getUnitsPreference().equals(UserSettings.KILOMETERS)) {
            metricTV.setText("KILOMETER");
            metricSwitch.setChecked(false);
        } else {
            metricTV.setText("MILES");
            metricSwitch.setChecked(true);
        }
    }

    private void saveUserSettingsToFirebase() {
        if (userSettingsRef != null) {
            userSettingsRef.child("customTheme").setValue(settings.getCustomTheme());
            userSettingsRef.child("unitsPreference").setValue(settings.getUnitsPreference());
        }
    }
}
