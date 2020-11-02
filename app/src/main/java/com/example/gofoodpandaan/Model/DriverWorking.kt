package com.example.gofoodpandaan.Model

class DriverWorking {

    var driver : String? = null
    var namatoko : String? = null
    var penumpang : String? = null
    var latitudePenumpang : String? = null
    var longitudePenumpang : String? = null
    var latitudeToko : String? = null
    var longitudeToko : String? = null
    var latitudeDriver : String? = null
    var longitudeDriver : String? = null
    var statusPerjalanan : String? = null
    var status : String? = null
    var telfondriver : String? = null
    var uid : String? = null
    var harga : String? = null
    var ongkir : String? = null
    var jarak : String? = null
    var hargapelanggan : String? = null

    constructor(){

    }

    constructor(
        driver: String?,
        namatoko: String?,
        penumpang: String?,
        latitudePenumpang: String?,
        longitudePenumpang: String?,
        latitudeToko: String?,
        longitudeToko: String?,
        latitudeDriver: String?,
        longitudeDriver: String?,
        statusPerjalanan: String?,
        status: String?,
        telfondriver: String?,
        uid: String?,
        harga: String?,
        ongkir: String?,
        jarak: String?,
        hargapelanggan: String?
    ) {
        this.driver = driver
        this.namatoko = namatoko
        this.penumpang = penumpang
        this.latitudePenumpang = latitudePenumpang
        this.longitudePenumpang = longitudePenumpang
        this.latitudeToko = latitudeToko
        this.longitudeToko = longitudeToko
        this.latitudeDriver = latitudeDriver
        this.longitudeDriver = longitudeDriver
        this.statusPerjalanan = statusPerjalanan
        this.status = status
        this.telfondriver = telfondriver
        this.uid = uid
        this.harga = harga
        this.ongkir = ongkir
        this.jarak = jarak
        this.hargapelanggan = hargapelanggan
    }
}