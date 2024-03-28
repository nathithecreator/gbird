package com.example.gbird;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Editor extends AppCompatActivity {

    private MaterialCardView SelectImage;
    private ImageView ImageBirdView;
    private Uri ImageUri;
    private Uri mImageUri;
    private Bitmap bitmap;
    private ProgressBar mProgressBar;
    private String photoUrl;

    private static final int PICK_IMAGE_REQUEST = 1;

    private StorageTask mUploadTask;

    private final static int REQUEST_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView latitude, longitude, address, city, country;
    TextView eBirdName, eBirdCount, eNotes, eDate;

    private Button saveButton;

    private FloatingActionButton goBackButton;
    SwitchMaterial currentLocationSelector;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        eBirdName = findViewById(R.id.birdname);
        eBirdCount = findViewById(R.id.countbird);
        eNotes = findViewById(R.id.notes);
        eDate = findViewById(R.id.date);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        ImageBirdView = findViewById(R.id.imagebird);
        SelectImage = findViewById(R.id.selectImageBird);
        saveButton = findViewById(R.id.save);
        goBackButton = findViewById(R.id.goback);
        mProgressBar = findViewById(R.id.progress_bar);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        currentLocationSelector = findViewById(R.id.currentLocationSelector); // Get a reference to the switch

        mStorageRef = FirebaseStorage.getInstance().getReference("BirdSightings");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("BirdSightings");


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(Editor.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isLocationEmpty = currentLocationSelector.isChecked() &&
                            (latitude.getText().toString().isEmpty() || longitude.getText().toString().isEmpty() ||
                                    address.getText().toString().isEmpty() || city.getText().toString().isEmpty() ||
                                    country.getText().toString().isEmpty());

                    boolean isTextFieldsEmpty = eBirdName.getText().toString().isEmpty() ||
                            eBirdCount.getText().toString().isEmpty() ||
                            eNotes.getText().toString().isEmpty() ||
                            eDate.getText().toString().isEmpty();

                    if (isLocationEmpty) {
                        Toast.makeText(Editor.this, "Turn on the switch", Toast.LENGTH_SHORT).show();
                    } else if (isTextFieldsEmpty) {
                        Toast.makeText(Editor.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadFile();
                    }
                }
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Editor.this, home.class);
                startActivity(intent);
            }
        });



        currentLocationSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    getLastLocation();
                } else {
                    Toast.makeText(Editor.this, "Switch on the switch", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermission();
            }
        });

        FloatingActionButton fab = findViewById(R.id.goback);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set an OnClickListener for eDate
        eDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Set the current date to eDate
        setCurrentDate();
    }

    private void setCurrentDate() {
        // Get the current date and format it
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Set the formatted date to the eDate EditText
        eDate.setText(currentDate);
    }

    private void showDatePickerDialog() {
        // Get the current date components
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Handle the selected date
                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                eDate.setText(selectedDate);
            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData(); // Update mImageUri
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri); // Use mImageUri here
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mImageUri != null) {
                ImageBirdView.setImageBitmap(bitmap);
            }
        }
    }








    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // ... (previous code)

                            // Use getDownloadUrl() to get the download URL
                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    String birdName = eBirdName.getText().toString().trim();
                                    String birdCount = eBirdCount.getText().toString().trim();
                                    String notes = eNotes.getText().toString().trim();
                                    String date = eDate.getText().toString().trim();
                                    double latitude = Double.parseDouble(Editor.this.latitude.getText().toString().trim());
                                    double longitude = Double.parseDouble(Editor.this.longitude.getText().toString().trim());
                                    String addressStr = address.getText().toString().trim();
                                    String cityStr = city.getText().toString().trim();
                                    String countryStr = country.getText().toString().trim();

                                    // Create a new birdModel object with text data and image URL
                                    birdModel newBird = new birdModel(
                                            birdName,
                                            birdCount,
                                            notes,
                                            date,
                                            latitude,
                                            longitude,
                                            addressStr,
                                            cityStr,
                                            countryStr,
                                            downloadUrl
                                    );

                                    // Upload the birdModel object to Firebase
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(newBird)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Database upload success
                                                    Toast.makeText(Editor.this, "Upload Successful", Toast.LENGTH_LONG).show();
                                                    // Navigate to Home.class on successful upload
                                                    Intent intent = new Intent(Editor.this, home.class);
                                                    startActivity(intent);
                                                    finish(); // Finish the current activity
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Database upload failed
                                                    Toast.makeText(Editor.this, "Database Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle upload failure for the image
                            Toast.makeText(Editor.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }




    // LOCATION METHOD
    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(Editor.this, Locale.getDefault());
                                List<Address> addresses = null;

                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    latitude.setText(String.valueOf(addresses.get(0).getLatitude()));
                                    longitude.setText(String.valueOf(addresses.get(0).getLongitude()));
                                    address.setText(addresses.get(0).getAddressLine(0));
                                    city.setText(addresses.get(0).getLocality());
                                    country.setText(addresses.get(0).getCountryName());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        } else {
            askPermission();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Please provide required permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                selectImageFromGallery();
            }
        } else {
            selectImageFromGallery();
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }





}
