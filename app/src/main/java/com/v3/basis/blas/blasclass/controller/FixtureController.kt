package com.v3.basis.blas.blasclass.controller

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.v3.basis.blas.blasclass.db.*
import com.v3.basis.blas.blasclass.ldb.LdbFixtureDispRecord
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.ldb.LdbUserRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.Exception

class FixtureController(context: Context, projectId: String): BaseController(context, projectId) {

    /**
     * 機器一覧を表示するためのSQL
     */
    private val searchDispFixtureSql  =
        "select fixtures.*" +
            ",o_fix.name as fix_org_name,u_fix.name as fix_user_name " +
            ",o_takeout.name as takeout_org_name,u_takeout.name as takeout_user_name" +
            ",o_rtn.name as rtn_org_name,u_rtn.name as rtn_user_name" +
            ",o_item.name as item_org_name,u_item.name as item_user_name" +
            " from fixtures " +
            " left join orgs as o_fix on fixtures.fix_org_id = o_fix.org_id" +
            " left join users as u_fix on fixtures.fix_user_id = u_fix.user_id" +
            " left join orgs as o_takeout on fixtures.takeout_org_id = o_takeout.org_id" +
            " left join users as u_takeout on fixtures.takeout_user_id = u_takeout.user_id" +
            " left join orgs as o_rtn on fixtures.rtn_org_id = o_rtn.org_id" +
            " left join users as u_rtn on fixtures.rtn_user_id = u_rtn.user_id " +
            " left join orgs as o_item on fixtures.item_org_id = o_item.org_id" +
            " left join users as u_item on fixtures.item_user_id = u_item.user_id"

    private val keyChanger = mapOf(
        "serial_number" to "fixtures.serial_number"
        ,"fixture_id" to "fixtures.fixture_id"
        ,"FixOrg" to "o_fix.name"
        ,"FixUser" to "u_fix.name"
        ,"kenpinDayMin" to "fixtures.fix_date"
        ,"kenpinDayMax" to "fixtures.fix_date"
        ,"TakeOutOrg" to "o_takeout.name"
        ,"TakeOutUser" to "u_takeout.name"
        ,"takeOutDayMin" to "fixtures.takeout_date"
        ,"takeOutDayMax" to "fixtures.takeout_date"
        ,"RtnOrg" to "o_rtn.name"
        ,"RtnUser" to "u_rtn.name"
        ,"returnDayMin" to "fixtures.rtn_date"
        ,"returnDayMax" to "fixtures.rtn_date"
        ,"ItemOrg" to "o_item.name"
        ,"ItemUser" to "u_item.name"
        ,"itemDayMin" to "fixtures.item_date"
        ,"itemDayMax" to "fixtures.item_date"
        ,"status" to "fixtures.status"
    )

    private lateinit var additionList : Array<String>
    private lateinit var plHolder : Array<String>

    /**
     * 自社だけ表示を判断
     */
    private fun chgeckLimitMyOrg() {
        val user = getUserInfo()
        var groupId = 1

        if( null != user  ) {
            groupId = user.group_id
        }
        Log.d("group_id取得！！！",groupId.toString())

        val fixtureDispRange = getGroupsValue(groupId,"fixture_disp_range")
        Log.d("fixtureDispRange",fixtureDispRange.toString())

        if( 0 == fixtureDispRange ) {
            // projectの設定に従う
            val showData = getProjectVlue("show_data")
            if( 1 == showData ) {
                // 自分の会社分しか見れない
                val myOrgId = user?.org_id
                additionList += " fixtures.fix_org_id = ? "
                plHolder += myOrgId.toString()
            }
        }

    }

    /**
     * 検索条件の生成
     */
    private fun createAddition(searchMap: Map<String, String?>) {
        Log.d("searchMap",searchMap.toString())

        searchMap.forEach {
            if( null == it.value || "" == it.value ) {
                return@forEach
            }

            when(it.key) {
                "freeWord" -> {
                    // 何もしない
                    return@forEach
                }
                "kenpinDayMin","takeOutDayMin","returnDayMin","itemDayMin" -> {
                    additionList += keyChanger[it.key] + " >= ? "
                    plHolder += it.value + " 00:00:00"
                }
                "kenpinDayMax","takeOutDayMax","returnDayMax","itemDayMax" -> {
                    additionList += keyChanger[it.key] + " < ? "
                    plHolder += it.value + " 00:00:00"
                }
                "status" -> {
                    when( it.value ) {
                        "持出可" -> {
                            additionList += "(" + keyChanger[it.key] + " = 0 or "+ keyChanger[it.key] + " = 4)"
                        }
                        "持出中" -> {
                            additionList += keyChanger[it.key] + " = 1 "

                        }
                        "設置済" -> {
                            additionList += keyChanger[it.key] + " = 2 "

                        }
                        "持出不可" -> {
                            additionList += keyChanger[it.key] + " = 3 "
                        }
                        else -> {
                            return@forEach
                        }
                    }
                }

                else -> {
                    additionList += keyChanger[it.key] + " like ? "
                    plHolder += "%" +  it.value + "%"
                }
            }
        }
    }

