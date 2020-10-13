package com.example.gofoodpandaan.Model

import com.google.gson.annotations.SerializedName

class RequestNotification {
    @SerializedName("to")
    var token : String? = null
    @SerializedName("data")
    var sendNotificationModel : String? = null
}