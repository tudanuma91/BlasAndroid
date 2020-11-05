package com.v3.basis.blas.blasclass.service

import android.content.Context
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.controller.FixtureController
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.sync.Kenpin
import com.v3.basis.blas.blasclass.sync.Rtn
import com.v3.basis.blas.blasclass.sync.Takeout

class RetryService : Service() {
    companion object {
        var lock:Object = Object()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val openIntent = Intent(this, TerminalActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        var notification = NotificationCompat.Builder(this, 1234.toString())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("これはタイトルです")
            .setContentText("これは内容です")
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        Thread(
            Runnable {
                while(true){
                    var ret = sendData("502")
                    if(!ret) {
                        Thread.sleep(1*60*1000)//1分停止
                    }
                    else {
                        //再送に成功したのでスレッドを終了する
                        break
                    }
                }
                stopSelf()
            }).start()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    fun sendData(projectId:String) : Boolean{
        var ret = false
        synchronized(lock) {
            //該当プロジェクトのDBを取得する

            //再送する

            //応答の結果を書き込み

            //未送信がなければ終了、未送信があれば、リトライを仕掛ける
        }

        return ret
    }

    fun sendFixture(projectId:String) {

        val fixtureController = FixtureController(
            applicationContext,
            projectId.toString()
        )
        val fixtureRecords = fixtureController.search(null,true)
        fixtureRecords.forEach {record ->
            //BLASに送信していないレコードを送信する
            when( record.status ) {
                BaseController.KENPIN_FIN -> {
                    sync = Kenpin(model,fixture)
                }
                BaseController.TAKING_OUT -> {
                    // 持出
                    sync = Takeout( model,fixture )
                }
                BaseController.RTN -> {
                    // 持出
                    sync = Rtn( model,fixture )
                }
                else -> {
                    Log.d("ERROR!!!","パラメータ異常")
                    return
                }
            }
        }
    }
}