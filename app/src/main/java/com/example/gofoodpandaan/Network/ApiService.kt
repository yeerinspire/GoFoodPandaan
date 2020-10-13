package com.example.gofoodpandaan.Network

import com.example.gofoodpandaan.Model.RequestNotification
import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.lang.reflect.Type

interface ApiService {
    @GET("json")
    fun actionRoute (@Query("origin") origin:String,
                     @Query("destination") destinations:String,
                     @Query("key") key:String):Flowable<ResultRoute>

    @Headers(
        "Authorization: key=AAAA1B1Ef7o:APA91bFzLzKZ3jrez02VjjiQ4sGDoUwDc6TyAph-8pfzfc-ifchkr8iYnFkpjXCK08LJhros4EbSvsvDdvzhy1fX5gNNGuqDIhQR8_vend8V6UEWh4LkMeNwYZy2-EM8izC2xLbrF0v2",
        "Content-Type:application/json"
    )

    @POST("fcm/send")
    fun sendChatNotification(@Body requestNotification: RequestNotification): Call<ResponseBody>
}