package com.vvadu.iot_project

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("filtered-data") // Ganti 'filtered-data' dengan path endpoint yang sesuai
    fun getFilteredData(
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): Call<List<SensorData>>
}