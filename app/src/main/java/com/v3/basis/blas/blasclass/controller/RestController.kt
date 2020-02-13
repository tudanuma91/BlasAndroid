package com.v3.basis.blas.blasclass.controller
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.concurrent.withLock


/**
 * データ形式を決めること
 */
data class RestRequestData(
    val seqNumber:UInt,                         /** 通信通番 */
    val reqCode:UInt,                           /** 要求コード */
    val payLoad:MutableMap<String, String>,     /** リクエストのパラメーター */
    var status:UInt,                            /** 通信ステータス */
    var retryCount:UInt,                        /** リトライした回数 */
    val methodChain:Int
)


/**
 * シングルトン。BLASへの通信を制御する。
 */
object QueueController {
    private var stop_flg:Boolean = false                                  //スレッド停止フラグ
    private var reqList:MutableList<RestRequestData> = mutableListOf()     // キューリスト
    val lock = java.util.concurrent.locks.ReentrantLock()                   //排他制御

    /**
     * スレッドを開始する
     */
    public fun start() {
        try {
            /**
             * 停止前の未送信データを復元する
             */
            reqList = loadQueueFromDB()
        }
        catch(e: Exception) {

        }

        thread{
            stop_flg = false
            mainLoop()
        }
    }

    /**
     * スレッドを停止する
     */
    public fun stop() {
        stop_flg = true
    }


    /**
     * DBからキューリストを復元する
     */
    private fun loadQueueFromDB():MutableList<RestRequestData>{
        /**
         * DBからキューリストを復元する。
         * ダンプファイルでやるなよ、データが吹っ飛んだらシャレになりません
         */
        return mutableListOf()
    }


    /**
     * 送信を要求する
     */
    public fun submit(payLoad:MutableMap<String, String>, viewCallBack:(String)->Unit) {
        lock.withLock {
            /**
             * DBにレコードを追加する。
             * DBはアプリケーションの不意のシャットダウンや再起動で復活させるため。
             */
        }
    }

    /**
     * 未送信データのキューを取得する
     */
    private fun getQueueList():Int {
        var queueId = 1
        return queueId
    }


    /**
     * 指定したIDのノードを削除する。削除するのは送信できた
     * リクエストののノード。
     */
    private fun delQueueNode(queueId:Int) {
        lock.withLock {
            /**
             * キューからノードを削除する
             */
        }
    }

    /**
     * 送信スレッド
     */
    @Synchronized public fun mainLoop() {
        /* 通信のバックグラウンド処理 */
        while(!stop_flg) {
            try {
                /* キューからデータを取り出す */

                /* 送信 */

                /* 正常 */
                キュー消す

                /* 異常 */

            }
            catch(e:Exception) {

            }
            /* キューデータを通信エラーになるまでループする */

            Thread.sleep(10* 1000)
        }

    }


}