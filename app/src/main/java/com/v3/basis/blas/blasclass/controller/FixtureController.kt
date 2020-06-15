package com.v3.basis.blas.blasclass.controller

import android.content.Context

class FixtureController() : DataController() {

    /**
     * 機器管理テーブルを検索する
     * スレッドで起動する
     */

    public fun find(context: Context,                       //コンテキスト
                    projectId:Int,                          //プロジェクトID
                    conditions:MutableMap<String, String>,  //検索条件
                    order:String,                           //order by
                    page:Int,                               //ページネーションのページ
                    limit:Int) {                            //1ページあたりのレコード数
        val dbName:String = "${projectId}.db"

    }
}