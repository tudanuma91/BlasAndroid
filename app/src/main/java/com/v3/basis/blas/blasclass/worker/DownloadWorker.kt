package com.v3.basis.blas.blasclass.worker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.rest.BlasRestCache
import com.v3.basis.blas.ui.ext.traceLog
import com.v3.basis.blas.ui.ext.unzip
import com.v3.basis.blas.ui.terminal.common.DownloadModel
import com.v3.basis.blas.ui.terminal.common.DownloadZipModel
import org.json.JSONObject
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
            //getSharedPreferencesはクッキーみたいなもの。
            //値は本クラスのdownload関数内でセットしている。
            return context.getSharedPreferences(COMPLETED_DOWNLOAD, Context.MODE_PRIVATE)
        }
    }

    override fun downloadTask(token: String, projectId: String, savePath: String, unZipPath: String): Result {

        return try {

            val url = getDownloadUrl(token, projectId)
            if (url.isBlank()) {
                return Result.failure()
            }
            download(url, savePath, unZipPath)
            Result.success()
        } catch (e: Exception) {
            traceLog("Failed to download task, ${e::class.java.name}")
            Result.failure()
        }
    }

    private fun getDownloadUrl(token: String, projectId: String): String {

        val payload = mapOf(
            "token" to token,
            "project_id" to projectId
        )
        val success: (json: JSONObject) -> Unit = {}
        val funcError:(Int,Int) -> Unit = {errorCode, aplCode -> }
        val response = BlasRestCache("zip", payload, success, funcError).getResponse()
        val zipModel = Gson().fromJson(response, DownloadZipModel::class.java)
        return BuildConfig.HOST + zipModel.zip_path
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
                ?: throw IllegalStateException("セーブPATHが設定されていません")
            //val fileName = File(unZipPath).listFiles()?.last()?.path
            val fileName = File(unZipPath).listFiles().filter({it.name.endsWith(".db")}).last().path
            Log.d("file path test", "$fileName")
            preferences().edit().putString(name, fileName).apply()
        }
    }
}
