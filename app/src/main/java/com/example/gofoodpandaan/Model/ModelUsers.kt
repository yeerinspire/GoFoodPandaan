package com.example.gofoodpandaan.Model
class ModelUsers {

    /// MOdel class
    var name : String? = null
    var email : String? = null
    var saldo : String? = null
    var photo : String? = null
    var nama : String? = null
    var postimage : String? = null
    var gambar:String? = null
    var id : String? = null
    var image: String? = null
    var password:String? = null
    var penjual:String? = null
    var alamat:String? = null
    var harga:String? = null
    var kode:String? = null
    var jumlah:String? = null
    var latitude:String? = null
    var longitude:String? = null
    var driver:String? = null
    constructor(){

    }

    constructor(email: String?,name : String?,saldo :String?,photo:String?,nama:String?,postimage:String?,gambar:String?,id:String?,image:String?,password:String?,penjual:String?,alamat:String?,harga:String?,kode:String?,jumlah:String?,latitude:String?,longitude:String?,driver:String?) {
        this.email = email
        this.name = name
        this.saldo = saldo
        this.photo = photo
        this.nama = nama
        this.postimage = postimage
        this.gambar = gambar
        this.id = id
        this.image = image
        this.password = password
        this.penjual = penjual
        this.alamat = alamat
        this.harga = harga
        this.kode = kode
        this.jumlah = jumlah
        this.latitude = latitude
        this.longitude = longitude
        this.driver = driver
    }
}