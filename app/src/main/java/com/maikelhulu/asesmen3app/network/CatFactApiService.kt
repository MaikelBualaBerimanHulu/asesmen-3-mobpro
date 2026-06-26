package com.maikelhulu.asesmen3app.network

import com.maikelhulu.asesmen3app.model.CatFact
import retrofit2.http.GET

interface CatFactApiService {
    @GET("fact")
    suspend fun getRandomFact(): CatFact
}
