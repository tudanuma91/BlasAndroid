package com.v3.basis.blas.blasclass.db.data.linkFixtures

import android.content.ContentValues
//import android.database.sqlite.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase

import com.v3.basis.blas.blasclass.db.BaseController
import com.v3.basis.blas.blasclass.db.data.Items

class LinkRmFixture(db : SQLiteDatabase?, item: Items, serialNumber:String)
    : LinkBase(db,item,serialNumber)
{

    override val table = "rm_fixtures"

    override fun check(): Boolean {

        // TODO:該当レコードがあるか？
        // TODO:未撤去機器か？(=撤去済みではないか？)

        return true
    }

    override fun createCV(): ContentValues {
        var cv = ContentValues()

        cv.put("item_id",item.item_id)
        cv.put("rm_org_id",item.org_id)
        cv.put("rm_user_id",item.user_id)
        cv.put("rm_date",item.update_date)
        cv.put("status", BaseController.REMOVE)  // 現場撤去
        cv.put("sync_status", BaseController.SYNC_STATUS_EDIT )
        cv.put("error_msg", "送信待ちです")


        return cv
    }


}