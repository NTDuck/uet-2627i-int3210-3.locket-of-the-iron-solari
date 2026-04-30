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

            val meResult = withContext(Dispatchers.IO) { userRepository.getMe() }
            val myId = (meResult as? ApiResult.Success)?.data?.id

            val feedResult = withContext(Dispatchers.IO) { feedRepository.getFeed(limit = 20) }
            val latestPost = if (feedResult is ApiResult.Success) {
                feedResult.data.posts.firstOrNull { it.author.id != myId }
            } else null

            if (latestPost != null) {
                views.setTextViewText(R.id.widget_post_caption, latestPost.caption)
                views.setViewVisibility(R.id.widget_post_caption, android.view.View.VISIBLE)
                
                val imageLoader = ImageLoader(context)
                
                // Load post image - Resize to avoid Binder transaction limit (1MB)
                val postImageRequest = ImageRequest.Builder(context)
                    .data(latestPost.imageUrl)
                    .size(512, 512) // Resize to reasonable widget size
                    .allowHardware(false)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build()
                
                val postImageResult = withContext(Dispatchers.IO) { imageLoader.execute(postImageRequest) }
                postImageResult.drawable?.let {
                    val bitmap = drawableToBitmap(it)
                    views.setImageViewBitmap(R.id.widget_post_image, bitmap)
                }

                // Load author avatar
                val avatarRequest = ImageRequest.Builder(context)
                    .data(latestPost.author.profileImageUrl)
                    .transformations(CircleCropTransformation())
                    .size(128, 128)
                    .allowHardware(false)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build()
                
                val avatarResult = withContext(Dispatchers.IO) { imageLoader.execute(avatarRequest) }
                avatarResult.drawable?.let {
                    val bitmap = drawableToBitmap(it)
                    views.setImageViewBitmap(R.id.widget_author_avatar, bitmap)
                }
            } else {
                views.setTextViewText(R.id.widget_post_caption, "No posts yet")
                views.setImageViewResource(R.id.widget_post_image, android.R.color.black)
                views.setImageViewResource(R.id.widget_author_avatar, android.R.color.transparent)
            }
        } catch (e: Exception) {
            Log.e("SolariWidget", "Error updating widget", e)
            views.setTextViewText(R.id.widget_post_caption, "Error loading data")
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
