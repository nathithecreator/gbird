package com.example.gbird;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private MaterialCardView SelectImage;
    private ImageView ImageBirdView;
    private Uri ImageUri;
    private Uri mImageUri;
    private Bitmap bitmap;
    private ProgressBar mProgressBar;
    private String photoUrl;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    private FloatingActionButton goBackButton;

    TextView latitude, longitude, address, city, country;
    TextView eBirdName, eBirdCount, eNotes, eDate;

    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        eBirdName = findViewById(R.id.birdname);
        eBirdCount = findViewById(R.id.countbird);
        eNotes = findViewById(R.id.notes);
        eDate = findViewById(R.id.date);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        goBackButton = findViewById(R.id.goback);
        ImageBirdView = findViewById(R.id.imagebird);
        SelectImage = findViewById(R.id.selectImageBird);
        saveButton = findViewById(R.id.save);

        // Retrieve data from the Intent
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String gBirdnames = intent.getStringExtra("Birdnames");
        String gBirdCounts = intent.getStringExtra("BirdCounts");
        String gBirdNotes = intent.getStringExtra("BirdNotes");
        String gDate = intent.getStringExtra("Date");
        double gBirdLatitudes = intent.getDoubleExtra("BirdLatitudes", 0.0); // 0.0 is a default value if not found
        double gBirdLongitudes = intent.getDoubleExtra("BirdLongitudes", 0.0);
        String gBirdAddress = intent.getStringExtra("BirdAddress");
        String gBirdCity = intent.getStringExtra("BirdCity");
        String gBirdCountry = intent.getStringExtra("BirdCountry");

        // Load the image and text
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerInside()
                .into(ImageBirdView);

        eBirdName.setText(gBirdnames);
        eBirdCount.setText(gBirdCounts);
        eNotes.setText(gBirdNotes);
        eDate.setText(gDate);
        latitude.setText(String.valueOf(gBirdLatitudes));
        longitude.setText(String.valueOf(gBirdLongitudes));
        address.setText(gBirdAddress);
        city.setText(gBirdCity);
        country.setText(gBirdCountry);


        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, myBirds.class);
                startActivity(intent);
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, myBirds.class);
                startActivity(intent);
                finish();
            }
        });
    }
}