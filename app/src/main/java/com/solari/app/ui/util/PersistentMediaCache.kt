package com.solari.app.ui.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Properties

private const val MediaCacheTtlMillis = 7L * 24L * 60L * 60L * 1000L
private const val FeedMediaCacheMaxBytes = 200L * 1024L * 1024L
private const val FeedThumbnailCacheMaxBytes = 100L * 1024L * 1024L

enum class PersistentMediaCacheKind(
    val directoryName: String,
    val maxBytes: Long
) {
    FeedMedia("feed-media", FeedMediaCacheMaxBytes),
    FeedThumbnail("feed-thumbnails", FeedThumbnailCacheMaxBytes)
}

object PersistentMediaCache {
    private val mutexes = PersistentMediaCacheKind.entries.associateWith { Mutex() }
    private val keyMutexes = mutableMapOf<String, Mutex>()
    private val memoryEntries = mutableMapOf<String, MemoryCacheEntry>()

    fun peekMemory(
        url: String,
        kind: PersistentMediaCacheKind
    ): Uri? {
        if (url.isBlank()) return null
        if (!url.isRemoteHttpUrl()) return url.toUri()

        val key = url.sha256()
        val now = System.currentTimeMillis()
        return readMemoryEntry(kind, key, now)?.let { Uri.fromFile(it.file) }
    }

    suspend fun resolve(
        context: Context,
        url: String,
        kind: PersistentMediaCacheKind
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            if (!url.isRemoteHttpUrl()) {
                return@runCatching url.toUri()
            }

            val appContext = context.applicationContext
            val directory = appContext.mediaCacheDirectory(kind)
            val key = url.sha256()
            val now = System.currentTimeMillis()
            val keyMutex = cacheKeyMutex(kind, key)

            keyMutex.withLock {
                mutexes.getValue(kind).withLock {
                    directory.mkdirs()
                    pruneExpired(directory, now)

                    findEntry(directory, key)?.takeIf { entry ->
                        now - entry.downloadedAtMillis <= MediaCacheTtlMillis && entry.file.exists()
                    }?.let { entry ->
                        entry.writeMetadata(lastAccessedAtMillis = now)
                        rememberMemoryEntry(kind, key, entry.file, entry.downloadedAtMillis)
                        return@withLock Uri.fromFile(entry.file)
                    }

                    deleteEntry(directory, key)
                }

                val downloadedFile = downloadToCache(directory, key, url, now)
                mutexes.getValue(kind).withLock {
                    pruneToMaxSize(directory, kind.maxBytes, protectedKey = key)
                    rememberMemoryEntry(kind, key, downloadedFile, now)
                    Uri.fromFile(downloadedFile)
                }
            }
        }
    }

    @Synchronized
    private fun cacheKeyMutex(kind: PersistentMediaCacheKind, key: String): Mutex {
        return keyMutexes.getOrPut("${kind.name}:$key") { Mutex() }
    }

    @Synchronized
    private fun readMemoryEntry(
        kind: PersistentMediaCacheKind,
        key: String,
        now: Long
    ): MemoryCacheEntry? {
        val memoryKey = memoryKey(kind, key)
        val entry = memoryEntries[memoryKey] ?: return null
        if (now - entry.downloadedAtMillis > MediaCacheTtlMillis || !entry.file.exists()) {
            memoryEntries.remove(memoryKey)
            return null
        }
        return entry
    }

    @Synchronized
    private fun rememberMemoryEntry(
        kind: PersistentMediaCacheKind,
        key: String,
        file: File,
        downloadedAtMillis: Long
    ) {
        memoryEntries[memoryKey(kind, key)] = MemoryCacheEntry(
            file = file,
            downloadedAtMillis = downloadedAtMillis
        )
    }

    private fun memoryKey(kind: PersistentMediaCacheKind, key: String): String {
        return "${kind.name}:$key"
    }
}

private data class MemoryCacheEntry(
    val file: File,
    val downloadedAtMillis: Long
)

