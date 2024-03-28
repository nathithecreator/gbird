package com.example.gbird;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Random;

public class discovery extends AppCompatActivity {

    BottomNavigationView nav;
    ImageView imageView;
    Button next;

    int[] intList = {R.drawable.aa, R.drawable.ab,R.drawable.ac,R.drawable.ad,R.drawable.ae,R.drawable.af,R.drawable.ag,R.drawable.ah,
            R.drawable.ai,R.drawable.aj,R.drawable.ak,R.drawable.al};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);


        nav = findViewById(R.id.nav);
        imageView = findViewById(R.id.myImage);

        Random random = new Random();
        int randomImage = random.nextInt(intList.length);
        imageView.setImageResource(intList[randomImage]);


        // Set the "Explore" tab as selected
        nav.setSelectedItemId(R.id.explore);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    Toast.makeText(discovery.this, "Home", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(discovery.this, home.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.birdentry) {
                    Toast.makeText(discovery.this, "Bird Entry", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(discovery.this, myBirds.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.explore) {
                    Random random = new Random();
                    int randomImage = random.nextInt(intList.length);
                    imageView.setImageResource(intList[randomImage]);
                } else if (itemId == R.id.record) {
                    Toast.makeText(discovery.this, "Record", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(discovery.this, RecordAudio.class);
                    startActivity(intentt);
                    finish();
                } else {
                    // Handle other cases if needed
                }

                return true;
            }
        });

    }
}
