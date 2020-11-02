package com.example.gofoodpandaan.Model


data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}