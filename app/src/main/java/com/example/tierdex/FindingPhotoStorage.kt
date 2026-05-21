package com.example.tierdex

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

const val STORAGE_URI_PREFIX = "storage://"
private const val FINDING_PHOTO_STORAGE_TAG = "FindingPhotoStorage"
private const val FINDING_PHOTO_MAX_DOWNLOAD_BYTES = 10L * 1024 * 1024
private const val FINDING_PHOTO_MAX_UPLOAD_BYTES = 5 * 1024 * 1024
private const val FINDING_PHOTO_MAX_EDGE_PX = 2560
private const val FINDING_PHOTO_JPEG_QUALITY = 88
private const val FINDING_THUMBNAIL_MAX_EDGE_PX = 800
private const val FINDING_THUMBNAIL_JPEG_QUALITY = 78
private const val FINDING_THUMBNAIL_MAX_UPLOAD_BYTES = 1024 * 1024
private const val PROFILE_PHOTO_MAX_EDGE_PX = 1600
private const val PROFILE_PHOTO_JPEG_QUALITY = 85
private const val FRIEND_FEED_THUMB_CACHE_DIR = "friend_feed_thumbs"
private const val REMOTE_FINDING_PHOTO_CACHE_DIR = "remote_finding_photos"
private const val JPEG_CONTENT_TYPE = "image/jpeg"

fun storageUriFromPath(path: String): String = "${STORAGE_URI_PREFIX}${path.trim()}"

fun storagePathFromUri(uriString: String): String? {
    val trimmedUri = uriString.trim()
    if (!trimmedUri.startsWith(STORAGE_URI_PREFIX)) return null

    return trimmedUri.removePrefix(STORAGE_URI_PREFIX)
        .trim()
        .takeIf { it.isNotBlank() }
}

object FindingPhotoStorageRepository {
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val jpegMetadata: StorageMetadata by lazy {
        StorageMetadata.Builder()
            .setContentType(JPEG_CONTENT_TYPE)
            .build()
    }

    private fun collectRemoteFindingStoragePaths(finding: AnimalFinding): List<String> {
        return buildList {
            add(finding.remotePhotoPath)
            addAll(finding.remotePhotoPaths)
            add(finding.thumbnailRemotePhotoPath)
        }.mapNotNull { rawValue ->
            val trimmedValue = rawValue.trim()
            when {
                trimmedValue.isBlank() -> null
                trimmedValue.startsWith("internal://", ignoreCase = true) -> null
                trimmedValue.startsWith("content://", ignoreCase = true) -> null
                trimmedValue.startsWith("file://", ignoreCase = true) -> null
                trimmedValue.startsWith("http://", ignoreCase = true) -> null
                trimmedValue.startsWith("https://", ignoreCase = true) -> null
                trimmedValue.startsWith("android.resource://", ignoreCase = true) -> null
                trimmedValue.startsWith(STORAGE_URI_PREFIX, ignoreCase = true) ->
                    storagePathFromUri(trimmedValue)
                else -> trimmedValue
            }
        }.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun normalizeOwnedFindingStoragePath(
        userId: String,
        rawPath: String
    ): String? {
        val trimmedUserId = userId.trim()
        if (trimmedUserId.isBlank()) return null

        val normalizedPath = when {
            rawPath.startsWith(STORAGE_URI_PREFIX, ignoreCase = true) ->
                storagePathFromUri(rawPath)

            rawPath.startsWith("internal://", ignoreCase = true) -> null
            rawPath.startsWith("content://", ignoreCase = true) -> null
            rawPath.startsWith("file://", ignoreCase = true) -> null
            rawPath.startsWith("http://", ignoreCase = true) -> null
            rawPath.startsWith("https://", ignoreCase = true) -> null
            rawPath.startsWith("android.resource://", ignoreCase = true) -> null
            else -> rawPath.trim()
        }?.trim()?.takeIf { it.isNotBlank() } ?: return null

        val requiredPrefix = "users/$trimmedUserId/findings/"
        return normalizedPath.takeIf { it.startsWith(requiredPrefix) }
    }

    private fun isFriendFeedThumbnailStoragePath(remotePhotoPath: String): Boolean {
        return remotePhotoPath.contains("thumb_photo", ignoreCase = true)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun localFriendThumbnailCacheFile(context: Context, remotePhotoPath: String): File {
        val cacheDir = File(context.filesDir, FRIEND_FEED_THUMB_CACHE_DIR)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "${sha256(remotePhotoPath.trim())}.jpg")
    }

    private fun localRemoteFindingPhotoCacheFile(context: Context, remotePhotoPath: String): File {
        val cacheDir = File(context.filesDir, REMOTE_FINDING_PHOTO_CACHE_DIR)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "${sha256(remotePhotoPath.trim())}.bin")
    }

