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
    var foto:String? = null
    var keterangan:String? = null
    var statusrating:Boolean? = null
    var ratingdriver:String? = null
    var telefon:String? = null
    var hargatotal:Int? = null


    constructor(){

    }

    constructor(
        name: String?,
        email: String?,
        saldo: String?,
        photo: String?,
        nama: String?,
        postimage: String?,
        gambar: String?,
        id: String?,
        image: String?,
        password: String?,
        penjual: String?,
        alamat: String?,
        harga: String?,
        kode: String?,
        jumlah: String?,
        latitude: String?,
        longitude: String?,
        driver: String?,
        foto: String?,
        keterangan: String?,
        statusrating: Boolean?,
        ratingdriver: String?,
        telefon: String?,
        hargatotal: Int?
    ) {
        this.name = name
        this.email = email
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
        this.foto = foto
        this.keterangan = keterangan
        this.statusrating = statusrating
        this.ratingdriver = ratingdriver
        this.telefon = telefon
        this.hargatotal = hargatotal
    }


}