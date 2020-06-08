package com.v3.basis.blas.blasclass.worker

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.v3.basis.blas.ui.ext.unzip
import java.io.*
import java.net.URL
import java.net.URLConnection


class SampleWorker(private val context: Context, workerParameters: WorkerParameters): BaseDownloadWorker(context, workerParameters) {

    override fun downloadTask(downloadUrl: String, savePath: String): Result {

        return download(downloadUrl, savePath)
    }

    override fun getMaxProgressValue(): Int {
        return 0
    }

    private fun download(textUrl: String, localPath: String): Result {

        return try {
            val url = URL(textUrl)
            val urlConnection: URLConnection = url.openConnection()
            urlConnection.connect()

            val lengthOfFile: Int = urlConnection.getContentLength()
            Log.d("FileDownloaderService", "Length of file: $lengthOfFile")
            val input: InputStream = BufferedInputStream(url.openStream())
            val output: OutputStream = FileOutputStream(localPath)
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                val progress = (total * 100 / lengthOfFile).toInt()
//                sendProgress(progress, resultReceiver)
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()

            // UnZip
            val zipPath: String = localPath + "downloadedZip.zip"
            unzip(zipPath, localPath)

            //  DeleteZipFile
            val file = File(zipPath)
            file.delete()
            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
