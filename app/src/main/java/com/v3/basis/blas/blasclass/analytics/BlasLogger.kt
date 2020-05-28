package com.v3.basis.blas.blasclass.analytics

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.v3.basis.blas.blasclass.analytics.BlasLogger.KEY_PREF_NAME
import com.v3.basis.blas.blasclass.app.BlasApp
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * ユーザーのイベントログを保存する為のクラス
 * FirebaseAnalyticsを内部で呼び出す
 */
object BlasLogger {

    const val KEY_PREF_NAME = "log_number"
    const val KEY_LOG_NUMBER = "log_file_number"

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
            val date = formatDateTime(
                BlasApp.applicationContext(),
                System.currentTimeMillis(),
                FORMAT_SHOW_YEAR or FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY or FORMAT_SHOW_TIME
            )
            val trace = t.stackTrace[1]//   UtilityでThrowableインスタンを生成するため、traceLogを呼び出したスタックを使う
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

    @Synchronized
    private fun saveLog(log: String) {

        //file save
        var number = preferences().getInt(KEY_LOG_NUMBER, 1)
        var name = makeFileName(number)
        var file = checkFileExists(name)

        if (overLimitFileSize(file)) {

            //  ファイル番号を更新する
            number = if (number >= 10) { 1 } else { number + 1 }
            preferences().edit().putInt(KEY_LOG_NUMBER, number).apply()
            name = makeFileName(number)
            file = checkFileExists(name)
        }

        FileOutputStream(file, true).bufferedWriter().use { writer ->
            val separator = System.getProperty("line.separator")
            writer.append(log + separator)
        }
    }

    private fun makeFileName(number: Int): String {
        return BlasApp.applicationContext().cacheDir.toString() + "/user_log_$number.txt"
    }

    private fun overLimitFileSize(file: File): Boolean {

        val mb = (file.length() / 1024) / 1024
        return mb > 2
    }

    private fun checkFileExists(name: String): File {

        val file = File(name)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                //Crashlyticsのログに保存する
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
        return file
    }

    private fun preferences(): SharedPreferences {

        return BlasApp.applicationContext().getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE)
    }
}
