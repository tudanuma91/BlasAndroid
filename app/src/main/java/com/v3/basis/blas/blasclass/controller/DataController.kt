package com.v3.basis.blas.blasclass.controller
import android.content.ContentValues
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_OK
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_QUEUE_ERR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_RETRY_MAX_ERR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.APL_SERVER_ERROR
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.FUNC_NAME_FIXTURE
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.FUNC_NAME_ITEM
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_ADD
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_KENPIN
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_RTN
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_TAKEOUT
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_UPDATE
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.OPE_NAME_UPLOAD
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.READ_TIME_OUT_POST
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.REQUEST_TABLE
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.STS_RETRY_MAX
import com.v3.basis.blas.blasclass.app.Is2String
import com.v3.basis.blas.blasclass.app.cakeToAndroid
import com.v3.basis.blas.blasclass.controller.LocationController.getLocation
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.context
import com.v3.basis.blas.blasclass.db.BlasSQLDataBase.Companion.database
import com.v3.basis.blas.blasclass.rest.*
import com.v3.basis.blas.blasclass.rest.BlasRest.Companion.context
import com.v3.basis.blas.blasclass.rest.BlasRest.Companion.queuefuncList
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.NETWORK_ERROR
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URI
import java.sql.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
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

interface LDBRecord {
    //val project_id:Int
    val recordStatus:Int //送信待ち、仮登録中(新規追加)、仮登録中(編集)、仮登録集(削除),送信完了
}

data class ItemRecord(
    val item_id:Int,
    val project_id:Int,
    val user_id:Int,
    val user_name:String,
    val org_id:Int,
    val org_name:String,
    val lat:String, //何に使うかは不明だが
    val lng:String,
    val flds:Array<String>,
    val ee_enter:String,
    val ee_enter_location:String,
    val end_flg:Int,
    val work_flg:Int,
    val modify_user:String,
    val create_date:String,
    val update_date:String,
    override val recordStatus:Int
):LDBRecord

data class FieldRecord(
    val field_id:Int,
    val project_id:Int,
    val col:Int,
    val name:String,
    val type:Int,
    val choice:String,
    val alnum:Int,
    val notify:Int,
    val essential:Int,
    val input:Int,
    val export:Int,
    val other:Int,
    val map:Int,
    val address:Int,
    val filename:Int,
    val parent_field_id:Int,
    val summary:Int,
    val create_date:String,
    val update_date:String,
    val unique_chk:Int,
    val work_day:Int,
    override val recordStatus:Int
):LDBRecord

data class ProjectImageRecord(
    val project_image_id:Int,       //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //ファイル名
    val rank:Int,                   //表示順序
    val list:Int,                   //一覧表示する場合1,一覧表示しない場合0
    val field_id:Int,               //ファイル名に出力する項目
    val create_date:String,         //作成日時
    val update_date:String,         //更新日時
    override val recordStatus:Int
):LDBRecord

data class ProjectFileRecord(
    val project_file_id:Int,        //主キー
    val project_id:Int,             //プロジェクトID
    val name:String,                //ファイル名
    val rank:Int,                   //表示順序
    val list:Int,                   //一覧表示する場合1,一覧表示しない場合0
    val create_date:String,         //作成日時
    val update_date:String,         //更新日時
    override val recordStatus:Int
):LDBRecord


data class FixtureRecord(
    val fixture_id:Int,            //主キー
    val project_id:Int,            //プロジェクトID
    val fix_org_id:Int,            //検品した会社ID
    val fix_org_name:String,       //検品した会社名
    val fix_user_id:Int,           //検品したユーザID
    val fix_user_name:String,      //検品したユーザ名
    val fix_date:String,           //検品した日時
    val takeout_org_id:Int,        //持ち出した会社ID
    val takeout_org_name:String,   //持ち出した会社前伊
    val takeout_user_id:Int,       //持出者のID
    val takeout_user_name:String,  //持出者の名前
    val takeout_date:String,       //持ち出した日時
    val rtn_org_id:Int,            //返却した会社ID
    val rtn_org_name:String,       //返却した会社の名前
    val rtn_user_id:Int,           //返却したユーザＩＤ
    val rtn_user_name:String,      //返却したユーザ名
    val rtn_date:String,           //返却した日時
    val item_id:Int,               //設置した機器が登録されているデータ管理の外部キー
    val item_org_id:Int,           //設置した会社のＩＤ
    val item_org_name:String,      //設置した会社の名前
    val item_user_id:Int,          //設置したユーザのＩＤ
    val item_user_name:String,     //設置したユーザ名
    val item_date:String,          //設置した日時
    val serial_number:String,      //シリアルナンバー
    val status:Int,                //0:検品済み(持ち出し可), 1:持ち出し中,2:設置済み, 3:持出不可
    val create_date:String,        //レコード作成日付
    val update_date:String,        //レコード更新日付
    override val recordStatus:Int
):LDBRecord

