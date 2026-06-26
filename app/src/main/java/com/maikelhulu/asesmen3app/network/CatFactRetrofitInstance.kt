package com.maikelhulu.asesmen3app.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object CatFactRetrofitInstance {
    val api: CatFactApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://catfact.ninja/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(CatFactApiService::class.java)
    }
}
