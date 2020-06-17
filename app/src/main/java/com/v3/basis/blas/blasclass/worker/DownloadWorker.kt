package com.v3.basis.blas.blasclass.worker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.WorkerParameters
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.ui.ext.traceLog
import com.v3.basis.blas.ui.ext.unzip
import java.io.*
import java.net.URL
import java.net.URLConnection


class DownloadWorker(context: Context, workerParameters: WorkerParameters): BaseDownloadWorker(context, workerParameters) {

    companion object {
        const val COMPLETED_DOWNLOAD: String = "completed_download"

        fun getDbFileName(projectId: String): String? {
            return getSavedPath(projectId)?.let {
                File(it).name
            }
        }

        fun getSavedPath(projectId: String): String? {
            return preferences().getString(projectId, null)
        }

        private fun preferences(): SharedPreferences {

            val context = BlasApp.applicationContext()
            return context.getSharedPreferences(COMPLETED_DOWNLOAD, Context.MODE_PRIVATE)
        }
    }

    override fun downloadTask(downloadUrl: String, savePath: String, unZipPath: String): Result {

        return try {

            download(downloadUrl, savePath, unZipPath)
            Result.success()
        } catch (e: Exception) {
            traceLog("Failed to download task, ${e::class.java.name}")
            Result.failure()
        }
    }

    override fun getMaxProgressValue(): Int {
        return 0
    }

    private fun download(textUrl: String, localPath: String, unZipPath: String) {

        val url = URL(textUrl)
        val urlConnection: URLConnection = url.openConnection()
        urlConnection.connect()

        val lengthOfFile: Int = urlConnection.contentLength
        Log.d("FileDownloaderService", "Length of file: $lengthOfFile")
        val input: InputStream = BufferedInputStream(url.openStream())
        val output: OutputStream = FileOutputStream(localPath)
        val data = ByteArray(1024)
        var total: Long = 0
        var count: Int
        while (input.read(data).also { count = it } != -1) {
            total += count.toLong()
//                val progress = (total * 100 / lengthOfFile).toInt()
//                sendProgress(progress, resultReceiver)
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        input.close()

        // UnZip
        if (unzip(localPath, unZipPath)) {
            //  DeleteZipFile
            val file = File(localPath)
            file.delete()

            val name = inputData.getString(KEY_SAVE_PATH_KEY_NAME)
                ?: throw IllegalStateException("might be forgot set to savePath key via with WorkerHelper")
            val fileName = File(unZipPath).listFiles()?.get(0)?.path
            preferences().edit().putString(name, fileName).apply()
        }
    }
}
