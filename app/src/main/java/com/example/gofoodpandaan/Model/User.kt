package com.example.gofoodpandaan.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class User(val name: String,
                val bio: String,
                val profilePicturePath: String?,
                val registrationTokens: MutableList<String>) {
    constructor(): this("", "", null, mutableListOf())
}