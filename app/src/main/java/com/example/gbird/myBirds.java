package com.example.gbird;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class myBirds extends AppCompatActivity implements Adapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private ProgressBar mProgressCircle;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<birdModel> mUploads;

    BottomNavigationView nav;


    private static final String TAG = "myBirds"; // Define a tag for logging


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_birds);

        nav = findViewById(R.id.nav);

        // Set the "Explore" tab as selected
        nav.setSelectedItemId(R.id.birdentry);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    Toast.makeText(myBirds.this, "Home", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(myBirds.this, home.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.birdentry) {
                    Toast.makeText(myBirds.this, "Bird Entry", Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.explore) {
                    Intent intentt = new Intent(myBirds.this, discovery.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.record) {
                    Intent intentt = new Intent(myBirds.this, RecordAudio.class);
                    startActivity(intentt);
                    finish();
                } else {
                    // Handle other cases if needed
                }

                return true;
            }
        });

        Log.d(TAG, "onCreate"); // Log onCreate event

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();

        mAdapter = new Adapter(myBirds.this, mUploads);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(myBirds.this);

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference("BirdSightings");

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange"); // Log onDataChange event

                mUploads.clear(); // Clear the list before adding new data
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    birdModel upload = postSnapshot.getValue(birdModel.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mAdapter.notifyDataSetChanged();

                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage()); // Log onCancelled event with error message
                Toast.makeText(myBirds.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDBListener != null) {
            mDatabaseRef.removeEventListener(mDBListener);
            Log.d(TAG, "onDestroy: Removed ValueEventListener"); // Log when ValueEventListener is removed
        }
    }

    @Override
    public void onItemClick(int position) {

        // Handle card item click here
        birdModel selectedItem = mUploads.get(position);

        // Pass data to the next activity using an Intent
        Intent intent = new Intent(myBirds.this, DetailActivity.class);
        intent.putExtra("imageUrl", selectedItem.getPhotoUrl());
        intent.putExtra("Birdnames", selectedItem.getBirdnames());
        intent.putExtra("BirdCounts", selectedItem.getBirdCounts());
        intent.putExtra("BirdNotes", selectedItem.getBirdNotes());
        intent.putExtra("Date", selectedItem.getDate());
        intent.putExtra("BirdLatitudes", selectedItem.getBirdLatitudes());
        intent.putExtra("BirdLongitudes", selectedItem.getBirdLongitudes());
        intent.putExtra("BirdAddress", selectedItem.getBirdAddress());
        intent.putExtra("BirdCity", selectedItem.getBirdCity());
        intent.putExtra("BirdCountry", selectedItem.getBirdCountry());
        startActivity(intent);
    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this, "Whatever Position is: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        birdModel selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();

        FirebaseStorage storage = FirebaseStorage.getInstance(); // Initialize FirebaseStorage
        StorageReference imageRef = storage.getReferenceFromUrl(selectedItem.getPhotoUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(myBirds.this, "Item deleted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Item deleted at position: " + position); // Log item deletion
            }
        });
    }

    @Override
    public void onPositionClick(int position) {

    }

    @Override
    public int getPosition() {

        return 0;
    }
}