    fun buildRemotePhotoPath(userId: String, finding: AnimalFinding): String {
        val documentId = FirestoreFindingRepository.documentIdForFinding(finding)
        return "users/$userId/findings/$documentId/photo.jpg"
    }

    fun buildRemotePhotoPath(userId: String, finding: AnimalFinding, photoIndex: Int): String {
        val documentId = FirestoreFindingRepository.documentIdForFinding(finding)
        return "users/$userId/findings/$documentId/photo_${photoIndex + 1}.jpg"
    }

    fun buildRemoteThumbnailPath(userId: String, finding: AnimalFinding): String {
        val documentId = FirestoreFindingRepository.documentIdForFinding(finding)
        return "users/$userId/findings/$documentId/thumb_photo.jpg"
    }

    fun buildProfilePhotoPath(userId: String): String {
        return "users/$userId/profile/photo.jpg"
    }

    fun buildProfileBackgroundPhotoPath(userId: String): String {
        return "users/$userId/profile/background.jpg"
    }

    suspend fun deleteFindingRemotePhotosBestEffort(
        userId: String,
        finding: AnimalFinding
    ) {
        val trimmedUserId = userId.trim()
        val remotePaths = collectRemoteFindingStoragePaths(finding)
        Log.d(
            "FindingPhotoCleanup",
            "cleanup start userIdPresent=${trimmedUserId.isNotBlank()} roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} remotePathCount=${remotePaths.size}"
        )

        if (trimmedUserId.isBlank() || remotePaths.isEmpty()) {
            Log.d(
                "FindingPhotoCleanup",
                "cleanup completed roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} deleted=0 skipped=${remotePaths.size}"
            )
            return
        }

        withContext(Dispatchers.IO) {
            remotePaths.forEach { remotePath ->
                Log.d(
                    "FindingPhotoCleanup",
                    "delete start roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} path=$remotePath"
                )
                runCatching {
                    Tasks.await(storage.reference.child(remotePath).delete())
                    Log.d(
                        "FindingPhotoCleanup",
                        "delete success roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} path=$remotePath"
                    )
                }.getOrElse { exception ->
                    val lowerMessage = exception.message.orEmpty().lowercase()
                    val fileMissing =
                        lowerMessage.contains("object does not exist") ||
                            lowerMessage.contains("not found") ||
                            lowerMessage.contains("no object exists")
                    if (fileMissing) {
                        Log.d(
                            "FindingPhotoCleanup",
                            "delete missing roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} path=$remotePath"
                        )
                    } else {
                        Log.w(
                            "FindingPhotoCleanup",
                            "delete error roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} path=$remotePath error=${exception.message ?: "Unbekannter Fehler"}",
                            exception
                        )
                    }
                }
            }
        }

        Log.d(
            "FindingPhotoCleanup",
            "cleanup completed roomId=${finding.roomId?.toString() ?: "-"} animalId=${finding.animalId} remotePathCount=${remotePaths.size}"
        )
    }

    fun collectOwnedFindingRemoteStoragePaths(
        userId: String,
        finding: AnimalFinding
    ): Set<String> {
        return collectRemoteFindingStoragePaths(finding)
            .mapNotNull { rawPath -> normalizeOwnedFindingStoragePath(userId, rawPath) }
            .toSet()
    }

