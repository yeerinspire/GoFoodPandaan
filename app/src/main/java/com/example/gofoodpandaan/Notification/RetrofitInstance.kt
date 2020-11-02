package com.example.gofoodpandaan.Notification

import com.example.gofoodpandaan.Constan.BaseUrlFcm
import com.example.gofoodpandaan.Constan.CONTENT_TYPE
import com.example.gofoodpandaan.Constan.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class RetrofitInstance {

    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BaseUrlFcm)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}