package com.v3.basis.blas.blasclass.db.fixture

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.Purser
import com.v3.basis.blas.blasclass.ldb.LdbFixtureDispRecord
import com.v3.basis.blas.blasclass.ldb.LdbFixtureRecord
import com.v3.basis.blas.blasclass.ldb.LdbUserRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.Exception

class FixtureController(context: Context, projectId: String): BaseController(context, projectId) {

    fun joinTest(): List<FixturesAndUsers> {
        val db = openDatabase()
        return db.fixtureDao().selectJoinUsers()
    }

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


    fun searchDisp(): List<LdbFixtureDispRecord> {

        val db = openSQLiteDatabase()
        val user = getUserInfo(db)
        var groupId = 1

        if( null != user  ) {
            groupId = user.group_id
        }
        Log.d("group_id取得！！！",groupId.toString())
        val fixtureDispRange = getGroupsValue(db,groupId,"fixture_disp_range")
        Log.d("fixtureDispRange",fixtureDispRange.toString())

        var sqlAdition = ""
        var plHolder  = arrayOf<String>()

        if( 0 == fixtureDispRange ) {
            // projectの設定に従う
            val showData = getProjectVlue(db,"show_data")
            if( 1 == showData ) {
                // 自分の会社分しか見れない
                //val myOrgId = getUserInfo(db,"org_id")
                val myOrgId = user?.org_id
                sqlAdition = " where fixtures.fix_org_id = ?"
                plHolder += myOrgId.toString()
            }
        }

        val sql = searchDispFixtureSql + sqlAdition + " order by fixtures.create_date desc"

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

    fun search( fixture_id : Long? = null  ) : List<LdbFixtureRecord> {

        val db = openSQLiteDatabase()

        var sqlAddition = ""
        var plHolder  = arrayOf<String>()
        if( null != fixture_id ) {
            sqlAddition = "where fixture_id = ?"
            plHolder += fixture_id.toString()
        }

        val sql = "select * from fixtures " + sqlAddition

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

    private fun checkExistSerail( db: SQLiteDatabase, serial_number:String ) : Boolean {

        var ret = false
        val sql = "select count(*) as count from fixtures where serial_number = ?"
        val cursor = db?.rawQuery(sql, arrayOf(serial_number))

        var count : Int = 0
        cursor?.also {
            it.moveToFirst()
            count = it.getInt( it.getColumnIndex("count") )
        }
        cursor.close()

        if( count > 0 ) {
            ret = true
        }

        return ret
    }

    private fun kenpin_insert( db:SQLiteDatabase ,serial_number: String) : Boolean {
        // user情報を取得する
        val user = getUserInfo(db)

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
        new_fixture.status = 0
        new_fixture.create_date = current.format(formatter)
        new_fixture.update_date = current.format(formatter)
        new_fixture.sync_status = 1

//        val exceptList = listOf("fixture_id")
        val cv = createConvertValue(new_fixture)

        return try {
            db.beginTransaction()
            //db.execSQL("INSERT into fixtures(serial_number) values (?)", arrayOf(serial_number))
            db.insert("fixtures",null,cv)

            db.setTransactionSuccessful()
            db.endTransaction()
            Log.d("kenpin","insert 成功！！")
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            Log.d("kenpin","Exception 発生！！！ " + e.message)
            e.printStackTrace()
            false
        }

    }

    private fun kenpin_update( db:SQLiteDatabase,serial_number: String,fixture:LdbFixtureRecord,user:LdbUserRecord ) : Boolean {

        if( null != user?.user_id )
            fixture.fix_user_id = user.user_id
        if( null != user?.org_id )
            fixture.fix_org_id = user.org_id

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        fixture.fix_date = current.format(formatter)
        fixture.update_date = current.format(formatter)
        fixture.sync_status = 2

        val cv = createConvertValue(fixture,null)

        return try {
            db.beginTransaction()
            db.update("fixtures",cv,"serial_number = ?", arrayOf(serial_number))
            db.setTransactionSuccessful()
            db.endTransaction()
            Log.d("kenpin","update 成功！！")
            true
        }
        catch ( ex : Exception) {
            false
        }

    }



    private fun getEqualFixtureInfo(db:SQLiteDatabase, serial_number: String )  : LdbFixtureRecord? {

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

    fun passPurser( db:SQLiteDatabase, serial_number:String ) : String {

        val purserType = getProjectVlue(db,"purser_type")

        var newSerialNumber:String = ""
        if( 0 == purserType ) {
            newSerialNumber = Purser().encode(serial_number)
        }
        else if( 1 == purserType ) {
            // PURSER_CSV
            newSerialNumber = Purser().encode(serial_number)
        }
        else if(2 == purserType) {
            // PURSER_SPACE
            //TODO:未実装
            newSerialNumber = serial_number
        }
        else if( 3 == purserType ) {
            // PURSER_REG
            //TODO:未実装
            newSerialNumber = serial_number
        }
        else if( 4 == purserType) {
            // PURSER_CSV_FIRST
            //TODO:未実装
            newSerialNumber = serial_number

        }
        else {
            throw Exception()
        }

        return newSerialNumber
    }


    fun kenpin( serial_number: String): Boolean {
        Log.d("kenpin","start")
        var ret = false

        val db = openSQLiteDatabase()
        db ?: return false

        val serial_number = passPurser( db,serial_number )

        // fixtureテーブル 同じserial_numberが存在しないかを確認
        if(  checkExistSerail(db,serial_number) ){
            // ある場合
            Log.d("kenpin","同一シリアルが登録済み")
            var user : LdbUserRecord? = getUserInfo(db)
            if(null == user) {
                user = LdbUserRecord()
                user.user_id = 1
                user.org_id = 1
            }

            var fixture = getEqualFixtureInfo(db,serial_number)

            if( fixture?.fix_org_id != user?.org_id && 0 == fixture?.status ) {
                Log.d("kenpin","他社検品を異動")
                // 他社が検品、持ち出し可なら ⇒ 異動
                ret = kenpin_update(db,serial_number,fixture,user)

                db.close()
                return ret
            }
            else {
                // 検品不可
                Log.d("kenpin","検品不可です")
                errorMessageEvent.onNext("検品不可です")

                db.close()
                return false
            }
        }

        // なければ新規追加
        Log.d("kenpin","存在しないシリアルなので新規作成する")

        ret = kenpin_insert(db,serial_number)
        db.close()

        return ret
    }


    private fun checkTakeout(fixture: LdbFixtureRecord?,user: LdbUserRecord?) : Boolean {
        if( null == fixture ) {
            Log.d("takeout message!","未登録のシリアルナンバーです")
            errorMessageEvent.onNext("未登録のシリアルナンバーです")

            return false
        }
        // ステータスが検品済み and 同じ会社で検品されているか？
        if( 0 != fixture?.status ) {

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

        return true
    }

    fun takeout(serial_number: String): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        val serial_number = passPurser( db,serial_number )

        // 該当シリアルナンバーの機器情報を取得
        var fixture = getEqualFixtureInfo(db,serial_number)
        var user : LdbUserRecord? = getUserInfo(db)
        if(null == user) {
            user = LdbUserRecord()
            user.user_id = 1
            user.org_id = 1
        }

        if( !checkTakeout(fixture,user) ) {
            db.close()
            return false
        }

        fixture!!.takeout_user_id = user.user_id
        fixture!!.takeout_org_id = user.org_id

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        fixture!!.takeout_date = current.format(formatter)
        fixture!!.update_date = current.format(formatter)
        fixture!!.status = 1
        fixture!!.sync_status = 2

        val cv = createConvertValue(fixture as Any,null)

        return try {
            db.beginTransaction()
            //db.execSQL("UPDATE fixtures set status = 1 where serial_number = ?", arrayOf(serial_number))
            db.update("fixtures",cv,"serial_number = ?", arrayOf(serial_number))
            db.setTransactionSuccessful()
            db.endTransaction()
            Log.d("takeout","成功！！！")
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
        finally {
            db.close()
        }
    }


    // TODO:takeoutと殆ど同じだから工夫しろ！
    private fun checkuRtn(fixture: LdbFixtureRecord?,user: LdbUserRecord?) : Boolean {

        if( null == fixture ) {
            Log.d("takeout message!","未登録のシリアルナンバーです")
            errorMessageEvent.onNext("未登録のシリアルナンバーです")

            return false
        }

        // ステータスが検品済み and 同じ会社で検品されているか？
        if( 1 != fixture?.status ) {

            if( 0 == fixture?.status ) {    // TODO:ここだけtakeoutと違う
                Log.d("takeout message!","持出確認が行われていません")
                errorMessageEvent.onNext("持出確認が行われていません")
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


        return true
    }

    fun rtn(serial_number: String): Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        val serial_number = passPurser( db,serial_number )

        // 該当シリアルナンバーの機器情報を取得
        var fixture = getEqualFixtureInfo(db,serial_number)
        var user : LdbUserRecord? = getUserInfo(db)
        if(null == user) {
            user = LdbUserRecord()
            user.user_id = 1
            user.org_id = 1
        }

        if( !checkuRtn(fixture,user) ) {
            db.close()
            return false
        }

        fixture!!.rtn_user_id = user.user_id
        fixture!!.rtn_org_id = user.org_id

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")

        fixture!!.rtn_date = current.format(formatter)
        fixture!!.update_date = current.format(formatter)
        fixture!!.status = 4
        fixture!!.sync_status = 2

        val cv = createConvertValue(fixture,null)

        return try {
            db.beginTransaction()
            // db.execSQL("UPDATE fixtures set status = 2 where serial_number = ?", arrayOf(serial_number))
            db.update("fixtures",cv,"serial_number = ?", arrayOf(serial_number))

            db.setTransactionSuccessful()
            db.endTransaction()
            Log.d("rtn","成功！！！")
            true
        } catch (e: Exception) {
            //とりあえず例外をキャッチして、Falseを返す？
            e.printStackTrace()
            false
        }
        finally {
            db.close()
        }
    }

    fun updateFixtureId( orgFixtureId:String, newFixtureId:String) : Boolean {
        val db = openSQLiteDatabase()
        db ?: return false

        val cv = ContentValues()
        cv.put("fixture_id",newFixtureId)
        cv.put("sync_status",0)

        return try {
            db.beginTransaction()
            db.update("fixtures",cv,"fixture_id = ?", arrayOf(orgFixtureId))
            db.setTransactionSuccessful()
            db.endTransaction()
            Log.d("update","成功！！")
            true
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            false
        }
        finally {
            db.close()
        }
    }

    fun resetSyncStatus( fixtureId:String ) : Boolean {

        val db = openSQLiteDatabase()
        db ?: return false

        val cv = ContentValues()
        cv.put("sync_status",0)

        return try {
            db.beginTransaction()
            db.update("fixtures",cv,"fixture_id = ?", arrayOf(fixtureId))
            db.setTransactionSuccessful()
            db.endTransaction()
            Log.d("sync_status reset","成功！！")
            true
        }
        catch ( ex : Exception ) {
            ex.printStackTrace()
            false
        }
        finally {
            db.close()
        }

    }

}
