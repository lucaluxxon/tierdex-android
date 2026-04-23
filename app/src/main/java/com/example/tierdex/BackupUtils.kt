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

        findings.mapNotNull { finding ->
            internalBackupFileName(finding.photoUri)
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

fun importFindings(context: Context): List<AnimalFinding> {
    val zipFile = File(context.getExternalFilesDir(null), BACKUP_ZIP_FILE_NAME)
    if (zipFile.exists()) {
        val importedFromZip = runCatching {
            importZipBackup(context, zipFile)
        }.getOrNull()

        if (importedFromZip != null) {
            return importedFromZip
        }
    }

    val legacyJsonFile = File(context.getExternalFilesDir(null), LEGACY_BACKUP_JSON_FILE_NAME)
    if (!legacyJsonFile.exists()) return emptyList()

    return runCatching {
        parseFindingsJson(legacyJsonFile.readText())
    }.getOrDefault(emptyList())
}

private fun importZipBackup(context: Context, zipFile: File): List<AnimalFinding> {
    val imagesDir = File(context.filesDir, BACKUP_FINDING_IMAGES_DIR).apply { mkdirs() }
    var findingsJson: String? = null

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
                            val targetFile = File(imagesDir, fileName)
                            targetFile.outputStream().use { output ->
                                zip.copyTo(output)
                            }
                        }
                    }
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }

    return findingsJson?.let { parseFindingsJson(it) }.orEmpty()
}

private fun parseFindingsJson(json: String): List<AnimalFinding> {
    val type = object : TypeToken<List<AnimalFinding>>() {}.type
    return runCatching {
        Gson().fromJson<List<AnimalFinding>>(json, type) ?: emptyList()
    }.getOrDefault(emptyList())
}

private fun internalBackupFileName(photoUri: String): String? {
    if (!photoUri.startsWith("internal://")) return null

    val fileName = photoUri.removePrefix("internal://")
    if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
        return null
    }

    return fileName
}
