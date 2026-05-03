package com.example.tierdex

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val STORAGE_URI_PREFIX = "storage://"
private const val FINDING_PHOTO_STORAGE_TAG = "FindingPhotoStorage"
private const val FINDING_PHOTO_MAX_DOWNLOAD_BYTES = 10L * 1024 * 1024

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

    fun buildProfilePhotoPath(userId: String): String {
        return "users/$userId/profile/photo.jpg"
    }

    suspend fun uploadFindingPhoto(
        context: Context,
        userId: String,
        finding: AnimalFinding
    ): String {
        val localPhotoUri = finding.photoUri.trim()
        if (userId.isBlank() || localPhotoUri.isBlank()) {
            return finding.remotePhotoPath.trim()
        }

        val remotePhotoPath = buildRemotePhotoPath(userId, finding)
        val photoBytes = withContext(Dispatchers.IO) {
            readLocalPhotoBytes(context, localPhotoUri)
        } ?: return finding.remotePhotoPath.trim()

        return withContext(Dispatchers.IO) {
            runCatching {
                Tasks.await(storage.reference.child(remotePhotoPath).putBytes(photoBytes))
                remotePhotoPath
            }.getOrElse { exception ->
                Log.e(
                    FINDING_PHOTO_STORAGE_TAG,
                    "Failed to upload finding photo to Storage: ${exception.message ?: "Unbekannter Fehler"}",
                    exception
                )
                finding.remotePhotoPath.trim()
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

        return runCatching {
            Tasks.await(
                storage.reference
                    .child(trimmedPath)
                    .getBytes(FINDING_PHOTO_MAX_DOWNLOAD_BYTES)
            )
        }.getOrElse { exception ->
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
}
