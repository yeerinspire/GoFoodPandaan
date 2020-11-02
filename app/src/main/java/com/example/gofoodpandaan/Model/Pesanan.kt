package com.example.gofoodpandaan.Model

class Pesanan {
    var kodeorder : String? = null
    var status : String? = null
    var uiddriver : String? = null
    var uidku : String? = null
    var latitudePenumpang : String? = null
    var longitudePenumpang : String? = null
    var latitudeTujuan : String? = null
    var longitudeTujuan: String? = null
    var latitudeToko: String? = null
    var longitudeToko: String? = null
    var namalokasi: String? = null
    var jarak: String? = null
    var gambar: String? = null
    var id: String? = null
    var harga: String? = null
    var namapenjual: String? = null
    var namaToko: String? = null
    var alamat_warung: String? = null
    var alamat_costumer: String? = null
    var alamat_tujuan: String? = null
    var penumpang: String? = null

    constructor(){

    }

    constructor(
        kodeorder: String?,
        status: String?,
        uiddriver: String?,
        uidku: String?,
        latitudePenumpang: String?,
        longitudePenumpang: String?,
        latitudeTujuan: String?,
        longitudeTujuan: String?,
        latitudeToko: String?,
        longitudeToko: String?,
        namalokasi: String?,
        jarak: String?,
        gambar: String?,
        id: String?,
        harga: String?,
        namapenjual: String?,
        namaToko: String?,
        alamat_warung: String?,
        alamat_costumer: String?,
        alamat_tujuan: String?,
        penumpang: String?
    ) {
        this.kodeorder = kodeorder
        this.status = status
        this.uiddriver = uiddriver
        this.uidku = uidku
        this.latitudePenumpang = latitudePenumpang
        this.longitudePenumpang = longitudePenumpang
        this.latitudeTujuan = latitudeTujuan
        this.longitudeTujuan = longitudeTujuan
        this.latitudeToko = latitudeToko
        this.longitudeToko = longitudeToko
        this.namalokasi = namalokasi
        this.jarak = jarak
        this.gambar = gambar
        this.id = id
        this.harga = harga
        this.namapenjual = namapenjual
        this.namaToko = namaToko
        this.alamat_warung = alamat_warung
        this.alamat_costumer = alamat_costumer
        this.alamat_tujuan = alamat_tujuan
        this.penumpang = penumpang
    }

}