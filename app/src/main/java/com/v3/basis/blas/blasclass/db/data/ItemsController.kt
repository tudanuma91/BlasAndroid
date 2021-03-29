package com.v3.basis.blas.blasclass.db.data

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.work.impl.WorkDatabasePathHelper.getDatabasePath
import com.v3.basis.blas.blasclass.app.BlasApp
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.linkFixtures.LinkFixture
import com.v3.basis.blas.blasclass.db.data.linkFixtures.LinkRmFixture
import com.v3.basis.blas.blasclass.db.field.FieldController
import com.v3.basis.blas.blasclass.db.fixture.Fixtures
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.ldb.LdbFieldRecord
import com.v3.basis.blas.blasclass.ldb.LdbItemRecord
import com.v3.basis.blas.blasclass.ldb.LdbRmFixtureRecord
import com.v3.basis.blas.blasclass.log.BlasLog
import com.v3.basis.blas.blasclass.worker.DownloadWorker
import com.v3.basis.blas.ui.item.common.FieldCheckText
import com.v3.basis.blas.ui.item.common.FieldModel
import com.v3.basis.blas.ui.item.common.FieldMultiSelect
import com.v3.basis.blas.ui.item.common.FieldText
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ItemsController(context: Context, projectId: String): BaseController(context, projectId) {

    companion object {

        const val FIELD_TYPE_SINGLE_SELECT = 5
        const val FIELD_TYPE_QRCODE_INT_INSPECTION = 8
        const val FIELD_TYPE_QRCODE_INT_RM = 11

        private val lock = ReentrantLock()
    }

    private val addList = mutableListOf<String>()
    private lateinit var plHolder : Array<String>
    private val itemCacheDir = BlasApp.applicationContext().dataDir.path  + "/items/${projectId}/"
    private val cacheFile = itemCacheDir + "id_map.cache"

    init {
        //キャッシュディレクトリがなかったら作成する
        if(!File(itemCacheDir).exists()) {
            File(itemCacheDir).mkdirs()
        }

    }
    /**
     * ワード検索条件を構築
     */
    private fun createAddList(findValueMap: MutableMap<String, String?>) {
        Log.d("createAddList()", "start")

        if( !findValueMap.containsKey("freeWord") )
            return
        else if(findValueMap["freeWord"].isNullOrBlank())
            return

        val freeWord = findValueMap["freeWord"]

        val user = getUserInfo()
        var groupId = 0
        if( null == user ) {
            groupId = 1
        }
        else {
            groupId = user.group_id
        }

        val dataDispHiddenCol = getGroupsValue(groupId, "data_disp_hidden_column")
        val fields = FieldController(context, projectId).getFieldRecords()
        var additionOr = arrayOf<String>()

        fields.forEach {
            var disp = false
            if( 1 == dataDispHiddenCol ) {
                disp = true
            }
            else {
                if( 1 == it.edit_id || 2 == it.edit_id ) {
                    disp = true
                }
            }

            if( disp ) {
                additionOr +=   " fld" + it.col + " like ? "
                plHolder += "%" + freeWord + "%"
            }
        }

        var orStr = ""
        if( additionOr.count() > 0 ) {

            orStr += " ("
            var first = true
            additionOr.forEach {
                if( !first ) {
                    orStr += " or "
                }
                orStr += it
                first = false
            }
            orStr += ") "
        }
        addList += orStr
    }

    fun find(conditions:Map<String, String>): MutableList<MutableMap<String, String?>> {
        var sql = "select * from Items where "
        val plHolder = mutableListOf<String>()
        val ret = mutableListOf<MutableMap<String, String?>>()

        conditions.forEach { key, value ->
            sql += "${key}=? or "
            plHolder.add(value)
        }
        sql = sql.substring(0, sql.length - 3)

        val cursor = db?.rawQuery(sql, plHolder.toTypedArray())

        cursor?.let { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                val value = getMapValues(c)
                if (null != value) {
                    ret.add(getMapValues(c))
                }
                notLast = c.moveToNext()
            }
        }

        return ret
    }

    fun findByItemId(item_id:String):MutableMap<String, String?> {
        var record = mutableMapOf<String, String?>()
        val sql = "select * from items where item_id=?"
        val plHolder = arrayOf<String>(item_id)
        val cursor = db?.rawQuery(sql, plHolder)
        cursor?.let {c->
            c.moveToFirst()
            record = getMapValues(c)
        }

        cursor?.close()

        return record
    }

    fun search(
        item_id: Long = 0L,
        offset: Int = 0,
        paging: Int = 20,
        endShow: Boolean = false,
        syncFlg: Boolean = false,
        freeword: Boolean = true,
        findValueMap: MutableMap<String, String?>? = null,
        isErrorOnly:Boolean = false
    ): MutableList<MutableMap<String, String?>> {

        // 初期化
        addList.clear()
        plHolder = arrayOf<String>()

        val cursor = when {
            0L == item_id -> {

                var addition = ""

                if( null != findValueMap ) {
                    // ワード検索
                    createAddList(findValueMap)
                }

                // ゴミ箱
                if( !endShow ) {
                    addList += " end_flg = 0 "
                }
                // 未同期のみ
                if( syncFlg ) {
                    addList += " sync_status > 0 "
                }

                // where文作成
                if( addList.count() > 0 ) {
                    addition += " where "
                    var first = true
                    addList.forEach {
                        if( !first ) {
                            addition += " and "
                        }
                        addition += it
                        first = false
                    }
                }

                if(isErrorOnly) {
                    addition += " sync_status >0 "
                }

                var addingPager = ""
                //var plHolder = arrayOf<String>()
                if( 0 != paging ) {
                    addingPager = "limit ? offset ?"
                    plHolder += paging.toString()
                    plHolder += offset.toString()
                }

                //ここで画像が非同期だった場合もサーバーに登録ボタンを表示したい
                val sql = "select * from items "+ addition + " order by create_date desc " + addingPager
                BlasLog.trace("I", sql)
                db?.rawQuery(sql, plHolder)
            }

            else -> {
                db?.rawQuery("select * from items where item_id = ?", arrayOf(item_id.toString()))
            }
        }

        val ret = mutableListOf<MutableMap<String, String?>>()
        cursor?.also { c ->
            var notLast = c.moveToFirst()
            while (notLast) {
                val value = getMapValues(cursor)
                if( null != value ) {
                    ret.add(getMapValues(cursor))
                }
                notLast = c.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    private fun ctlValidQr(
        map: Map<String, String?>,
        orgItemMap: MutableMap<String, String?>? = null
    ) {
        val inst = getFieldCols(FIELD_TYPE_QRCODE_INT_INSPECTION)
        val rms = getFieldCols(FIELD_TYPE_QRCODE_INT_RM)

        // QRコード(検品連動バリデートチェック)
        inst.forEach{
            if(map.contains("fld" + it.toString()) && map["fld" + it.toString()]?.isNotEmpty()!!) {
                if( null != orgItemMap && map["fld" + it.toString()] == orgItemMap["fld" + it.toString()] ) {
                    return@forEach
                }
                qrCodeCheck(map["fld" + it.toString()])
            }
        }
        // QRコード(撤去連動バリデートチェック)
        rms.forEach {
            if( map.contains("fld" + it.toString()) && map["fld" + it.toString()]?.isNotEmpty()!!) {
                if( null != orgItemMap && map["fld" + it.toString()] == orgItemMap["fld" + it.toString()] ) {
                    return@forEach
                }
                rmQrCodeCheck(map["fld" + it.toString()])
            }
        }

    }

    fun insertToLDB(map: MutableMap<String, String?>): Boolean {
        var ret = true

        // QRコードバリデート処理
        ctlValidQr(map)

        val item = Items()
        setProperty(item, map)

        item.item_id = createTempId()
        item.project_id = projectId.toInt()

        val user = getUserInfo()
        if( null != user ) {
            item.user_id = user?.user_id
            item.org_id = user?.org_id
        }
        else {
            item.user_id = 1
            item.org_id = 1
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        item.create_date = current.format(formatter)
        item.update_date = current.format(formatter)
        item.sync_status = SYNC_STATUS_NEW
        item.error_msg = "送信待ちです"

        val cv = createConvertValue(item)

        try {
            db?.beginTransaction()

            // itemテーブルに追加
            db?.insertOrThrow("items", null, cv)
            // fixture(rm_fixture)を更新
           updateFixture(item, map)

            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            BlasLog.trace("E", "データの新規追加に失敗しました", e)
            ret = false
        }
        finally {
            db?.endTransaction()
        }

        return ret
    }

    fun updateToLDB(map: Map<String, String?>, status:Int=SYNC_STATUS_EDIT): Boolean {
        var ret = true

        // QRコードバリデート処理
        val orgItemMap = map["item_id"]?.toLong()?.let { search(it) }
        orgItemMap?.get(0)?.let { ctlValidQr(map, it) }

        // mapで来たのをclass入れる
         val item = Items()
        setProperty(item, map)

        val user = getUserInfo()
        if( null != user ) {
            item.user_id = user?.user_id
            item.org_id = user?.org_id
        }
        else {
            item.user_id = 1
            item.org_id = 1
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")
        item.update_date = current.format(formatter)
        item.sync_status = status
        if(status == SYNC_STATUS_EDIT) {
            item.error_msg = "送信待ちです"
        }

        // けどまたmap…
        val cv = createConvertValue(item, null)

        try {
            db?.beginTransaction()
            // itmeテーブルを更新
            var num = db?.update("items", cv, "item_id = ?", arrayOf(item.item_id.toString()))
            if(num == 0) {
                BlasLog.trace("E", "DBの更新に失敗しました")
            }
            else {
                // fixture(rm_fixture)を更新
                updateFixture(item, map)
            }

            db?.setTransactionSuccessful()

            BlasLog.trace("I", "item_id:${item.item_id.toString()}を更新しました")
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            BlasLog.trace("E", "item_id:${item.item_id.toString()}の更新に失敗しました", e)
            ret = false
        }
        finally {
            db?.endTransaction()
        }

        return ret
    }

    /**
     * 指定されたタイプのFieldのColを返す
     */
    private fun getFieldCols(type: Int) : List<Int> {
        val ret = mutableListOf<Int>()

        val sql = "select * from fields where type = ?"
        val cursor = db?.rawQuery(sql, arrayOf(type.toString()))

        cursor?.also {
            var notLast = it.moveToFirst()
            while( notLast ) {
                ret.add(cursor.getInt(cursor.getColumnIndex("col")))
                notLast = it.moveToNext()
            }
        }
        cursor?.close()

        return ret as List<Int>
    }


    /**
     * fixtureローカルテーブルの更新
     */
    private fun updateFixture(item: Items, map: Map<String, String?>) {
        val inst = getFieldCols(FIELD_TYPE_QRCODE_INT_INSPECTION)
        val rms = getFieldCols(FIELD_TYPE_QRCODE_INT_RM)

        // 設置
        inst.forEach{
            // TODO:約束事ではmapからではなくitemから取得する ⇒ うまくいかないのでとりあえずmapから取得
//                val test = item::class.java.getField("fld" + it.toString())
//                Log.d("test",test.toString())

            if( map.containsKey("fld" + it.toString()) ) {
                LinkFixture(db, item, map.get("fld" + it.toString()).toString()).exec()
            }
        }

        // 撤去
        rms.forEach{
            if( map.containsKey("fld" + it.toString()) ) {
                LinkRmFixture(db, item, map.get("fld" + it.toString()).toString()).exec()
            }
        }

    }

    /**
     * ItemIdを新しいものに置き換える
     */
    fun updateItemId(org_item_id: String, new_item_id: String) {
        val cv = ContentValues()
        cv.put("item_id", new_item_id)
        cv.put("sync_status", SYNC_STATUS_SYNC)
        cv.put("error_status", NETWORK_NORMAL)

        try {
            db?.beginTransaction()

            db?.update("items", cv, "item_id = ?", arrayOf(org_item_id))
            db?.update("fixtures", cv, "item_id = ?", arrayOf(org_item_id))
            db?.update("rm_fixtures", cv, "item_id = ?", arrayOf(org_item_id))
            db?.setTransactionSuccessful()!!

            //画面が開きっぱなしのとき、画面はIDの変更を知らないので、変更テーブルをキャッシュファイルに書き込む。
            if(org_item_id != new_item_id) {
                writeCache(org_item_id, new_item_id)
            }
        }
        catch (e: Exception) {
            BlasLog.trace("E", "レコードの更新に失敗しました", e)
        }
        finally {
            db?.endTransaction()
        }
    }

    fun updateItemRecordStatus(item_id:String?, errorStatus:Int?, sendCnt:Int?, errorMsg:String) {
        if(item_id == null){
            return
        }

        if(errorStatus == null){
            return
        }

        if(sendCnt == null) {
            return
        }

        val cv = ContentValues()
        cv.put("error_status", errorStatus)
        cv.put("send_cnt", sendCnt)
        cv.put("error_msg", errorMsg)

        try {
            db?.beginTransaction()

            db?.update("items", cv, "item_id = ?", arrayOf(item_id))

            db?.setTransactionSuccessful()!!
        }
        catch (e: Exception) {
            BlasLog.trace("E", "レコードの更新に失敗しました", e)
        }
        finally {
            db?.endTransaction()
        }

    }

    class ItemCheckException(message: String) : Exception(message) {}

    /**
     * 検品連動QRコード読込み後のチェック
     */
    fun qrCodeCheck(serialNumber: String?) {

        val sql = "select * from fixtures where serial_number = ?"
        val cursor = db?.rawQuery(sql, arrayOf(serialNumber))

        if( null == cursor ) {
            throw Exception("sqlite error!!!!")
        }

        val  count = cursor.count
        if( 0 == count ) {
            cursor.close()
            throw ItemCheckException("検品されていないシリアル番号です")
        }
        else {
            cursor.moveToFirst()
            val fixture = setProperty(Fixtures(), cursor) as Fixtures
            cursor.close()

            if( KENPIN_FIN == fixture.status || RTN == fixture.status ) {
                throw ItemCheckException("持ち出されていないシリアル番号です")
            }
            else if( SET_FIN == fixture.status ){
                throw ItemCheckException("設置済みのシリアル番号です")
            }
            else if( DONT_TAKE_OUT == fixture.status ) {
                throw ItemCheckException("持出不可のシリアル番号です")
            }
        }

    }

    fun rmQrCodeCheck(serialNumber: String?) {

        val sql = "select * from rm_fixtures where serial_number = ?"
        val cursor = db?.rawQuery(sql, arrayOf(serialNumber))

        if( null == cursor ) {
            throw Exception("sqlite error!!!!")
        }

        val  count = cursor.count
        if( 0 == count ) {
            cursor.close()
            throw ItemCheckException("撤去登録されていないシリアル番号です")
        }
        else {

            cursor.moveToFirst()
            val rm_fixture = setProperty(LdbRmFixtureRecord(), cursor) as LdbRmFixtureRecord
            cursor.close()

            // TODO:既存では特にこれ以上のチェックなし？？？
        }

    }

    fun setErrorMsg(itemId: String, errMsg: String) {
        val cv = ContentValues()
        cv.put("error_msg", errMsg)

        return try {
            db?.beginTransaction()
            db?.update("items", cv, "item_id = ?", arrayOf(itemId))
            db?.setTransactionSuccessful()
            db?.endTransaction()!!
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }


    fun delete(itemId: Long) {
        try {
            db?.beginTransaction()
            db?.delete("items", "itemId = ?", arrayOf(itemId.toString()))
            db?.setTransactionSuccessful()
            db?.endTransaction()
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }

    /**
     * 仮IDと新規IDのCSVファイルを作成する。
     * データの書式はヘッダなしで以下。
     * 仮ID,新規ID\n
     */
    fun writeCache(oldItemId:String, newItemId:String) {
        lock.withLock {
            //ログファイルの決定
            FileWriter(cacheFile, true).use {
                val record = "$oldItemId,$newItemId\n"
                it.write(record)
                it.flush()
            }
        }
    }

    fun getRealItemId(oldItemId:String):String? {
        var retItemId:String? = null
        var records:List<String>? = null

        lock.withLock {
            try {
                FileReader(cacheFile).use {
                    //面倒なので一回すべて読む
                    records = it.readLines()
                }
            }
            catch(e:java.lang.Exception) {
                BlasLog.trace("W", "${cacheFile}がありません", e)
            }
        }

        //仮IDと一致するレコードを探す
        val record = records?.firstOrNull() {
            val tokens = it.split(",", "\n")
            //引数の仮IDと一致したレコードを返す
            tokens[0] == oldItemId
        }

        if(record != null) {
            //新しいIDを返却する
            retItemId = record.split(",", "\n")[1]
        }

        return retItemId
    }

    /**
     * 不要なレコードを削除する
     */
    fun deleteCacheRecord(oldItemId:String) {
        var srcRecords:List<String>? = null
        var dstRecords = mutableListOf<String>()

        lock.withLock {
            FileReader(cacheFile).use {
                //面倒なので一回すべて読む
                srcRecords = it.readLines()
            }

            srcRecords?.forEach {
                val tokens = it.split(",", "\n")
                val old = tokens[0]
                if(old != oldItemId) {
                    //消さないレコードだけリストに再登録
                    dstRecords.add(it)
                }
            }

            //残ったレコードを書き直す
            FileWriter(cacheFile, false).use {fw->
                dstRecords.forEach {record->
                    fw.write(record+"\n")
                }
                fw.flush()
            }
        }
    }

    fun getParentFieldModelIndex(fields: MutableList<Any?>, parent_field_id:Int):Int {

        val index = fields.indexOfFirst {
            val model = (it as FieldModel)
            model.field.field_id == parent_field_id
        }

        return index
    }

    /*
    fun validate(fields: MutableList<Any?>):Boolean {
        //保存時のバリデーション
        var ret = true  //一つでもバリデーションに引っ掛かったら戻り値はfalseにする
        fields.forEachIndexed { index, f ->
            val model = (f as FieldModel)
            when(model.field.type.toString()){
                FieldType.CHECK_VALUE,
                FieldType.QR_CODE_WITH_CHECK-> {
                    //入力値チェック連動または入力値チェック連動_QRコード(検品と連動)
                    val childModel = (f as FieldCheckText)
                    if(model.field.parent_field_id != null) {
                        //親のフィールドIDが定義されている
                        val parentIndex = this.getParentFieldModelIndex(fields, model.field.parent_field_id!!)
                        val text = f.text.get()
                        if(RestHelper().isBlank(text.toString())) {
                            //テキスト領域に何も入力されていなかったら、無視する
                            ret = true
                        }
                        else if(parentIndex != -1) {

                            val memo = f.memo.get()
                            val type = (fields[parentIndex] as FieldModel).field.type.toString()
                            when(type) {
                                //親項目の値を取得する
                                FieldType.TEXT_FIELD,
                                FieldType.TEXT_AREA,
                                FieldType.DATE_TIME,
                                FieldType.TIME,
                                FieldType.LOCATION,
                                FieldType.KENPIN_RENDOU_QR,
                                FieldType.QR_CODE,
                                FieldType.TEKKYO_RENDOU_QR,
                                FieldType.ACOUNT_NAME,
                                FieldType.LAT_LOCATION,
                                FieldType.LNG_LOCATION,
                                FieldType.CURRENT_DATE_AND_TIME,
                                FieldType.WORKER_NAME,
                                FieldType.SCHEDULE_DATE,
                                FieldType.ADDRESS,
                                FieldType.EVENT_FIELD,
                                FieldType.BAR_CODE -> {
                                    val parentModel = (fields[parentIndex] as FieldText)
                                    val parentText = parentModel.text.get()
                                    if(parentText != childModel.text.get()) {
                                        //親項目の値と子項目の値が異なる
                                        if((memo != null) && (memo != "")) {
                                            //備考欄に入力がある
                                        }
                                        else {
                                            //親項目と異なり、備考欄に入力がない。NGのケース
                                            ret = false
                                            val colName = parentModel.field.name
                                            childModel.validationMsg.set("${colName}と値が異なります。値を修正するか、備考欄に理由を記載してください")
                                        }
                                    }
                                }

                                FieldType.SINGLE_SELECTION,
                                FieldType.MULTIPLE_SELECTION,
                                FieldType.CATEGORY_SELECTION,
                                FieldType.WORK_CONTENT_SELECTION->{
                                    //チェックボックスとラジオボタン
                                    val parentModel = (fields[parentIndex] as FieldMultiSelect)
                                }

                                FieldType.CHECK_VALUE->{
                                    //値チェック時
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret
    }*/
}