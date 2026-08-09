package com.example.tierdex

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val BACKUP_JSON_FILE_NAME = "findings.json"
private const val BACKUP_ZIP_FILE_NAME = "tierdex_backup.zip"
private const val LEGACY_BACKUP_JSON_FILE_NAME = "tierdex_backup.json"
private const val BACKUP_IMAGES_DIR = "images/"
private const val BACKUP_FINDING_IMAGES_DIR = "finding_images"

private data class BackupAnimalFindingDto(
    val roomId: Int? = null,
    val findingId: String? = null,
    val animalId: String = "",
    val date: String = "",
    val location: String = "",
    val note: String = "",
    val photoUri: String = "",
    val remotePhotoPath: String = "",
    val thumbnailRemotePhotoPath: String = "",
    val photoUris: List<String> = emptyList(),
    val remotePhotoPaths: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String? = null,
    val ownerId: String? = null,
    val taggedFriendIds: List<String>? = null
)

data class BackupImportResult(
    val findings: List<AnimalFinding>,
    val success: Boolean,
    val message: String
)

fun shareBackup(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val mimeType = if (file.extension.equals("zip", ignoreCase = true)) {
        "application/zip"
    } else {
        "application/json"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Backup teilen"))
}

fun exportFindings(context: Context, findings: List<AnimalFinding>): File {
    val json = Gson().toJson(findings)
    val backupFile = File(context.getExternalFilesDir(null), BACKUP_ZIP_FILE_NAME)
    val imagesDir = File(context.filesDir, BACKUP_FINDING_IMAGES_DIR)

    ZipOutputStream(backupFile.outputStream().buffered()).use { zip ->
        zip.putNextEntry(ZipEntry(BACKUP_JSON_FILE_NAME))
        zip.write(json.toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        findings.flatMap { finding ->
            effectiveLocalPhotoUris(finding).mapNotNull(::internalBackupFileName)
        }.distinct().forEach { fileName ->
            val sourceFile = File(imagesDir, fileName)
            if (!sourceFile.exists() || !sourceFile.isFile) return@forEach

            runCatching {
                zip.putNextEntry(ZipEntry("$BACKUP_IMAGES_DIR$fileName"))
                sourceFile.inputStream().use { input ->
                    input.copyTo(zip)
                }
                zip.closeEntry()
            }
        }
    }

    return backupFile
}

fun importFindings(context: Context): BackupImportResult {
    val zipFile = File(context.getExternalFilesDir(null), BACKUP_ZIP_FILE_NAME)
    if (zipFile.exists()) {
        val importedFromZip = runCatching {
            importZipBackup(context, zipFile)
        }.getOrElse {
            BackupImportResult(
                findings = emptyList(),
                success = false,
                message = "ZIP-Backup konnte nicht gelesen werden."
            )
        }

        if (importedFromZip.success) {
            return importedFromZip
        }

        val legacyFallback = importLegacyJsonBackup(context)
        if (legacyFallback.success) {
            return legacyFallback.copy(
                message = "ZIP-Backup unvollständig. JSON-Backup wurde stattdessen geladen."
            )
        }

        return importedFromZip
    }

    return importLegacyJsonBackup(context)
}

private fun importZipBackup(context: Context, zipFile: File): BackupImportResult {
    val stagingDir = File(context.cacheDir, "backup_import_staging").apply {
        deleteRecursively()
        mkdirs()
    }
    val extractedImageNames = mutableSetOf<String>()
    var zipReadHadErrors = false
    var findingsJson: String? = null

    try {
        ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.isDirectory -> Unit
                    entry.name == BACKUP_JSON_FILE_NAME -> {
                        findingsJson = zip.readBytes().toString(Charsets.UTF_8)
                    }
                    entry.name.startsWith(BACKUP_IMAGES_DIR) -> {
                        val fileName = entry.name.removePrefix(BACKUP_IMAGES_DIR)
                        if (fileName.isNotBlank() && !fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\")) {
                            runCatching {
                                val targetFile = File(stagingDir, fileName)
                                targetFile.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                extractedImageNames.add(fileName)
                            }.onFailure {
                                zipReadHadErrors = true
                            }
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    } catch (_: Exception) {
        zipReadHadErrors = true
    }

    val safeJson = findingsJson?.takeIf { it.isNotBlank() }
        ?: return BackupImportResult(
            findings = emptyList(),
            success = false,
            message = "Backup ist unvollständig. Die Funddaten-Datei fehlt."
        )

    val findings = parseFindingsJsonOrNull(safeJson)
        ?: return BackupImportResult(
            findings = emptyList(),
            success = false,
            message = "Backup ist ungültig. Die Funddaten konnten nicht gelesen werden."
        )

    val imagesDir = File(context.filesDir, BACKUP_FINDING_IMAGES_DIR).apply { mkdirs() }
    extractedImageNames.forEach { fileName ->
        runCatching {
            val sourceFile = File(stagingDir, fileName)
            val targetFile = File(imagesDir, fileName)
            sourceFile.inputStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }.onFailure {
            zipReadHadErrors = true
        }
    }

    val expectedImageNames = findings.flatMap { finding ->
        effectiveLocalPhotoUris(finding).mapNotNull(::internalBackupFileName)
    }.distinct()
    val missingImageCount = expectedImageNames.count { it !in extractedImageNames }
    val isPartialImport = zipReadHadErrors || missingImageCount > 0

    stagingDir.deleteRecursively()

    return BackupImportResult(
        findings = findings,
        success = true,
        message = if (isPartialImport) {
            "Backup geladen. Einige Fotos fehlen oder konnten nicht vollständig wiederhergestellt werden."
        } else {
            "Backup geladen"
        }
    )
}

private fun importLegacyJsonBackup(context: Context): BackupImportResult {
    val legacyJsonFile = File(context.getExternalFilesDir(null), LEGACY_BACKUP_JSON_FILE_NAME)
    if (!legacyJsonFile.exists()) {
        return BackupImportResult(
            findings = emptyList(),
            success = false,
            message = "Kein Backup gefunden."
        )
    }

    val findings = runCatching {
        parseFindingsJsonOrNull(legacyJsonFile.readText())
    }.getOrNull()

    return if (findings != null) {
        BackupImportResult(
            findings = findings,
            success = true,
            message = "JSON-Backup geladen"
        )
    } else {
        BackupImportResult(
            findings = emptyList(),
            success = false,
            message = "JSON-Backup ist ungültig und konnte nicht geladen werden."
        )
    }
}

private fun parseFindingsJsonOrNull(json: String): List<AnimalFinding>? {
    val type = object : TypeToken<List<BackupAnimalFindingDto>>() {}.type
    return runCatching {
        Gson().fromJson<List<BackupAnimalFindingDto>>(json, type)?.map { dto ->
            AnimalFinding(
                roomId = dto.roomId,
                findingId = dto.findingId,
                animalId = dto.animalId,
                date = dto.date,
                location = dto.location,
                note = dto.note,
                photoUri = normalizePhotoList(dto.photoUris, dto.photoUri).firstOrNull().orEmpty(),
                remotePhotoPath = normalizePhotoList(dto.remotePhotoPaths, dto.remotePhotoPath).firstOrNull().orEmpty(),
                thumbnailRemotePhotoPath = dto.thumbnailRemotePhotoPath,
                photoUris = normalizePhotoList(dto.photoUris, dto.photoUri),
                remotePhotoPaths = normalizePhotoList(dto.remotePhotoPaths, dto.remotePhotoPath),
                latitude = dto.latitude,
                longitude = dto.longitude,
                locationSource = dto.locationSource,
                ownerId = dto.ownerId,
                taggedFriendIds = dto.taggedFriendIds
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    .orEmpty()
            )
        }
    }.getOrNull()
}

private fun internalBackupFileName(photoUri: String): String? {
    if (!photoUri.startsWith("internal://")) return null

    val fileName = photoUri.removePrefix("internal://")
    if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
        return null
    }

    return fileName
}
