package com.solari.app.data.feed

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.solari.app.data.network.ApiResult
import com.solari.app.data.websocket.WebSocketEvent
import com.solari.app.data.websocket.WebSocketManager
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.models.User
import com.solari.app.ui.util.PreparedPostMedia
import com.solari.app.ui.util.preparePostMediaForUpload
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostUploadCoordinatorTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val context = mockk<Context>(relaxed = true)
    private val feedRepository = mockk<FeedRepository>()
    private val webSocketManager = mockk<WebSocketManager>()
    private val wsEvents = MutableSharedFlow<WebSocketEvent>()

    @Before
    fun setup() {
        mockkStatic("com.solari.app.ui.util.PostMediaPreparationKt")
        every { webSocketManager.events } returns wsEvents
        
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri
        every { mockUri.toString() } returns "content://media/1"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `startUpload follows flow initiate-upload-finalize`() = runTest {
        val coordinator = PostUploadCoordinator(
            context = context,
            feedRepository = feedRepository,
            webSocketManager = webSocketManager,
            scope = backgroundScope
        )
        val media = CapturedMedia(uri = mockk(), isVideo = false, contentType = "image/jpeg")
        val preparedMedia = PreparedPostMedia(
            previewUri = mockk(),
            contentType = "image/jpeg",
            bytes = byteArrayOf(1, 2, 3),
            width = 100,
            height = 100,
            isVideo = false
        )
        val session = PostUploadSession(postId = "post-123", objectKey = "key", uploadUrl = "url")

        every { preparePostMediaForUpload(any(), any()) } returns Result.success(preparedMedia)
        coEvery { feedRepository.initiatePostUpload(any()) } returns ApiResult.Success(session)
        coEvery { feedRepository.uploadPostBinary(any(), any(), any()) } returns ApiResult.Success(Unit)
        coEvery { feedRepository.finalizePostUpload(any(), any()) } returns ApiResult.Success(Unit)

        coordinator.uploads.test {
            awaitItem() // initial

            coordinator.startUpload(
                media = media,
                caption = "Test caption",
                isPublic = true,
                selectedFriendIds = emptySet()
            )

            // draft added -> updateDraft -> bindServerPostId -> markProcessing
            var processing = awaitItem()
            while (processing.isEmpty() || processing[0].draft.uploadStatus != PostUploadStatus.Processing) {
                processing = awaitItem()
            }
            
            assertEquals(PostUploadStatus.Processing, processing[0].draft.uploadStatus)
            assertEquals("post-123", processing[0].serverPostId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `upload error marks draft as failed`() = runTest {
        val coordinator = PostUploadCoordinator(
            context = context,
            feedRepository = feedRepository,
            webSocketManager = webSocketManager,
            scope = backgroundScope
        )
        val media = CapturedMedia(uri = mockk(), isVideo = false, contentType = "image/jpeg")
        val preparedMedia = PreparedPostMedia(
            previewUri = mockk(),
            contentType = "image/jpeg",
            bytes = byteArrayOf(1, 2, 3),
            width = 100,
            height = 100,
            isVideo = false
        )

        every { preparePostMediaForUpload(any(), any()) } returns Result.success(preparedMedia)
        coEvery { feedRepository.initiatePostUpload(any()) } returns ApiResult.Failure(null, null, "Network error")

        coordinator.uploads.test {
            awaitItem() // initial

            coordinator.startUpload(
                media = media,
                caption = "Test caption",
                isPublic = true,
                selectedFriendIds = emptySet()
            )

            var failed = awaitItem()
            while (failed.isEmpty() || failed[0].draft.uploadStatus != PostUploadStatus.Failed) {
                failed = awaitItem()
            }
            
            assertEquals(PostUploadStatus.Failed, failed[0].draft.uploadStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `receiving POST_PROCESSED replaces draft with real post`() = runTest {
        val coordinator = PostUploadCoordinator(
            context = context,
            feedRepository = feedRepository,
            webSocketManager = webSocketManager,
            scope = backgroundScope
        )
        val media = CapturedMedia(uri = mockk(), isVideo = false, contentType = "image/jpeg")
        val preparedMedia = PreparedPostMedia(
            previewUri = mockk(),
            contentType = "image/jpeg",
            bytes = byteArrayOf(1, 2, 3),
            width = 100,
            height = 100,
            isVideo = false
        )
        val session = PostUploadSession(postId = "post-123", objectKey = "key", uploadUrl = "url")
        val remotePost = mockk<Post>()
        every { remotePost.id } returns "post-123"
        every { remotePost.copy(uploadStatus = any(), uploadError = any()) } returns remotePost

        every { preparePostMediaForUpload(any(), any()) } returns Result.success(preparedMedia)
        coEvery { feedRepository.initiatePostUpload(any()) } returns ApiResult.Success(session)
        coEvery { feedRepository.uploadPostBinary(any(), any(), any()) } returns ApiResult.Success(Unit)
        coEvery { feedRepository.finalizePostUpload(any(), any()) } returns ApiResult.Success(Unit)
        coEvery { feedRepository.getPost("post-123") } returns ApiResult.Success(remotePost)

        coordinator.uploads.test {
            awaitItem() // initial

            coordinator.startUpload(
                media = media,
                caption = "Test caption",
                isPublic = true,
                selectedFriendIds = emptySet()
            )

            var processing = awaitItem()
            while (processing.isEmpty() || processing[0].draft.uploadStatus != PostUploadStatus.Processing) {
                processing = awaitItem()
            }

            wsEvents.emit(WebSocketEvent.PostProcessed(postId = "post-123", status = "ready"))

            var completed = awaitItem()
            while (completed.isEmpty() || completed[0].draft.uploadStatus != PostUploadStatus.None) {
                completed = awaitItem()
            }
            
            assertEquals(PostUploadStatus.None, completed[0].draft.uploadStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
