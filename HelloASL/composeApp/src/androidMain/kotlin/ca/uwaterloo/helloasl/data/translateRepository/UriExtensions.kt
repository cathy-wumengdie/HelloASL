package ca.uwaterloo.helloasl.data.translateRepository

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun Uri.toTempFile(context: Context): File? {
    val tempFile = File(
        context.cacheDir,
        "temp_sign_video_${System.currentTimeMillis()}.mp4"
    )

    return try {
        val inputStream = context.contentResolver.openInputStream(this) ?: return null

        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        if (tempFile.exists() && tempFile.length() > 0) tempFile else null
    } catch (e: Exception) {
        if (tempFile.exists()) tempFile.delete()
        null
    }
}