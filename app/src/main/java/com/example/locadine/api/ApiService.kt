package com.example.locadine.api

import com.example.locadine.pojos.OpenAIRequest
import com.example.locadine.pojos.OpenAIResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("v1/chat/completions")
    fun sendMessageToOpenAI(@Body body: OpenAIRequest): Call<OpenAIResponse>
}