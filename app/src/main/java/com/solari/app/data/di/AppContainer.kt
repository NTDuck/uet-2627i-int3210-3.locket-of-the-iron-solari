package com.solari.app.data.di

import android.content.Context
import androidx.room.Room
import com.solari.app.BuildConfig
import com.solari.app.data.auth.AuthInterceptor
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.auth.AuthSessionInvalidationNotifier
import com.solari.app.data.auth.DefaultAuthRepository
import com.solari.app.data.auth.TokenRefreshAuthenticator
import com.solari.app.data.websocket.WebSocketEventParser
import com.solari.app.data.websocket.WebSocketManager
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.conversation.DefaultConversationRepository
import com.solari.app.data.feed.DefaultFeedRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.friend.DefaultFriendRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.local.SolariDatabase
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.preferences.PushNotificationStore
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.remote.conversation.ConversationApi
import com.solari.app.data.remote.auth.AuthApi
import com.solari.app.data.remote.feed.FeedApi
import com.solari.app.data.remote.friend.FriendApi
import com.solari.app.data.remote.user.UserApi
import com.solari.app.data.security.TokenCipher
import com.solari.app.data.user.DefaultUserRepository
import com.solari.app.data.user.UserRepository
import com.solari.app.notifications.PushNotificationCoordinator
import com.solari.app.ui.viewmodels.SolariViewModelFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(
    context: Context
) {
    private val applicationContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val database = Room.databaseBuilder(
        applicationContext,
        SolariDatabase::class.java,
        "solari.db"
    )
        .addMigrations(SolariDatabase.Migration1To2)
        .build()

    private val tokenCipher = TokenCipher()
    private val recentEmojiStore = RecentEmojiStore(applicationContext)
    private val pushNotificationStore = PushNotificationStore(applicationContext)
    private val authSessionInvalidationNotifier = AuthSessionInvalidationNotifier()
    private val apiExecutor = ApiExecutor(json)

    private val refreshOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    private val refreshRetrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SOLARI_BACKEND_URL)
        .client(refreshOkHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val refreshAuthApi: AuthApi = refreshRetrofit.create(AuthApi::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .authenticator(
            TokenRefreshAuthenticator(
                authApi = refreshAuthApi,
                authSessionDao = database.authSessionDao(),
                apiExecutor = apiExecutor,
                tokenCipher = tokenCipher,
                sessionInvalidationNotifier = authSessionInvalidationNotifier
            )
        )
        .addInterceptor(AuthInterceptor(database.authSessionDao(), tokenCipher))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SOLARI_BACKEND_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    private val userApi: UserApi = retrofit.create(UserApi::class.java)
    private val feedApi: FeedApi = retrofit.create(FeedApi::class.java)
    private val friendApi: FriendApi = retrofit.create(FriendApi::class.java)
    private val conversationApi: ConversationApi = retrofit.create(ConversationApi::class.java)

    private val userRepository: UserRepository = DefaultUserRepository(
        userApi = userApi,
        apiExecutor = apiExecutor
    )

    val authRepository: AuthRepository = DefaultAuthRepository(
        authApi = authApi,
        authSessionDao = database.authSessionDao(),
        apiExecutor = apiExecutor,
        tokenCipher = tokenCipher,
        recentEmojiStore = recentEmojiStore,
        sessionInvalidationNotifier = authSessionInvalidationNotifier,
        pushNotificationStore = pushNotificationStore
    )

    private val feedRepository: FeedRepository = DefaultFeedRepository(
        feedApi = feedApi,
        apiExecutor = apiExecutor
    )

    private val friendRepository: FriendRepository = DefaultFriendRepository(
        friendApi = friendApi,
        apiExecutor = apiExecutor
    )

    private val conversationRepository: ConversationRepository = DefaultConversationRepository(
        conversationApi = conversationApi,
        apiExecutor = apiExecutor
    )

    private val webSocketEventParser = WebSocketEventParser(json)

    val webSocketManager = WebSocketManager(
        baseUrl = BuildConfig.SOLARI_BACKEND_URL,
        authSessionDao = database.authSessionDao(),
        tokenCipher = tokenCipher,
        json = json,
        eventParser = webSocketEventParser
    )

    private val postUploadCoordinator = PostUploadCoordinator(
        context = applicationContext,
        feedRepository = feedRepository,
        webSocketManager = webSocketManager
    )

    val pushNotificationCoordinator = PushNotificationCoordinator(
        context = applicationContext,
        pushNotificationStore = pushNotificationStore,
        authRepository = authRepository,
        userRepository = userRepository
    )

    val viewModelFactory = SolariViewModelFactory(
        authRepository = authRepository,
        userRepository = userRepository,
        feedRepository = feedRepository,
        friendRepository = friendRepository,
        conversationRepository = conversationRepository,
        recentEmojiStore = recentEmojiStore,
        postUploadCoordinator = postUploadCoordinator,
        webSocketManager = webSocketManager
    )
}
