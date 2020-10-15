package com.example.gofoodpandaan.Model

class DataDriver {

    var email : String? = null
    var foto : String? = null
    var motor : String? = null
    var nama : String? = null
    var password : String? = null
    var platnomor : String? = null
    var statusOjek : String? = null

    constructor(){

    }

    constructor(
        email: String?,
        foto: String?,
        motor: String?,
        nama: String?,
        password: String?,
        platnomor: String?,
        statusOjek: String?
    ) {
        this.email = email
        this.foto = foto
        this.motor = motor
        this.nama = nama
        this.password = password
        this.platnomor = platnomor
        this.statusOjek = statusOjek
    }


}