    suspend fun deleteRemoteStoragePathsBestEffort(
        userId: String,
        remotePaths: Collection<String>
    ) {
        val trimmedUserId = userId.trim()
        if (trimmedUserId.isBlank()) {
            Log.d("FindingPhotoCleanup", "deleteRemotePaths skipped reason=blankUserId")
            return
        }

        val normalizedPaths = remotePaths
            .mapNotNull { rawPath -> normalizeOwnedFindingStoragePath(trimmedUserId, rawPath) }
            .distinct()
        if (normalizedPaths.isEmpty()) {
            Log.d("FindingPhotoCleanup", "deleteRemotePaths skipped reason=noOwnedFindingPaths")
            return
        }

        withContext(Dispatchers.IO) {
            normalizedPaths.forEach { remotePath ->
                runCatching {
                    Tasks.await(storage.reference.child(remotePath).delete())
                    Log.d("FindingPhotoCleanup", "deleteRemotePaths success path=$remotePath")
                }.getOrElse { exception ->
                    val lowerMessage = exception.message.orEmpty().lowercase()
                    val fileMissing =
                        lowerMessage.contains("object does not exist") ||
                            lowerMessage.contains("not found") ||
                            lowerMessage.contains("no object exists")
                    if (fileMissing) {
                        Log.d("FindingPhotoCleanup", "deleteRemotePaths missing path=$remotePath")
                    } else {
                        Log.w(
                            "FindingPhotoCleanup",
                            "deleteRemotePaths error path=$remotePath error=${exception.message ?: "Unbekannter Fehler"}",
                            exception
                        )
                    }
                }
            }
        }
    }

    suspend fun uploadFindingPhoto(
        context: Context,
        userId: String,
        finding: AnimalFinding
    ): String {
        return uploadFindingPhotos(context, userId, finding).firstOrNull()
            ?: finding.remotePhotoPath.trim()
    }

    suspend fun uploadFindingPhotos(
        context: Context,
        userId: String,
        finding: AnimalFinding
    ): List<String> {
        val localPhotoUris = effectiveLocalPhotoUris(finding)
        if (userId.isBlank() || localPhotoUris.isEmpty()) {
            return effectiveRemotePhotoPaths(finding)
        }

        return withContext(Dispatchers.IO) {
            localPhotoUris.mapIndexedNotNull { index, localPhotoUri ->
                val remotePhotoPath = if (index == 0) {
                    buildRemotePhotoPath(userId, finding)
                } else {
                    buildRemotePhotoPath(userId, finding, index)
                }
                val photoBytes = prepareUploadJpegBytes(
                    context = context,
                    localPhotoUri = localPhotoUri,
                    maxEdgePx = FINDING_PHOTO_MAX_EDGE_PX,
                    jpegQuality = FINDING_PHOTO_JPEG_QUALITY,
                    maxBytes = FINDING_PHOTO_MAX_UPLOAD_BYTES
                ) ?: return@mapIndexedNotNull null

                runCatching {
                    Tasks.await(
                        storage.reference.child(remotePhotoPath)
                            .putBytes(photoBytes, jpegMetadata)
                    )
                    remotePhotoPath
                }.getOrElse { exception ->
                    Log.e(
                        FINDING_PHOTO_STORAGE_TAG,
                        "Failed to upload finding photo $index to Storage: ${exception.message ?: "Unbekannter Fehler"}",
                        exception
                    )
                    null
                }
            }.take(3)
        }
    }

