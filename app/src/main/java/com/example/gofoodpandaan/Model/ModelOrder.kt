package com.example.gofoodpandaan.Model

import java.util.*

class ModelOrder {

    /// MOdel class
    var driver : String? = null
    var harga : String? = null
    var jarak : String? = null
    var latAwal : Double? = null
    var latTujuan : Double? = null
    var lokasiAwal : String? = null
    var lokasiTujuan : String? = null
    var status : Int? = null
    var tanggal : String? = null
    var uid : String? = null
    var calender : String? = null
    var namatoko : String? = null
    var gambar : String? = null
    var idtoko : String? = null


    constructor(){

    }

    constructor(
        driver: String?,
        harga: String?,
        jarak: String?,
        latAwal: Double?,
        latTujuan: Double?,
        lokasiAwal: String?,
        lokasiTujuan: String?,
        status: Int?,
        tanggal: String?,
        uid: String?,
        calender: String?,
        namatoko: String?,
        gambar: String?
    ) {
        this.driver = driver
        this.harga = harga
        this.jarak = jarak
        this.latAwal = latAwal
        this.latTujuan = latTujuan
        this.lokasiAwal = lokasiAwal
        this.lokasiTujuan = lokasiTujuan
        this.status = status
        this.tanggal = tanggal
        this.uid = uid
        this.calender = calender
        this.namatoko = namatoko
        this.gambar = gambar
    }


}