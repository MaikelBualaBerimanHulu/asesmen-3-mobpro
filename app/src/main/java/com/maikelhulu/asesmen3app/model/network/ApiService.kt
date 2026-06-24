package com.maikelhulu.asesmen3app.network

import com.maikelhulu.asesmen3app.model.ApiItem
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("images/search")
    suspend fun getItems(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 0
    ): List<ApiItem>
}