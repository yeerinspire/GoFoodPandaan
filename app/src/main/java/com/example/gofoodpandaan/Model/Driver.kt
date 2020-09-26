package com.example.gofoodpandaan.Model

class Driver {

    var latitude : String? = null
    var longitude : String? = null
    var name : String? = null

    constructor(latitude: String?, longitude: String?, name: String?) {
        this.latitude = latitude
        this.longitude = longitude
        this.name = name
    }

    constructor()
}