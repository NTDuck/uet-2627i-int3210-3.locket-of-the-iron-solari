package com.solari.app.data.remote.conversation

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ConversationApi {
    @GET("conversations")
    suspend fun getConversations(
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): Response<GetConversationsResponseDto>

    @GET("conversations/{conversationId}")
    suspend fun getConversation(
        @Path("conversationId") conversationId: String
    ): Response<GetConversationResponseDto>

    @GET("conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): Response<GetMessagesResponseDto>

    @POST("conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequestDto
    ): Response<SendMessageResponseDto>

    @DELETE("conversations/{conversationId}/messages/{messageId}")
    suspend fun unsendMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String
    ): Response<UnsendMessageResponseDto>
}