data class RmFixtureRecord(
    val rm_fixture_id:Int,          //主キー
    val project_id:Int,             //プロジェクトID
    val rm_org_id:Int,              //撤去した会社ID
    val rm_org_name:String,         //撤去した会社の名前
    val rm_user_id:Int,             //撤去したユーザID
    val rm_user_name:String,        //撤去したユーザ名
    val rm_date:String,             //撤去した日時
    val rm_tmp_org_id:Int,          //一時保管した会社ID
    val rm_tmp_org_name:String,     //一時保管した会社名
    val rm_tmp_user_id:Int,         //一時保管したユーザID
    val rm_tmp_user_name:String,    //一時保管したユーザ名
    val rm_tmp_date:String,         //一時保管した日付
    val rm_comp_org_id:Int,         //撤去完了した会社ID
    val rm_comp_org_name:String,    //撤去完了した会社名
    val rm_comp_user_id:Int,        //撤去完了したユーザID
    val rm_comp_user_name:String,   //撤去完了したユーザ名
    val rm_comp_date:String,        //撤去完了した日時
    val item_id:Int,                //撤去した機器のシリアルナンバーを含むデータ管理のレコード(外部キー)
    val item_org_id:Int,            //撤去した機器のシリアルナンバーを含むデータ管理の会社ID
    val item_org_name:String,       //撤去した機器のシリアルナンバーを含むデータ管理の会社名
    val item_user_id:Int,           //撤去した機器のシリアルナンバーを含むデータ管理のユーザID
    val item_user_name:String,      //撤去した機器のシリアルナンバーを含むデータ管理のユーザ名
    val serial_number:String,       //シリアルナンバー
    val status:Int,                 //0:未撤去 1:現場撤去 2:一時保管 3:撤去完了
    override val recordStatus:Int
):LDBRecord

data class OrgRecord(
    val org_id:Int,
    val name:String,
    val kana:String,
    val en_name:String,
    val type1_id:Int,
    val type2_id:Int,
    val zip1:String,
    val zip2:String,
    val zip:String,
    val pref_id:Int,
    val city:String,
    val address:String,
    val tel1:String,
    val tel2:String,
    val tel3:String,
    val tel:String,
    val fax1:String,
    val fax2:String,
    val fax3:String,
    val mail:String,
    val remark:String,
    val status_id:Int,
    val image:String,
    val create_user_id:Int,
    val create_date:String,
    val update_date:String,
    override val recordStatus:Int
):LDBRecord

data class UserRecord(
    val user_id:Int,
    val username:String,
    val password:String,
    val mail:String,
    val org_id:Int,
    val name:String,
    val group_id:Int,
    val image:String,
    val sign:String,
    val status:Int,
    val w_count:Int,
    val remark:String,
    val en_org_id:Int,
    val token:String,
    val alive_date:String,
    val lat:String,
    val lng:String,
    val use_polemap:Int,
    val working_item_id:Int,
    val active_date:String,
    val create_user_id:Int,
    val seed:String,
    override val recordStatus:Int
):LDBRecord

data class DrawingRecord(
    val drawing_id:Int,
    val project_id:Int,
    val name:String,
    val filename:String,
    val user_id:Int,
    val user_name:String,
    val org_id:Int,
    val org_name:String,
    val item_id:Int,
    val file_type:String,
    val file_path:String,
    val spot_volume:Int,
    val create_date:String,
    val update_date:String,
    val created:String,
    val modified:String,
    val drawing_category_id:Int,
    val drawing_sub_category_id:Int,
    override val recordStatus:Int
):LDBRecord

data class DrawingCategoryRecord(
    val drawing_category_id:Int,
    val project_id:Int,
    val drawing_id:Int,
    val name:String,
    val rank:String,
    val create_date:String,
    val update_date:String,
    override val recordStatus:Int
):LDBRecord

data class DrawingSubCategoryRecord(
    val drawing_sub_category_id: Int,
    val drawing_category_id:Int,
    val project_id:Int,
    val drawing_id:Int,
    val name:String,
    val rank:Int,
    val create_date:String,
    val update_date:String,
    override val recordStatus:Int
):LDBRecord


data class DrawingSpotRecord(
    val spot_id:Int,
    val project_id:Int,
    val item_id:Int,
    val drawing_id:Int,
    val user_id:Int,
    val org_id:Int,
    val name:String,
    val shape_color:String,
    val comment:String,
    val abscissa:String,
    val ordinate:String,
    val link:String,
    val col:Int,
    val create_date:String,
    val update_date:String,
    override val recordStatus:Int
):LDBRecord



/**
 * シングルトン。BLASへの通信を制御する。
 */
object DataManager {
    private var stop_flg:Boolean = false                                  //スレッド停止フラグ
    private var reqList:MutableList<RestRequestData> = mutableListOf()     // キューリスト
    val lock = java.util.concurrent.locks.ReentrantLock()                   //排他制御
    var param:String = ""
    private var listenerList:MutableList<ViewObserver> = mutableListOf()


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
    public fun delete(observer: ViewObserver) {
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
    public fun regist(projectId:Int, operation:Int, dataObj:Any):Int {
        var record: Any
        var ret = 0
        if(dataObj is ItemRecord) {
            record = dataObj as ItemRecord
            //Itemsテーブルにレコードを仮登録する
        }
        else if (dataObj is FixtureRecord) {
            record = dataObj as FixtureRecord
            //Fixturesテーブルにレコードを仮登録する
        }
        else {
            //対応していないクラスが指定された
            throw IllegalArgumentException("対応していないクラスが指定されました")
        }

        //Queueテーブルにレコードを追加する

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