package com.v3.basis.blas.blasclass.analytics

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils.*
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.ui.ext.checkFileExists
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * ユーザーのイベントログを保存する為のクラス
 * [設定]
 * ログ保存に使用するファイルサイズはデフォルトで2MB
 * ファイルサイズはFILE_SIZE_LIMIT_MBで指定する
 */
object BlasLogger {

    private const val KEY_PREF_NAME = "log_number"
    private const val KEY_LOG_NUMBER = "log_file_number"
    private const val DIR_NAME = "blas_log"
    private const val FILE_NAME_PREFIX = "/user_log_"
    private const val EXTENSIONS = ".txt"

    // ログを保存する最大サイズ
    var FILE_SIZE_LIMIT_MB: Int = 2
    set(value) {
        if (value in 1..20) {
            field = value
        } else {
            Log.d("BlasLogger", "指定のファイルサイズが大きすぎます.")
        }
    }

    /**
     * ログ保存処理
     * [引き数]
     * msg:String ログに追加するメッセージ
     * [戻り値]
     * なし
     * [例外処理]
     * なし
     */
    fun logEvent(t: Throwable, msg: String = "") {

        val disposable = CompositeDisposable()
        val action = Completable.fromAction {

            val trace = t.stackTrace[1]//   UtilityでThrowableインスタンを生成するため、traceLogを呼び出したスタックを使う
            val date = makeDateTime()
            val log = "$date ${BlasApp.token} TraceFile:${trace.fileName} TraceClass:${trace.className} function:${trace.methodName} line:${trace.lineNumber} msg:$msg"
            saveLog(log)
        }

        action.observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.newThread())
            .doOnError {
                FirebaseCrashlytics.getInstance().log("Failed to saveLog: ${it.stackTrace}")
                disposable.dispose()
            }
            .doOnComplete {
                disposable.dispose()
            }
            .onErrorComplete()
            .subscribe()
            .addTo(disposable)
    }

    /**
     * 現在日時を取得して文字列で返す
     *  [引き数]
     *  なし
     *  [戻り値]
     *  年月日と時分秒、ミリ秒のフォーマットで返す
     */
    private fun makeDateTime(): String {

        val date = formatDateTime(
            BlasApp.applicationContext(),
            System.currentTimeMillis(),
            FORMAT_SHOW_YEAR or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY or FORMAT_SHOW_TIME
        )
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = Date()
        val sec = calendar.get(Calendar.SECOND)
        val mill = calendar.get(Calendar.MILLISECOND)
        return "$date:$sec $mill"
    }

    /**
     * ログ保存処理
     * [引き数]
     * log: String ログに追加するメッセージ
     * [戻り値]
     * なし
     * [例外処理]
     * なし
     */
    @Synchronized
    private fun saveLog(log: String) {

        //file save
        val dir = checkDirExists()
        var number = preferences().getInt(KEY_LOG_NUMBER, 1)
        var name = makeFilePath(dir, number)
        var file = checkFileExists(name)

        if (overLimitFileSize(file)) {

            //  ファイル番号を更新する
            number = if (number >= 10) { 1 } else { number + 1 }
            preferences().edit().putInt(KEY_LOG_NUMBER, number).apply()
            name = makeFilePath(dir, number)
            file = checkFileExists(name)
        }

        FileOutputStream(file, true).bufferedWriter().use { writer ->
            val separator = System.getProperty("line.separator")
            writer.append(log + separator)
        }
    }

    /**
     * ログ用ディレクトリのパスを返す
     * ディレクトリが存在しなければ、ディレクトリを作成する
     *  [引き数]
     *  なし
     *  [戻り値]
     *  ログファイルディレクトリのパス
     */
    private fun checkDirExists(): String {
        val path = BlasApp.applicationContext().filesDir.toString() + "/$DIR_NAME"
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdir()
        }
        return path
    }

    /**
     *  ファイルパスを生成する関数
     *  [引き数]
     *  number ファイル番号
     *  [戻り値]
     *  String　ファイルパス
     */
    private fun makeFilePath(dirPath: String, number: Int): String {
        return dirPath + FILE_NAME_PREFIX + number + EXTENSIONS
    }

    /**
     *  最大ファイルサイズを超えているかチェックする関数
     *  [引き数]
     *  file: File　対象File
     *  [戻り値]
     *  true: 最大ファイルサイズを超えている
     *  false: ファイルサイズは問題なし
     */
    private fun overLimitFileSize(file: File): Boolean {

        val mb = (file.length() / 1024) / 1024
        return mb > FILE_SIZE_LIMIT_MB
    }

    /**
     * ログ番号用のプリファレンスを取得する
     *  [引き数]
     *  なし
     *  [戻り値]
     *  プリファレンスのインスタンス
     */
    private fun preferences(): SharedPreferences {

        return BlasApp.applicationContext().getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE)
    }
}
