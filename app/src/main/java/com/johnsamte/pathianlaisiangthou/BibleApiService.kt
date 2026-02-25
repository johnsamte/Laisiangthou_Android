package com.johnsamte.pathianlaisiangthou

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BibleApiService {
    @GET("{query}")
    fun getVerses(
        @Path("query", encoded = true) query: String,
        @Query("translation") translation: String = "null"
    ): Call<BibleApiResponse>

}