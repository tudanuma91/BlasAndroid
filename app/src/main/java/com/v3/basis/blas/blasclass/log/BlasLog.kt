package com.v3.basis.blas.blasclass.log

import android.os.Looper
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object BlasLog {
    private val lock = ReentrantLock()
    private val logDir = BlasApp.applicationContext().dataDir.path  + "/logs/"
    private val logPath1 = logDir + "trace0.log"
    private val logPath2 = logDir + "trace1.log"
    private val maxLogSize = 10 * 1024 * 1024
    private val logs = arrayOf(logPath1, logPath2)

    init {
        val dir = File(logDir)
        if(!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun rotate():String {
        var retIndex = 0
        var find = false
        for(i in 0 until logs.size) {
            val logFile = File(logs[i])
            if((logFile.exists()) and (logFile.length() < maxLogSize)) {
                //ファイルが存在して上限サイズより小さい
                retIndex = i
                find = true
                break
            }
            else if(!logFile.exists()) {
                //まだファイルが作成されていない
                retIndex = i
                find = true
                break
            }
        }

        if(!find) {
            //どちらもファイルの上限が超えていた場合は日付が古いほうを消す
            var oldLogFile = File(logs[0])
            var tmpAccessTime = oldLogFile.lastModified()
            for(i in 0 until logs.size) {
                val logFile = File(logs[i])
                if (tmpAccessTime > logFile.lastModified()) {
                    tmpAccessTime = logFile.lastModified()
                    oldLogFile = logFile

                }
            }

            //ファイルをクリアする
            oldLogFile.delete()
        }

        return logs[retIndex]
    }


    fun trace(level:String, msg:String) {
        try {
            //時刻生成
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")
            val dateTime = current.format(formatter)

            //Looperのスレッド(メインスレッド)
            val mainThreadId = Looper.getMainLooper().thread.id

            //スレッドID取得
            val myThreadId = Thread.currentThread().id

            //コール元関数名
            val fromMethod = Throwable().stackTrace[1]

            //ログメッセージ
            val logMsg = "${level} ${dateTime} ${mainThreadId}-${myThreadId} ${fromMethod} ${msg}\n"

            lock.withLock {
                //ログファイルの決定
                val logFileName = rotate()
                FileWriter(logFileName, true).use {
                    it.write(logMsg)
                }

                Log.d("blas-trace-log", logMsg)
            }
        }
        catch(e:Exception) {
            e.printStackTrace()
        }
    }


    fun trace(level:String, msg:String, e:Exception?=null) {
        try {
            //時刻生成
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")
            val dateTime = current.format(formatter)

            //Looperのスレッド(メインスレッド)
            val mainThreadId = Looper.getMainLooper().thread.id

            //スレッドID取得
            val myThreadId = Thread.currentThread().id

            //コール元関数名
            val fromMethod = Throwable().stackTrace[1]

            //ログメッセージ
            val logMsg = "${level} ${dateTime} ${mainThreadId}-${myThreadId} ${fromMethod} ${msg}\n"

            lock.withLock {
                //ログファイルの決定
                val logFileName = rotate()
                FileWriter(logFileName, true).use {
                    it.write(logMsg)
                    if(e != null) {
                        //Exceptionが渡されたときは、例外を書き込む
                        val pw = PrintWriter(it)
                        if(e.message != null) {
                            it.write(e.message + "\n")
                        }
                        e.printStackTrace(pw)
                    }
                }

                Log.d("blas-trace-log", logMsg)
            }
        }
        catch(e:Exception) {
            e.printStackTrace()
        }
    }
}