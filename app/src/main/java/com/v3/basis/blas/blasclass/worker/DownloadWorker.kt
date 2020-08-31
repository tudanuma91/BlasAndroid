package com.v3.basis.blas.blasclass.worker

import android.accounts.NetworkErrorException
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.v3.basis.blas.BuildConfig
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.rest.BlasRestCache
import com.v3.basis.blas.blasclass.rest.SyncBlasRestCache
import com.v3.basis.blas.ui.ext.traceLog
import com.v3.basis.blas.ui.ext.unzip
import com.v3.basis.blas.ui.terminal.common.DownloadZipModel
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*

/**
 * [説明]
 * BLASからデータベースファイルをバックグラウンドでダウンロードするクラス。
 */
class DownloadWorker(context: Context, workerParameters: WorkerParameters): BaseDownloadWorker(context, workerParameters) {

    companion object {
        const val COMPLETED_DOWNLOAD: String = "completed_download"

        fun getDbFileName(projectId: String): String? {
            return getSavedPath(projectId)?.let {
                File(it).name
            }
        }

        /**
         * [説明]
         * ローカルDBの保存先のパスを取得するクラス。
         * 保存先のパスは、プレファレンス(WEBのクッキー)に保存しているため、
         * プレファレンスから取得する。プレファレンスの設定自体はダウンロード完了時に行っている。
         * ダウンロードは本クラスのdownloadメソッドのpreference().edit()で行っている。
         * [引数]
         * プロジェクトID
         * [戻り値]
         * DBの保存パス。エラー時はdefaultValueのnullを返す。
         */
        fun getSavedPath(projectId: String): String? {

            val all = preferences().all
//            val path = preferences().getString(projectId, null)
            val path = preferences().getString( BlasApp.userId.toString() + "_" + projectId + "_", null)

            return path
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
                return Result.success(workDataOf(KEY_RESULT_SUCCEEDED to false))
            }
            download(url, savePath, unZipPath,projectId)
            Result.success()
        } catch (e: Exception) {
            traceLog("Failed to download task, ${e::class.java.name}")
            Log.d("error!!!!!!!!!!!!",e.message)
            //  failureを返すと、永久に再ダウンロードできなくなる
            Result.success(workDataOf(KEY_RESULT_SUCCEEDED to false))
        }
    }

    /**
     * [説明]
     * BLASで作成されたZIPファイルのURLを取得する
     * [TODO]
     * エラー時の処理がない？
     */
    private fun getDownloadUrl(token: String, projectId: String): String {

        val payload = mapOf(
            "token" to token,
            "project_id" to projectId
        )
        //BLASからLDBをダウンロードする。
        val response = SyncBlasRestCache().downloadZipUrl(payload)
        val zipModel = Gson().fromJson(response, DownloadZipModel::class.java)
        return BuildConfig.HOST + zipModel.zip_path
    }

    override fun getMaxProgressValue(): Int {
        /*機能していません。overrideしないといけないのでしているだけ。 */
        return 0
    }

    /**
     * [説明]
     * getDownloadUrlメソッドで取得したURLを指定してLDBをダウンロードする。
     * ダウンロードしたファイルはpreferenceに保存する。
     */
    private fun download(textUrl: String, localPath: String, unZipPath: String,projectId:String) {

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
            val filePath = File(unZipPath).listFiles().filter({it.name.endsWith(".db")}).last().path
            Log.d("file path test", "$filePath")

            // userIdをファイル名に付加
            Log.d("file name",File(filePath).name)
            val new_name = BlasApp.userId.toString() + "_" + projectId + "_" +  UUID.randomUUID().toString()
            val new_path = unZipPath + "/" + new_name
            Log.d("new file path",new_path)

            File(filePath).renameTo( File( new_path ) )

            preferences().edit().putString(BlasApp.userId.toString() + "_" + projectId + "_", new_path ).apply()
        }
    }
}
