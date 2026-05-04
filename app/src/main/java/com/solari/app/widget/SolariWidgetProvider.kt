package com.solari.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.solari.app.R
import com.solari.app.SolariApplication
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone

class SolariWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(
            "SolariWidgetUpdate",
            "SolariWidgetProvider.onUpdate triggered with ids: ${appWidgetIds.contentToString()}"
        )
        val pendingResult = goAsync()
        scope.launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d("SolariWidgetUpdate", "Updating specific widget $appWidgetId")
        val views = RemoteViews(context.packageName, R.layout.solari_widget_layout)

        // Set up click intent first so widget is interactive even while loading
        val intent = Intent(context, com.solari.app.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_post_image, pendingIntent)

        try {
            val appContainer = (context.applicationContext as SolariApplication).appContainer
            val feedRepository = appContainer.feedRepository
            val userRepository = appContainer.userRepository
            val userPreferencesStore = appContainer.userPreferencesStore

            val meResult = withContext(Dispatchers.IO) { userRepository.getMe() }
            if (meResult !is ApiResult.Success) {
                Log.d("SolariWidget", "User not logged in or error getting profile")
                views.setTextViewText(R.id.widget_post_caption, "Please log in to view posts")
                views.setViewVisibility(R.id.widget_post_caption, android.view.View.VISIBLE)
                views.setImageViewResource(R.id.widget_post_image, android.R.color.black)
                views.setImageViewResource(R.id.widget_author_avatar, android.R.color.transparent)
                views.setViewVisibility(R.id.widget_unseen_badge, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_streak_pill, android.view.View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val myId = meResult.data.id

            // Fetch current streak
            val streakResult =
                withContext(Dispatchers.IO) { userRepository.getCurrentStreak(TimeZone.getDefault().id) }
            val streak = (streakResult as? ApiResult.Success)?.data ?: 0

            if (streak > 0) {
                views.setViewVisibility(R.id.widget_streak_pill, android.view.View.VISIBLE)
                views.setTextViewText(R.id.widget_streak_text, "🔥 $streak")
            } else {
                views.setViewVisibility(R.id.widget_streak_pill, android.view.View.GONE)
            }

            // Get last viewed timestamp to calculate unseen posts
            val prefs = userPreferencesStore.userPreferencesFlow.first()
            val lastViewedTimestamp = prefs.lastFeedViewedTimestamp

            var unseenCount = 0
            var latestPost: Post? = null
            var nextCursor: String? = null

            do {
                val feedResult = withContext(Dispatchers.IO) {
                    feedRepository.getFeed(
                        limit = 20,
                        cursor = nextCursor
                    )
                }
                if (feedResult is ApiResult.Success) {
                    val posts = feedResult.data.posts
                    val friendPosts = posts.filter { it.author.id != myId }

                    unseenCount += friendPosts.count { it.timestamp > lastViewedTimestamp }

                    latestPost = friendPosts.firstOrNull()

                    nextCursor = feedResult.data.nextCursor
                    if (latestPost != null || nextCursor == null || posts.isEmpty()) {
                        break
                    }
                } else {
                    break
                }
            } while (true)

            if (unseenCount > 0) {
                views.setViewVisibility(R.id.widget_unseen_badge, android.view.View.VISIBLE)
                views.setTextViewText(R.id.widget_unseen_badge, unseenCount.toString())
            } else {
                views.setViewVisibility(R.id.widget_unseen_badge, android.view.View.GONE)
            }

            if (latestPost != null) {
                Log.d(
                    "SolariWidget",
                    "Found latest post: ${latestPost.id} from ${latestPost.author.username}"
                )
                if (latestPost.caption.isNotEmpty()) {
                    views.setTextViewText(R.id.widget_post_caption, latestPost.caption)
                    views.setViewVisibility(R.id.widget_post_caption, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_post_caption, android.view.View.GONE)
                }

                val imageLoader = ImageLoader(context)

                // Load post image - Resize to avoid Binder transaction limit (1MB)
                // 384 * 384 * 4 = ~576KB, safe for Binder
                val postImageRequest = ImageRequest.Builder(context)
                    .data(latestPost.imageUrl)
                    .size(384, 384)
                    .allowHardware(false)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build()

                val postImageResult =
                    withContext(Dispatchers.IO) { imageLoader.execute(postImageRequest) }
                postImageResult.drawable?.let {
                    val bitmap = it.toBitmap(384, 384, Bitmap.Config.ARGB_8888)
                    views.setImageViewBitmap(R.id.widget_post_image, bitmap)
                } ?: Log.e("SolariWidget", "Failed to load post image")

                // Load author avatar
                if (latestPost.author.profileImageUrl.isNullOrEmpty()) {
                    val letter = latestPost.author.username.firstOrNull()?.uppercase() ?: "?"
                    val bitmap = createDefaultAvatarBitmap(letter)
                    views.setImageViewBitmap(R.id.widget_author_avatar, bitmap)
                } else {
                    val avatarRequest = ImageRequest.Builder(context)
                        .data(latestPost.author.profileImageUrl)
                        .transformations(CircleCropTransformation())
                        .size(64, 64)
                        .allowHardware(false)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .build()

                    val avatarResult =
                        withContext(Dispatchers.IO) { imageLoader.execute(avatarRequest) }
                    avatarResult.drawable?.let {
                        val bitmap = it.toBitmap(64, 64, Bitmap.Config.ARGB_8888)
                        views.setImageViewBitmap(R.id.widget_author_avatar, bitmap)
                    } ?: Log.e("SolariWidget", "Failed to load avatar")
                }
            } else {
                Log.d("SolariWidget", "No posts found")
                views.setTextViewText(R.id.widget_post_caption, "No posts available")
                views.setViewVisibility(R.id.widget_post_caption, android.view.View.VISIBLE)
                views.setImageViewResource(R.id.widget_post_image, android.R.color.black)
                views.setImageViewResource(R.id.widget_author_avatar, android.R.color.transparent)
                views.setViewVisibility(R.id.widget_unseen_badge, android.view.View.GONE)
                views.setViewVisibility(R.id.widget_streak_pill, android.view.View.GONE)
            }
        } catch (e: Exception) {
            Log.e("SolariWidget", "Error updating widget", e)
            views.setViewVisibility(R.id.widget_post_caption, android.view.View.VISIBLE)
            views.setTextViewText(R.id.widget_post_caption, "Error loading widget")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createDefaultAvatarBitmap(letter: String): Bitmap {
        val size = 64
        val bitmap = createBitmap(size, size)
        val canvas = android.graphics.Canvas(bitmap)

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 32f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val fontMetrics = textPaint.fontMetrics
        val baseline = size / 2f + (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
        canvas.drawText(letter, size / 2f, baseline, textPaint)

        return bitmap
    }
}