    suspend fun uploadFindingThumbnail(
        context: Context,
        userId: String,
        finding: AnimalFinding,
        onPrepared: ((Int) -> Unit)? = null
    ): String {
        val localPhotoUri = effectiveLocalPhotoUris(finding).firstOrNull().orEmpty().trim()
        if (userId.isBlank() || localPhotoUri.isBlank()) {
            return finding.thumbnailRemotePhotoPath.trim()
        }

        val remoteThumbnailPath = buildRemoteThumbnailPath(userId, finding)
        val thumbnailBytes = withContext(Dispatchers.IO) {
            runCatching {
                prepareUploadJpegBytes(
                    context = context,
                    localPhotoUri = localPhotoUri,
                    maxEdgePx = FINDING_THUMBNAIL_MAX_EDGE_PX,
                    jpegQuality = FINDING_THUMBNAIL_JPEG_QUALITY,
                    maxBytes = FINDING_THUMBNAIL_MAX_UPLOAD_BYTES
                )
            }.getOrElse { exception ->
                Log.w(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to prepare finding thumbnail bytes: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                null
            }
        } ?: return ""

        onPrepared?.invoke(thumbnailBytes.size / 1024)

        return withContext(Dispatchers.IO) {
            runCatching {
                Tasks.await(
                    storage.reference.child(remoteThumbnailPath)
                        .putBytes(thumbnailBytes, jpegMetadata)
                )
                remoteThumbnailPath
            }.getOrElse { exception ->
                Log.w(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to upload finding thumbnail to Storage: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                ""
            }
        }
    }

    suspend fun uploadFindingThumbnailFromRemoteOriginal(
        context: Context,
        userId: String,
        finding: AnimalFinding,
        remoteOriginalPhotoPath: String,
        onPrepared: ((Int) -> Unit)? = null
    ): String {
        val trimmedRemoteOriginalPhotoPath = remoteOriginalPhotoPath.trim()
        if (
            userId.isBlank() ||
            trimmedRemoteOriginalPhotoPath.isBlank() ||
            trimmedRemoteOriginalPhotoPath.contains("thumb_photo", ignoreCase = true)
        ) {
            return finding.thumbnailRemotePhotoPath.trim()
        }

        val remoteThumbnailPath = buildRemoteThumbnailPath(userId, finding)
        val thumbnailBytes = withContext(Dispatchers.IO) {
            runCatching {
                val remoteOriginalDownloadStartedAt = SystemClock.elapsedRealtime()
                val bitmap = loadCorrectlyOrientedBitmapFromStoragePath(
                    context = context,
                    remotePhotoPath = trimmedRemoteOriginalPhotoPath,
                    maxImageSizePx = FINDING_THUMBNAIL_MAX_EDGE_PX
                ) ?: return@runCatching null
                Log.d(
                    "FindingPhotoRepair",
                    "remoteThumbnailRepair remote original download durationMs=${SystemClock.elapsedRealtime() - remoteOriginalDownloadStartedAt}"
                )
                createThumbnailBytes(bitmap)
            }.getOrElse { exception ->
                Log.w(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to prepare finding thumbnail bytes from remote original: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                null
            }
        } ?: return ""

        onPrepared?.invoke(thumbnailBytes.size / 1024)

        return withContext(Dispatchers.IO) {
            runCatching {
                val thumbnailUploadStartedAt = SystemClock.elapsedRealtime()
                Tasks.await(
                    storage.reference.child(remoteThumbnailPath)
                        .putBytes(thumbnailBytes, jpegMetadata)
                )
                Log.d(
                    "FindingPhotoRepair",
                    "remoteThumbnailRepair thumbnail upload durationMs=${SystemClock.elapsedRealtime() - thumbnailUploadStartedAt}"
                )
                remoteThumbnailPath
            }.getOrElse { exception ->
                Log.w(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to upload finding thumbnail from remote original to Storage: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                ""
            }
        }
    }

    suspend fun uploadProfilePhoto(
        context: Context,
        userId: String,
        localPhotoUri: String,
        currentProfilePhotoPath: String = ""
    ): String {
        val trimmedPhotoUri = localPhotoUri.trim()
        if (userId.isBlank() || trimmedPhotoUri.isBlank()) {
            return currentProfilePhotoPath.trim()
        }

        val remotePhotoPath = buildProfilePhotoPath(userId)
        val photoBytes = withContext(Dispatchers.IO) {
            prepareUploadJpegBytes(
                context = context,
                localPhotoUri = trimmedPhotoUri,
                maxEdgePx = PROFILE_PHOTO_MAX_EDGE_PX,
                jpegQuality = PROFILE_PHOTO_JPEG_QUALITY,
                maxBytes = FINDING_PHOTO_MAX_UPLOAD_BYTES
            )
        } ?: return currentProfilePhotoPath.trim()

        return withContext(Dispatchers.IO) {
            runCatching {
                Tasks.await(
                    storage.reference.child(remotePhotoPath)
                        .putBytes(photoBytes, jpegMetadata)
                )
                remotePhotoPath
            }.getOrElse { exception ->
                Log.e(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to upload profile photo to Storage: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                currentProfilePhotoPath.trim()
            }
        }
    }

    suspend fun uploadProfileBackgroundPhoto(
        context: Context,
        userId: String,
        localPhotoUri: String,
        currentProfileBackgroundPhotoPath: String = ""
    ): String {
        val trimmedPhotoUri = localPhotoUri.trim()
        if (userId.isBlank() || trimmedPhotoUri.isBlank()) {
            return currentProfileBackgroundPhotoPath.trim()
        }

        val remotePhotoPath = buildProfileBackgroundPhotoPath(userId)
        val photoBytes = withContext(Dispatchers.IO) {
            prepareUploadJpegBytes(
                context = context,
                localPhotoUri = trimmedPhotoUri,
                maxEdgePx = PROFILE_PHOTO_MAX_EDGE_PX,
                jpegQuality = PROFILE_PHOTO_JPEG_QUALITY,
                maxBytes = FINDING_PHOTO_MAX_UPLOAD_BYTES
            )
        } ?: return currentProfileBackgroundPhotoPath.trim()

        return withContext(Dispatchers.IO) {
            runCatching {
                Tasks.await(
                    storage.reference.child(remotePhotoPath)
                        .putBytes(photoBytes, jpegMetadata)
                )
                remotePhotoPath
            }.getOrElse { exception ->
                Log.e(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to upload profile background photo to Storage: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                currentProfileBackgroundPhotoPath.trim()
            }
        }
    }

    fun loadFindingPhotoBytes(remotePhotoPath: String): ByteArray? {
        val trimmedPath = remotePhotoPath.trim()
        if (trimmedPath.isBlank()) return null
        val loadStartedAt = SystemClock.elapsedRealtime()
        val isThumbnail = trimmedPath.contains("thumb_photo", ignoreCase = true)
        Log.d(
            "FriendPhotoTiming",
            "storage download start isRemote=true isThumbnail=$isThumbnail pathPresent=${trimmedPath.isNotBlank()}"
        )

        return runCatching {
            Tasks.await(
                storage.reference
                    .child(trimmedPath)
                    .getBytes(FINDING_PHOTO_MAX_DOWNLOAD_BYTES)
            )
        }.onSuccess { bytes ->
            Log.d(
                "FriendPhotoTiming",
                "storage download success isThumbnail=$isThumbnail durationMs=${SystemClock.elapsedRealtime() - loadStartedAt} sizeKb=${bytes.size / 1024}"
            )
        }.getOrElse { exception ->
            Log.w(
                "FriendPhotoTiming",
                "storage download failed isThumbnail=$isThumbnail durationMs=${SystemClock.elapsedRealtime() - loadStartedAt} error=${exception.message ?: "Unbekannter Fehler"}"
            )
            Log.e(
                FINDING_PHOTO_STORAGE_TAG,
                "Failed to download finding photo from Storage: ${exception.message ?: "Unbekannter Fehler"}",
                exception
            )
            null
        }
    }

    fun loadFindingPhotoBytesCached(context: Context, remotePhotoPath: String): ByteArray? {
        val trimmedPath = remotePhotoPath.trim()
        if (trimmedPath.isBlank()) return null

        val cacheFile = localRemoteFindingPhotoCacheFile(context, trimmedPath)
        if (cacheFile.exists() && cacheFile.isFile) {
            val cachedBytes = runCatching { cacheFile.readBytes() }.getOrNull()
            if (cachedBytes != null) {
                return cachedBytes
            }
        }

        val downloadedBytes = loadFindingPhotoBytes(trimmedPath) ?: return null
        runCatching {
            cacheFile.writeBytes(downloadedBytes)
        }
        return downloadedBytes
    }

    fun loadFriendFeedThumbnailBytesCached(context: Context, remotePhotoPath: String): ByteArray? {
        val trimmedPath = remotePhotoPath.trim()
        if (trimmedPath.isBlank() || !isFriendFeedThumbnailStoragePath(trimmedPath)) return null

        val cacheStartedAt = SystemClock.elapsedRealtime()
        val cacheFile = localFriendThumbnailCacheFile(context, trimmedPath)
        if (cacheFile.exists() && cacheFile.isFile) {
            val cachedBytes = runCatching { cacheFile.readBytes() }.getOrNull()
            if (cachedBytes != null) {
                Log.d(
                    "FriendThumbCache",
                    "thumbnail cache hit sizeKb=${cachedBytes.size / 1024} durationMs=${SystemClock.elapsedRealtime() - cacheStartedAt}"
                )
                return cachedBytes
            }
        }

        Log.d(
            "FriendThumbCache",
            "thumbnail cache miss durationMs=${SystemClock.elapsedRealtime() - cacheStartedAt}"
        )
        val downloadStartedAt = SystemClock.elapsedRealtime()
        Log.d("FriendThumbCache", "storage download start")
        val downloadedBytes = runCatching {
            Tasks.await(
                storage.reference
                    .child(trimmedPath)
                    .getBytes(FINDING_PHOTO_MAX_DOWNLOAD_BYTES)
            )
        }.onSuccess { bytes ->
            Log.d(
                "FriendThumbCache",
                "storage download end success=true sizeKb=${bytes.size / 1024} durationMs=${SystemClock.elapsedRealtime() - downloadStartedAt}"
            )
        }.getOrElse { exception ->
            Log.w(
                "FriendThumbCache",
                "storage download end success=false durationMs=${SystemClock.elapsedRealtime() - downloadStartedAt} error=${exception.message ?: "Unbekannter Fehler"}"
            )
            null
        } ?: return null

        val writeStartedAt = SystemClock.elapsedRealtime()
        val writeSuccess = runCatching {
            cacheFile.writeBytes(downloadedBytes)
            true
        }.getOrElse {
            false
        }
        Log.d(
            "FriendThumbCache",
            "local file write success=$writeSuccess sizeKb=${downloadedBytes.size / 1024} durationMs=${SystemClock.elapsedRealtime() - writeStartedAt}"
        )
        return downloadedBytes
    }

    private fun readLocalPhotoBytes(context: Context, photoUri: String): ByteArray? {
        return try {
            when {
                photoUri.startsWith("internal://") -> {
                    val fileName = photoUri.removePrefix("internal://")
                    if (fileName.isBlank()) return null
                    val sourceFile = File(File(context.filesDir, "finding_images"), fileName)
                    if (!sourceFile.exists() || !sourceFile.isFile) {
                        null
                    } else {
                        sourceFile.readBytes()
                    }
                }

                else -> {
                    context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { input ->
                        input.readBytes()
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e(
                FINDING_PHOTO_STORAGE_TAG,
                "Failed to read local finding photo bytes: ${exception.message ?: "Unbekannter Fehler"}",
                exception
            )
            null
        }
    }

    private fun prepareUploadJpegBytes(
        context: Context,
        localPhotoUri: String,
        maxEdgePx: Int,
        jpegQuality: Int,
        maxBytes: Int
    ): ByteArray? {
        val bitmap = loadCorrectlyOrientedBitmapFromUriString(
            context = context,
            uriString = localPhotoUri,
            maxImageSizePx = maxEdgePx
        ) ?: return null

        val uploadBytes = createJpegBytes(
            sourceBitmap = bitmap,
            maxEdgePx = maxEdgePx,
            jpegQuality = jpegQuality
        ) ?: return null

        if (uploadBytes.size > maxBytes) {
            Log.w(
                FINDING_PHOTO_STORAGE_TAG,
                "Prepared JPEG exceeds upload limit: size=${uploadBytes.size} maxBytes=$maxBytes uri=$localPhotoUri"
            )
            return null
        }

        return uploadBytes
    }

    private fun createThumbnailBytes(sourceBitmap: Bitmap): ByteArray? {
        return createJpegBytes(
            sourceBitmap = sourceBitmap,
            maxEdgePx = FINDING_THUMBNAIL_MAX_EDGE_PX,
            jpegQuality = FINDING_THUMBNAIL_JPEG_QUALITY
        )
    }

    private fun createJpegBytes(
        sourceBitmap: Bitmap,
        maxEdgePx: Int,
        jpegQuality: Int
    ): ByteArray? {
        val scaledBitmap = scaleBitmapToMaxEdge(sourceBitmap, maxEdgePx)
        return runCatching {
            java.io.ByteArrayOutputStream().use { output ->
                scaledBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    jpegQuality,
                    output
                )
                output.toByteArray()
            }
        }.getOrNull()
    }

    private fun scaleBitmapToMaxEdge(bitmap: Bitmap, maxEdgePx: Int): Bitmap {
        val largestEdge = maxOf(bitmap.width, bitmap.height)
        if (largestEdge <= maxEdgePx) {
            return bitmap
        }

        val scaleFactor = maxEdgePx.toFloat() / largestEdge.toFloat()
        val targetWidth = (bitmap.width * scaleFactor).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * scaleFactor).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
