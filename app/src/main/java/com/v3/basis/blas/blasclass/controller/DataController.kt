package com.v3.basis.blas.blasclass.controller
import android.content.Context

import com.v3.basis.blas.blasclass.ldb.LDBRecord
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

data class ResultData(
    val totalRecord:Int,    //検索対象のレコード数
    val nowPage:Int,        //現在のページ
    val type:Int,           //テーブルの種類 0:Items, 1:fields 2:Fixtures, 3:rmFixtures,
    //             4:ProjectImages, 5:ProjectFiles,6:Users,
    //             7:Orgs #合成テーブルはどうするかな…それ用にItemUserRecordとか
    //             のデータクラスを作成する
    val records:List<Any>   //レコード（ページの数)
)

//とりあえず仮
interface ViewObserver {
    /**
     * 登録済みデータ一覧を通知するのに利用
     * @param list　
     */
    fun onEvent(result:ResultData)    //TODO Anyをデータ型に変更する
}

/**
 * シングルトン。BLASへの通信を制御する。
 */
open class DataController {
    private var stop_flg:Boolean = false                                  //スレッド停止フラグ
    private var reqList:MutableList<RestRequestData> = mutableListOf()     // キューリスト
    val lock = java.util.concurrent.locks.ReentrantLock()                   //排他制御
    var param:String = ""
    private var listenerList:MutableList<ViewObserver> = mutableListOf()
    private val dbVesion:Int = 1

    /**
     * 購読登録
     * [引数]
     * observer: ViewObserverを継承したobserver
     * [戻り値]
     * なし
     */
    public fun subscribe(observer: ViewObserver){
        lock.withLock {
            listenerList.add(observer)
        }
    }

    /**
     * 購読登録解除
     * [引数]
     * observer: ViewObserverを継承したobserver
     * [戻り値]
     * なし
     */
    @Synchronized
    public fun observerDelete(observer: ViewObserver) {
        lock.withLock {
            listenerList.remove(observer)
        }
    }

    /**
     * 仮登録要求処理。
     * [引数]
     * projectId: 要求を行うプロジェクトID
     * operation: 0:新規登録 1:編集 2:削除
     * [戻り値]
     * 仮登録したIDを返却するつもりだったけど、まだ未定。
     * [例外]
     * IllegalArgumentException
     * ItemsRecord,FixtureRecord以外のクラスをdataObjに指定した
     */
    public fun regist(context: Context, projectId:Int, operation:Int, record: LDBRecord):Int {
        var ret = 0
        val dbName = "${projectId}.db"
        val dbHelper = LDBHelper(context, dbName, null, dbVesion)
       // record.recordStatus = operation

       /* if(record is LDBFixtureRecord) {
            val ldb = LDBFixtureTable(dbHelper)
            //ldb.find()
            ldb.save(record)
        }*/
        return ret
    }

    /**
     * 仮登録中のデータを取り消す
     * [引数]
     * id:テーブルのID。主キー
     * tableType:Itemsテーブルの場合0,FixtureTableの場合1
     * [戻り値]
     * なし
     * [例外]
     * Exception
     * すでに実行され終わったデータをキャンセルしようとした場合
     * 2重キャンセルした場合
     * 指定されたIDが不正だった場合
     */
    public fun cancel(id:Int, tableType:Int){
        throw Exception("キャンセル済みです")
    }

    /**
     * 検索要求。
     * [引数]
     * projectId:検索対象のproject_id
     * page:ページ番号
     * unit:単位
     * conditions:検索条件を辞書形式で指定する。指定された検索条件はOR検索
     *            {"fld1" to "aaa", "fld2" to "bbb"}
     * options:["order" to "fld1 asc"]
     * [備考]
     * 非同期で応答を返すため、本APIを呼び出す前にsubscribeで
     * 購読登録すること。
     */
    public fun find(projectId:Int, conditions:MutableMap<String, String>? = null, page:Int = 1, unit:Int = 20) {
        //nullだったら全部検索

        //レコード数はすべて取得

        //全件数はどうするか
        lock.withLock {
            listenerList.forEach{
                // it.onEvent(1, list)
            }
        }
    }

    /**
     * 仮登録されたデータをBLASに送信する
     *
     */
    public fun send(registId:Int) {
        //ここはDBに書き込むだけ。
    }


    /**
     * スレッドを開始する
     */
    public fun start() {

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
     * 送信スレッド
     */
    @Synchronized public fun mainLoop() {
        /*
        for (//送信ループ) {
            //送信する
            //rest_items…
            //結果を通知
            listenerList.forEach() {
                //it.onEvent()
            }
        }
         */
    }


}