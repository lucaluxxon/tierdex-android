package com.example.tierdex

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider

fun shareBackup(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Backup teilen"))
}

fun exportFindings(context: Context, findings: List<AnimalFinding>): File {
    val json = Gson().toJson(findings)

    val file = File(context.getExternalFilesDir(null), "tierdex_backup.json")
    file.writeText(json)

    return file
}

fun importFindings(context: Context): List<AnimalFinding> {
    val file = File(context.getExternalFilesDir(null), "tierdex_backup.json")

    if (!file.exists()) return emptyList()

    val json = file.readText()
    val type = object : TypeToken<List<AnimalFinding>>() {}.type

    return Gson().fromJson(json, type)
}
