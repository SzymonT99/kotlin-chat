package com.example.shoutbox
import retrofit2.Call
import retrofit2.http.*


interface JsonPlaceHolderAPI {

    @GET("shoutbox/messages")
    fun getPosts(): Call<MutableList<Post>>

    @POST("shoutbox/message")
    fun createPost(@Body post: Post): Call<Post>

    @PUT("shoutbox/message/{id}")
    fun putPost(@Path("id") id: String, @Body post: Post): Call<Post>

    @DELETE("shoutbox/message/{id}")
    fun deletePost(@Path("id") id: String): Call<Void>
}
