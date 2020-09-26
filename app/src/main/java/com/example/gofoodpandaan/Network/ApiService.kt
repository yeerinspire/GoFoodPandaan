package com.example.gofoodpandaan.Network

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("json")
    fun actionRoute (@Query("origin") origin:String,
                     @Query("destination") destinations:String,
                     @Query("key") key:String):Flowable<ResultRoute>

}