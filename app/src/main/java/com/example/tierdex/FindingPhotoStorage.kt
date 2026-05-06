package com.example.tierdex

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val STORAGE_URI_PREFIX = "storage://"
private const val FINDING_PHOTO_STORAGE_TAG = "FindingPhotoStorage"
private const val FINDING_PHOTO_MAX_DOWNLOAD_BYTES = 10L * 1024 * 1024
private const val FINDING_THUMBNAIL_MAX_EDGE_PX = 800
private const val FINDING_THUMBNAIL_JPEG_QUALITY = 78

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
                val photoBytes = readLocalPhotoBytes(context, localPhotoUri) ?: return@mapIndexedNotNull null

                runCatching {
                    Tasks.await(storage.reference.child(remotePhotoPath).putBytes(photoBytes))
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
                val bitmap = loadCorrectlyOrientedBitmapFromUriString(
                    context = context,
                    uriString = localPhotoUri,
                    maxImageSizePx = FINDING_THUMBNAIL_MAX_EDGE_PX
                ) ?: return@runCatching null
                createThumbnailBytes(bitmap)
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
                Tasks.await(storage.reference.child(remoteThumbnailPath).putBytes(thumbnailBytes))
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
            readLocalPhotoBytes(context, trimmedPhotoUri)
        } ?: return currentProfilePhotoPath.trim()

        return withContext(Dispatchers.IO) {
            runCatching {
                Tasks.await(storage.reference.child(remotePhotoPath).putBytes(photoBytes))
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

    private fun createThumbnailBytes(sourceBitmap: Bitmap): ByteArray? {
        val scaledBitmap = scaleBitmapToMaxEdge(sourceBitmap, FINDING_THUMBNAIL_MAX_EDGE_PX)
        return runCatching {
            java.io.ByteArrayOutputStream().use { output ->
                scaledBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    FINDING_THUMBNAIL_JPEG_QUALITY,
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