private data class CacheEntry(
    val key: String,
    val file: File,
    val metadataFile: File,
    val downloadedAtMillis: Long,
    val lastAccessedAtMillis: Long
) {
    fun writeMetadata(lastAccessedAtMillis: Long = this.lastAccessedAtMillis) {
        Properties().apply {
            setProperty("fileName", file.name)
            setProperty("downloadedAtMillis", downloadedAtMillis.toString())
            setProperty("lastAccessedAtMillis", lastAccessedAtMillis.toString())
        }.store(metadataFile.outputStream(), null)
    }
}

private fun Context.mediaCacheDirectory(kind: PersistentMediaCacheKind): File {
    return File(cacheDir, "solari-persistent-media/${kind.directoryName}")
}

private fun downloadToCache(
    directory: File,
    key: String,
    url: String,
    now: Long
): File {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 45_000
        instanceFollowRedirects = true
    }

    try {
        val responseCode = connection.responseCode
        require(responseCode in 200..299) { "Media download failed with HTTP $responseCode." }

        val contentType = connection.contentType?.substringBefore(';')?.trim().orEmpty()
        val extension = contentType.toExtension()
            ?: url.substringBefore('?').substringAfterLast('.', missingDelimiterValue = "")
                .takeIf { it.length in 2..5 }
            ?: "bin"
        val destination = File(directory, "$key.$extension")
        val tempFile = File.createTempFile("$key-", ".download", directory)

        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (!tempFile.renameTo(destination)) {
            tempFile.copyTo(destination, overwrite = true)
            tempFile.delete()
        }

        CacheEntry(
            key = key,
            file = destination,
            metadataFile = File(directory, "$key.properties"),
            downloadedAtMillis = now,
            lastAccessedAtMillis = now
        ).writeMetadata()

        return destination
    } finally {
        connection.disconnect()
    }
}

private fun findEntry(directory: File, key: String): CacheEntry? {
    val metadataFile = File(directory, "$key.properties")
    if (!metadataFile.exists()) return null

    val properties = Properties().apply {
        metadataFile.inputStream().use(::load)
    }
    val fileName = properties.getProperty("fileName") ?: return null
    val downloadedAtMillis =
        properties.getProperty("downloadedAtMillis")?.toLongOrNull() ?: return null
    val lastAccessedAtMillis = properties.getProperty("lastAccessedAtMillis")?.toLongOrNull()
        ?: downloadedAtMillis
    val file = File(directory, fileName)

    return CacheEntry(
        key = key,
        file = file,
        metadataFile = metadataFile,
        downloadedAtMillis = downloadedAtMillis,
        lastAccessedAtMillis = lastAccessedAtMillis
    )
}

private fun pruneExpired(directory: File, now: Long) {
    directory.listFiles { file -> file.extension == "properties" }
        .orEmpty()
        .mapNotNull { metadataFile ->
            val key = metadataFile.nameWithoutExtension
            findEntry(directory, key)
        }
        .filter { now - it.downloadedAtMillis > MediaCacheTtlMillis }
        .forEach { deleteEntry(directory, it.key) }
}

private fun pruneToMaxSize(directory: File, maxBytes: Long, protectedKey: String) {
    val entries = directory.listFiles { file -> file.extension == "properties" }
        .orEmpty()
        .mapNotNull { findEntry(directory, it.nameWithoutExtension) }
        .filter { it.file.exists() }
    var totalBytes = entries.sumOf { it.file.length() }

    entries
        .filterNot { it.key == protectedKey }
        .sortedWith(compareBy<CacheEntry> { it.lastAccessedAtMillis }.thenBy { it.downloadedAtMillis })
        .forEach { entry ->
            if (totalBytes <= maxBytes) return
            totalBytes -= entry.file.length()
            deleteEntry(directory, entry.key)
        }
}

private fun deleteEntry(directory: File, key: String) {
    directory.listFiles()
        .orEmpty()
        .filter { file ->
            file.name == "$key.properties" || file.name.startsWith("$key.")
        }
        .forEach(File::delete)
}

private fun String.isRemoteHttpUrl(): Boolean {
    return startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
}

private fun String.toExtension(): String? {
    return MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(this)
        ?.takeIf { it.isNotBlank() }
}

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray(Charsets.UTF_8))
    return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
