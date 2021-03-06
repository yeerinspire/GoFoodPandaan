package com.alfanshter.udinlelangfix.Session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context?) {
    val privateMode = 0
    val privateName ="login"
    var Pref : SharedPreferences?=context?.getSharedPreferences(privateName,privateMode)
    var editor : SharedPreferences.Editor?=Pref?.edit()

    private val islogin = "login"
    fun setLogin(check: Boolean){
        editor?.putBoolean(islogin,check)
        editor?.commit()
    }

    fun getLogin():Boolean?
    {
        return Pref?.getBoolean(islogin,false)
    }


    private val iduser = "iduser"
    fun setiduser(check:String)
    {
        editor?.putString(iduser,check)
        editor?.commit()
    }

    fun getiduser():String?
    {
        return Pref?.getString(iduser,"")
    }

    private val logicwarung = "logicwarung"
    fun setlogicwarung(check:String)
    {
        editor?.putString(logicwarung,check)
        editor?.commit()
    }

    fun getlogicwarung():String?
    {
        return Pref?.getString(logicwarung,"")
    }



    private val telefon = "telefon"
    fun settelefon(check:String)
    {
        editor?.putString(telefon,check)
        editor?.commit()
    }

    fun gettelefon():String?
    {
        return Pref?.getString(telefon,"")
    }




    private val status = "status"
    fun setstatuslelang(check:String?)
    {
        editor?.putString(status,check)
        editor?.commit()
    }

    fun getstatuslelang():String?
    {
        return Pref?.getString(status,"")
    }


    private val isloginadmin = "loginadmin"
    fun setLoginadmin(check: Boolean){
        editor?.putBoolean(isloginadmin,check)
        editor?.commit()
    }

    fun getLoginadmin():Boolean?
    {
        return Pref?.getBoolean(isloginadmin,false)
    }

    private val logicmaps = "status"
    fun setlogicmaps(check:String?)
    {
        editor?.putString(logicmaps,check)
        editor?.commit()
    }

    fun getlogicmaps():String?
    {
        return Pref?.getString(logicmaps,"")
    }

    private val harga = "harga"
    fun setharga(harga:String?)
    {
        editor?.putString(logicmaps,harga)
        editor?.commit()
    }

    fun getharga():String?
    {
        return Pref?.getString(harga,"")
    }



}