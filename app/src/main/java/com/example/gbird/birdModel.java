package com.example.gbird;

import com.google.firebase.database.Exclude;

public class birdModel {

    private String Birdnames, BirdCounts, BirdNotes, Date, BirdAddress, BirdCity, BirdCountry, PhotoUrl;
    private double BirdLatitudes, BirdLongitudes;

    private String mKey;

    private birdModel() {
        // Default constructor required for calls to DataSnapshot.getValue(Upload.class)
    }

    public birdModel(String birdnames, String birdCounts, String birdNotes, String date, double birdLatitudes, double birdLongitudes, String birdAddress, String birdCity, String birdCountry, String photoUrl) {
        Birdnames = birdnames;
        BirdCounts = birdCounts;
        BirdNotes = birdNotes;
        Date = date;
        BirdLatitudes = birdLatitudes;
        BirdLongitudes = birdLongitudes;
        BirdAddress = birdAddress;
        BirdCity = birdCity;
        BirdCountry = birdCountry;
        PhotoUrl = photoUrl;
    }

    public String getBirdnames() {
        return Birdnames;
    }

    public void setBirdnames(String birdnames) {
        Birdnames = birdnames;
    }

    public String getBirdCounts() {
        return BirdCounts;
    }

    public void setBirdCounts(String birdCounts) {
        BirdCounts = birdCounts;
    }

    public String getBirdNotes() {
        return BirdNotes;
    }

    public void setBirdNotes(String birdNotes) {
        BirdNotes = birdNotes;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public double getBirdLatitudes() {
        return BirdLatitudes;
    }

    public void setBirdLatitudes(double birdLatitudes) {
        BirdLatitudes = birdLatitudes;
    }

    public double getBirdLongitudes() {
        return BirdLongitudes;
    }

    public void setBirdLongitudes(double birdLongitudes) {
        BirdLongitudes = birdLongitudes;
    }

    public String getBirdAddress() {
        return BirdAddress;
    }

    public void setBirdAddress(String birdAddress) {
        BirdAddress = birdAddress;
    }

    public String getBirdCity() {
        return BirdCity;
    }

    public void setBirdCity(String birdCity) {
        BirdCity = birdCity;
    }

    public String getBirdCountry() {
        return BirdCountry;
    }

    public void setBirdCountry(String birdCountry) {
        BirdCountry = birdCountry;
    }

    public String getPhotoUrl() {
        return PhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        PhotoUrl = photoUrl;
    }


    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        this.mKey = key; // Use "this" to refer to the class field
    }
}
