package com.solari.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.widget.RemoteViews
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolariWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
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

    private suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val appContainer = (context.applicationContext as SolariApplication).appContainer
        val feedRepository = appContainer.feedRepository
        val userRepository = appContainer.userRepository

        val meResult = userRepository.getMe()
        val myId = (meResult as? ApiResult.Success)?.data?.id

        val feedResult = feedRepository.getFeed(limit = 20)
        val latestPost = if (feedResult is ApiResult.Success) {
            feedResult.data.posts.firstOrNull { it.author.id != myId }
        } else null

        val views = RemoteViews(context.packageName, R.layout.solari_widget_layout)

        // Click intent to open the app
        val intent = Intent(context, com.solari.app.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_post_image, pendingIntent)

        if (latestPost != null) {
            views.setTextViewText(R.id.widget_post_caption, latestPost.caption)
            
            val imageLoader = ImageLoader(context)
            
            // Load post image
            val postImageRequest = ImageRequest.Builder(context)
                .data(latestPost.imageUrl)
                .allowHardware(false)
                .build()
            val postImageResult = imageLoader.execute(postImageRequest)
            postImageResult.drawable?.let {
                val bitmap = drawableToBitmap(it)
                views.setImageViewBitmap(R.id.widget_post_image, bitmap)
            }

            // Load author avatar
            val avatarRequest = ImageRequest.Builder(context)
                .data(latestPost.author.profileImageUrl)
                .transformations(CircleCropTransformation())
                .allowHardware(false)
                .build()
            val avatarResult = imageLoader.execute(avatarRequest)
            avatarResult.drawable?.let {
                val bitmap = drawableToBitmap(it)
                views.setImageViewBitmap(R.id.widget_author_avatar, bitmap)
            }
        } else {
            views.setTextViewText(R.id.widget_post_caption, "No posts yet")
            views.setImageViewResource(R.id.widget_post_image, android.R.color.black)
            views.setImageViewResource(R.id.widget_author_avatar, android.R.color.transparent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 512
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}
