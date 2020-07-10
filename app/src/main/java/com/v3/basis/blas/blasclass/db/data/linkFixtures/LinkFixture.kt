package com.v3.basis.blas.blasclass.db.data.linkFixtures

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.v3.basis.blas.blasclass.db.data.Items

class LinkFixture(db : SQLiteDatabase?,item: Items, serialNumber:String)
    : LinkBase(db,item,serialNumber)
{

    override val table = "fixtures"

    override fun check(): Boolean {

        // TODO:ここでチェックと思ったけど、ItemViewModelの中でやらないと、元の画面に戻れんか・・・
        // TODO:検品されている機器か？
        // TODO:持出者と同じ設置者か？
        // TODO:持出できない機器ではないか？
        // TODO:持出確認が行われているか？
        // TODO:既に設置されていないか？

        return true
    }

    override fun createCV(): ContentValues {
        var cv = ContentValues()

        cv.put("item_id",item.item_id)
        cv.put("item_org_id",item.org_id)
        cv.put("item_user_id",item.user_id)
        cv.put("item_date",item.update_date)
        cv.put("status",2)  // 設置済み
//        cv.put("sync_status",2)

        return cv
    }
}