    /**
     * where文を生成する
     */
    private fun createSqlWhere(additionList : Array<String> ) : String {

        var sqlAdition = ""
        if( additionList.count() > 0 ) {
            var first = true

            sqlAdition += " where "
            additionList.forEach {
                if( !first ) {
                    sqlAdition += " and "
                }
                sqlAdition += it
                first = false
            }
        }

        return  sqlAdition
    }


    /**
     * (表示用：ユーザー、会社の結付あり)機器一覧の取得
     */
    fun searchDisp( offset: Int = 0, paging: Int = 20, searchMap: Map<String, String?>): List<LdbFixtureDispRecord> {
        Log.d("search","start!!!!!!  offset:" + offset + "  paging:" + paging)

        // 初期化
        additionList = arrayOf<String>()
        plHolder = arrayOf<String>()

        chgeckLimitMyOrg()
        createAddition(searchMap)

        // where文を生成
        val sqlWhere = createSqlWhere( additionList )

        // limit,offset
        plHolder += paging.toString()
        plHolder += offset.toString()

        val sql = searchDispFixtureSql + sqlWhere  + " order by fixtures.create_date desc limit ? offset ? "
        Log.d("search fixture sql",sql)
//        Log.d("plHolder",plHolder.toString())

        val cursor = db?.rawQuery(sql, plHolder)
        val ret = mutableListOf<LdbFixtureDispRecord>()
        cursor?.also { c_now ->
            var notLast = c_now.moveToFirst()
            while (notLast) {
                val fix = setProperty(LdbFixtureDispRecord() ,c_now)  as  LdbFixtureDispRecord
                ret.add(fix)
                notLast = c_now.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /**
     * (内部処理用:ユーザー、会社の結付なし)機器一覧情報の取得
     */
    fun search( fixture_id : Long? = null,syncFlg : Boolean = false) : List<LdbFixtureRecord> {

        val addList = mutableListOf<String>()
        var sqlAddition = ""
        var plHolder  = arrayOf<String>()

        if( null != fixture_id ) {
            addList += " fixture_id = ? "
            plHolder += fixture_id.toString()
        }
        if( syncFlg ) {
            addList += " sync_status > 0 "
        }
        if( addList.count() > 0 ) {
            var first = true
            sqlAddition += " where "
            addList.forEach {
                if( !first ) {
                    sqlAddition += " and "
                }
                sqlAddition += it
                first = true
            }
        }

        val sql = "select * from fixtures " + sqlAddition

        Log.d("search sql",sql)

        val cursor = db?.rawQuery(sql, plHolder)
        val ret = mutableListOf<LdbFixtureRecord>()
        cursor?.also { c_now ->
            var notLast = c_now.moveToFirst()
            while (notLast) {
                val fix = setProperty(LdbFixtureRecord() ,c_now)  as  LdbFixtureRecord
                ret.add(fix)
                notLast = c_now.moveToNext()
            }
        }
        cursor?.close()

        return ret
    }

    /*
    //  BlasRestFixture.createはアプリからも使ってない。
    fun create(fixtures: Fixtures): Boolean {

        return true
    }

    //  BlasRestFixture.updateはアプリからも使ってない。
    fun update(fixtures: Fixtures): Boolean {

        return false
    }

    //  BlasRestFixture.deleteはアプリからも使ってない。
    fun delete(fixtures: Fixtures): Boolean {
        return false
    }
     */

    /**
     * シリアルナンバーがDB中に存在するか確認する
     */
    private fun checkExistSerial( serial_number:String ) : Boolean {

        var ret = false
        val sql = "select count(*) as count from fixtures where serial_number = ?"
        val cursor = db?.rawQuery(sql, arrayOf(serial_number))

        var count : Int = 0
        cursor?.also {
            it.moveToFirst()
            count = it.getInt( it.getColumnIndex("count") )
        }
        cursor?.close()

        if( count > 0 ) {
            ret = true
        }

        return ret
    }

    /**
     * 検品に伴う新規レコード作成
     */
    private fun kenpin_insert( serial_number: String) : Boolean {
        // user情報を取得する
        val user = getUserInfo()

        val new_fixture = LdbFixtureRecord()
        new_fixture.project_id = projectId.toInt()

        if( null != user ) {
            new_fixture.fix_org_id = user.org_id
            new_fixture.fix_user_id = user.user_id
        }
        else {
            new_fixture.fix_org_id = 1
            new_fixture.fix_user_id = 1
        }

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        new_fixture.fixture_id = createTempId()
        new_fixture.fix_date = current.format(formatter)
        new_fixture.serial_number = serial_number
        new_fixture.status = KENPIN_FIN
        new_fixture.create_date = current.format(formatter)
        new_fixture.update_date = current.format(formatter)
        new_fixture.sync_status = SYNC_STATUS_NEW

//        val exceptList = listOf("fixture_id")
        val cv = createConvertValue(new_fixture)

        return try {
            db?.beginTransaction()
            //db.execSQL("INSERT into fixtures(serial_number) values (?)", arrayOf(serial_number))
            db?.insert("fixtures",null,cv)

            db?.setTransactionSuccessful()
            Log.d("kenpin","insert 成功！！")
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            Log.d("kenpin","Exception 発生！！！ " + e.message)
            e.printStackTrace()
            false
        }
        finally {
            db?.endTransaction()
        }

    }

    /**
     * 既存レコードに対する再検品(他社異動など)
     */
    private fun kenpin_update( serial_number: String,fixture:LdbFixtureRecord,user:LdbUserRecord ) : Boolean {

        if( null != user?.user_id )
            fixture.fix_user_id = user.user_id
        if( null != user?.org_id )
            fixture.fix_org_id = user.org_id

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        fixture.fix_date = current.format(formatter)
        fixture.update_date = current.format(formatter)
        fixture.sync_status = SYNC_STATUS_EDIT

        val cv = createConvertValue(fixture,null)

        return try {
            db?.beginTransaction()
            db?.update("fixtures",cv,"serial_number = ?", arrayOf(serial_number))
            db?.setTransactionSuccessful()
            Log.d("kenpin","update 成功！！")
            true
        }
        catch ( ex : Exception) {
            false
        }
        finally {
            db?.endTransaction()
        }

    }

    /**
     * 指定されたシリアルナンバーのFixtureレコードを返す
     */
    private fun getEqualFixtureInfo(serial_number: String )  : LdbFixtureRecord? {

        val sql = "select * from fixtures where serial_number = ?"
        val cursor = db?.rawQuery(sql, arrayOf(serial_number))

        if( 0 == cursor?.count ) {
            return null
        }

        var fixture : LdbFixtureRecord? = null

        cursor?.also {
            it.moveToFirst()
            fixture = setProperty( LdbFixtureRecord(),it ) as LdbFixtureRecord
         }

        return fixture
    }

    /**
     * パーサーを適用する処理
     */
    fun passPurser( serial_number:String ) : MutableList<String> {

        val purserType = getProjectVlue("purser_type")

        lateinit var newSerialNumber:MutableList<String>
        if( 0 == purserType ) {

            newSerialNumber = Purser().encode(serial_number)
        }
        else if( 1 == purserType ) {
            // PURSER_CSV
            newSerialNumber = PurserCSV().encode(serial_number)
        }
        else if(2 == purserType) {
            // PURSER_SPACE
            newSerialNumber = PurserNCU().encode(serial_number)
        }
        else if( 3 == purserType ) {
            // PURSER_REG
            //TODO:未実装
            newSerialNumber = mutableListOf(serial_number)
        }
        else if( 4 == purserType) {
            // PURSER_CSV_FIRST
            newSerialNumber = PurserCSVFirst().encode(serial_number)

        }
        else {
            throw Exception()
        }

        return newSerialNumber
    }

    /**
     * 検品処理
     */
    fun kenpin( serial_number: String): Boolean {
        Log.d("kenpin","start")
        var ret = false

        val serial_numbers = passPurser( serial_number )

        serial_numbers.forEach {
            // fixtureテーブル 同じserial_numberが存在しないかを確認
            if (checkExistSerial(it)) {
                // ある場合
                Log.d("kenpin", "同一シリアルが登録済み")
                var user: LdbUserRecord? = getUserInfo()
                if (null == user) {
                    user = LdbUserRecord()
                    user.user_id = 1
                    user.org_id = 1
                }

                var fixture = getEqualFixtureInfo(it)

                if (fixture?.fix_org_id != user?.org_id && 0 == fixture?.status) {
                    Log.d("kenpin", "他社検品を異動")
                    // 他社が検品、持ち出し可なら ⇒ 異動
                    ret = kenpin_update(it, fixture, user)

                    return ret
                } else {
                    // 検品不可
                    Log.d("kenpin", "検品済みです")
                    errorMessageEvent.onNext("検品済みです")

                    return false
                }
                // なければ新規追加
                Log.d("kenpin", "存在しないシリアルなので新規作成する")
                errorMessageEvent.onNext("")
            }
            ret = kenpin_insert(it)
            if(!ret) {
                return false
            }
        }

        return ret
    }

    /**
     * 持ち帰り処理の確認
     */
    private fun checkTakeout(fixture: LdbFixtureRecord?,user: LdbUserRecord?) : Boolean {
        if( null == fixture ) {
            Log.d("takeout message!","未登録のシリアルナンバーです")
            errorMessageEvent.onNext("未登録のシリアルナンバーです")
            return false
        }

        if( SYNC_STATUS_SYNC != fixture.sync_status ) {
            Log.d("takeout message!","サーバー同期待ちのシリアルナンバーです")
            errorMessageEvent.onNext("サーバー同期待ちのシリアルナンバーです")
            return false
        }


        // ステータスが検品済み and 同じ会社で検品されているか？
        if( KENPIN_FIN != fixture?.status && RTN != fixture?.status ) {

            if( 1 == fixture?.status ) {
                Log.d("takeout message!","すでに持ち出し中です")
                errorMessageEvent.onNext("すでに持ち出し中です")
            }
            else if( 2 == fixture?.status ) {
                Log.d("takeout message!","すでに設置済みです")
                errorMessageEvent.onNext("すでに設置済みです")
            }
            else if( 3 == fixture?.status ) {
                Log.d("takeout message!","持出不可です")
                errorMessageEvent.onNext("持出不可です")
            }
            return false
        }

        if( fixture?.fix_org_id != user?.org_id ) {
            Log.d("takeout message!","異なる会社で検品されています")
            errorMessageEvent.onNext("異なる会社で検品されています")
            return false
        }

        errorMessageEvent.onNext("")
        return true
    }

    /**
     * 持ち帰り処理
     */
    fun takeout(serial_number: String): Boolean {

        val serial_numbers = passPurser( serial_number )

        serial_numbers.forEach {
            // 該当シリアルナンバーの機器情報を取得
            var fixture = getEqualFixtureInfo(it)
            var user: LdbUserRecord? = getUserInfo()
            if (null == user) {
                user = LdbUserRecord()
                user.user_id = 1
                user.org_id = 1
            }

            if (!checkTakeout(fixture, user)) {
                return false
            }

            fixture!!.takeout_user_id = user.user_id
            fixture!!.takeout_org_id = user.org_id

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

            fixture!!.takeout_date = current.format(formatter)
            fixture!!.update_date = current.format(formatter)
            fixture!!.status = TAKING_OUT
            fixture!!.sync_status = SYNC_STATUS_EDIT

            val cv = createConvertValue(fixture as Any, null)

            return try {
                db?.beginTransaction()
                //db.execSQL("UPDATE fixtures set status = 1 where serial_number = ?", arrayOf(serial_number))
                db?.update("fixtures", cv, "serial_number = ?", arrayOf(it))
                db?.setTransactionSuccessful()
                Log.d("takeout", "成功！！！")
                errorMessageEvent.onNext("")
                true
            } catch (e: Exception) {
                //とりあえず例外をキャッチして、Falseを返す？
                Log.d("takeout", "db error")
                e.printStackTrace()
                false
            } finally {
                db?.endTransaction()
            }
        }
        return false
    }

    /**
     * 返却処理の確認
     */
    // TODO:takeoutと殆ど同じだから工夫しろ！
    private fun checkuRtn(fixture: LdbFixtureRecord?,user: LdbUserRecord?) : Boolean {

        if( null == fixture ) {
            Log.d("takeout message!","未登録のシリアルナンバーです")
            errorMessageEvent.onNext("未登録のシリアルナンバーです")

            return false
        }

        if(  SYNC_STATUS_SYNC != fixture.sync_status ) {
            Log.d("takeout message!","サーバー同期待ちのシリアルナンバーです")
            errorMessageEvent.onNext("サーバー同期待ちのシリアルナンバーです")
            return false
        }

        // ステータスが検品済み and 同じ会社で検品されているか？
        if( TAKING_OUT != fixture?.status ) {

            if( KENPIN_FIN == fixture?.status ) {    // TODO:ここだけtakeoutと違う
                Log.d("takeout message!","持出確認が行われていません")
                errorMessageEvent.onNext("持出確認が行われていません")
            }
            else if( SET_FIN == fixture?.status ) {
                Log.d("takeout message!","すでに設置済みです")
                errorMessageEvent.onNext("すでに設置済みです")
            }
            else if( DONT_TAKE_OUT == fixture?.status ) {
                Log.d("takeout message!","持出不可です")
                errorMessageEvent.onNext("持出不可です")
            }
            return false
        }

        if( fixture?.fix_org_id != user?.org_id ) {
            Log.d("takeout message!","異なる会社で検品されています")
            errorMessageEvent.onNext("異なる会社で検品されています")
            return false
        }

        errorMessageEvent.onNext("")
        return true
    }

    /**
     * 返却処理
     */
    fun rtn(serial_number: String): Boolean {

        val serial_numbers = passPurser( serial_number )

        serial_numbers.forEach {
            // 該当シリアルナンバーの機器情報を取得
            var fixture = getEqualFixtureInfo(it)
            var user : LdbUserRecord? = getUserInfo()
            if(null == user) {
                user = LdbUserRecord()
                user.user_id = 1
                user.org_id = 1
            }

            if( !checkuRtn(fixture,user) ) {
                return false
            }

            fixture!!.rtn_user_id = user.user_id
            fixture!!.rtn_org_id = user.org_id

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

            fixture!!.rtn_date = current.format(formatter)
            fixture!!.update_date = current.format(formatter)
            fixture!!.status = RTN
            fixture!!.sync_status = SYNC_STATUS_EDIT

            val cv = createConvertValue(fixture,null)

            return try {
                db?.beginTransaction()
                // db.execSQL("UPDATE fixtures set status = 2 where serial_number = ?", arrayOf(serial_number))
                db?.update("fixtures",cv,"serial_number = ?", arrayOf(it))

                db?.setTransactionSuccessful()
                Log.d("rtn","成功！！！")
                true
            } catch (e: Exception) {
                //とりあえず例外をキャッチして、Falseを返す？
                e.printStackTrace()
                false
            }
            finally {
                db?.endTransaction()
            }
        }
        return false
    }

    /**
     * 仮IDをサーバーから取得した正しいIDに直す
     */
    fun updateFixtureId( oldFixtureId:String, newFixtureId:String) : Boolean {

        val cv = ContentValues()
        cv.put("fixture_id",newFixtureId)
        cv.put("sync_status", SYNC_STATUS_SYNC)
        cv.put("error_msg", "")


        return try {
            db?.beginTransaction()
            db?.update("fixtures",cv,"fixture_id = ?", arrayOf(oldFixtureId))
            db?.setTransactionSuccessful()
            db?.endTransaction()
            Log.d("update","成功！！")
            true
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * sqliteのsync_status(同期状況)を0(何もなし)に戻す
     */
    fun resetSyncStatus( fixtureId:String ) : Boolean {

        val cv = ContentValues()
        cv.put("sync_status", SYNC_STATUS_SYNC)

        return try {
            db?.beginTransaction()
            db?.update("fixtures",cv,"fixture_id = ?", arrayOf(fixtureId))
            db?.setTransactionSuccessful()
            db?.endTransaction()
            Log.d("sync_status reset","成功！！")
            true
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            false
        }

    }

    fun setErrorMsg( fixtureId: String,errMsg : String ) {

        val cv = ContentValues()
        cv.put("error_msg",errMsg)

        return try {
            db?.beginTransaction()
            db?.update("fixtures",cv,"fixture_id = ?", arrayOf(fixtureId))
            db?.setTransactionSuccessful()
            db?.endTransaction()!!
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            throw ex
        }
    }

    fun delete(fixtureId:Long) {
       try {
           db?.beginTransaction()
           db?.delete("fixtures", "fixture_id = ?", arrayOf(fixtureId.toString()))
           db?.setTransactionSuccessful()
           db?.endTransaction()
       }
       catch ( ex : Exception ) {
           ex.printStackTrace()
           throw ex
       }
    }
}
