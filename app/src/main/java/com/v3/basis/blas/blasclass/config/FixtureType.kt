package com.v3.basis.blas.blasclass.config

class FixtureType {
    companion object {
        //機器管理のステータス全般
        val canTakeOut:String = "0"
        val takeOut:String = "1"
        val finishInstall:String = "2"
        val notTakeOut:String = "3"

        //ステータスの日本語訳
        val statusCanTakeOut = "持出可"
        val statusTakeOut = "持出中"
        val statusFinishInstall = "設置済"
        val statusNotTakeOut = "持出不可"
        val statusAll = "すべて"

        //restのステータス
        val kenpin : String = "0"
        val returns : String = "2"

    }
}