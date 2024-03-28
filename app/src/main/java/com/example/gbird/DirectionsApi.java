package com.example.gbird;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionsApi {

    @GET("json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,  // e.g., driving, walking
            @Query("key") String apiKey
    );
}

