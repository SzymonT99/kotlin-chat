package com.example.shoutbox

class Post {
    private var content: String? = null
    private var login: String? = null
    private var date: String? = null
    private var id: String? = null

    constructor(_content: String, _login: String){
        content = _content
        login = _login
    }

    fun getContent(): String? {
        return content
    }

    fun getLogin(): String? {
        return login
    }

    fun getDate(): String? {
        return date
    }

    fun getId(): String? {
        return id
    }

}