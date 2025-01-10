package com.vvadu.iot_project

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NetworkHelper(private val client: OkHttpClient) {

    interface NetworkCallback {
        fun onSuccess(response: String)
        fun onFailure(error: IOException)
    }

    // GET request
    fun getRequest(url: String, callback: NetworkCallback) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    callback.onSuccess(it)
                } ?: callback.onFailure(IOException("Empty Response"))
            }
        })
    }

    // POST request
    fun postRequest(url: String, jsonBody: String, callback: NetworkCallback) {
        val body = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    callback.onSuccess(it)
                } ?: callback.onFailure(IOException("Empty Response"))
            }
        })
    }
}