package com.v3.basis.blas.blasclass.app

import android.util.Log
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.config.FixtureType
import com.v3.basis.blas.blasclass.rest.RestfulRtn
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BlasCom {

}

    /**
     * オブジェクトをJSON文字列に変換するメソッド
     * [引数]
     * stream(オブジェクト) :　restful通信にて取得したデータ。
     *
     * [返り値]
     * sb.toString(文字列) : streamを文字列にして返す。
     * */
    fun Is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
        Log.d("[rest/BlasRest]","{$reader}")
        var line = reader.readLine()
        if(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    /**
     * cakePHPから返却されたデータをandroidで使用しやすい形式に変換する。
     * @param jsonRecord 文字列形式のjson
     * @param tableName cakePHPのテーブル名
     * @return RestfulRtnクラス(データクラス)
     */
    public fun cakeToAndroid(jsonRecord:String, tableName:String): RestfulRtn {
        //返却用エラーコード
        var errorCode = 0
        //返却用メッセージ
        var message = ""
        //返却用リスト
        var recordList:MutableList<MutableMap<String, String?>>? = mutableListOf<MutableMap<String, String?>>()

        try {
            val root = JSONObject(jsonRecord)
            //エラーコード取得
            errorCode = root.getInt("error_code")
            //メッセージ取得
            message = root.getString("message")

            if(errorCode == 0) {
                //正常時だけレコードがあるため、取得する
                val records = root.getJSONArray("records")

                for (i in 0 until records.length()) {

                    var fields = JSONObject(records[i].toString())

                    for (j in 0 until fields.length()) {
                        var data = JSONObject(fields[tableName].toString())  //指定されたテーブルを取得する
                        var recordMap = mutableMapOf<String, String?>()
                        for (k in data.keys()) {
                            recordMap[k] = data[k].toString()
                        }
                        if(recordList != null) {
                            recordList.add(recordMap)
                        }
                    }
                }
            }
            else {
                recordList = null
            }
        }
        catch(e: JSONException) {
            Log.d("konishi", e.message)
            recordList = null
        }
        return RestfulRtn(errorCode, message, recordList)
    }

    /**
     * 検索を行う関数
     * @param cond 検索条件のmap(key:項目名 value:検索ワード)
     * @param search 検索する対象
     * @return result 検索結果
     */
    public fun searchAndroid(cond:MutableMap<String, String?>, search:MutableList<MutableMap<String, String?>>): MutableList<MutableMap<String, String?>> {
        //TODO:検索の条件をしっかり把握すること(例：大文字・小文字・半角・全角)
        //大文字小文字は別文字として判別している。
        //フリーワード検索は、データ管理・機器管理ともに同じ処理を行う。

        val result = search.toMutableList()
        val chkList :MutableList<Int> = mutableListOf()
        val removeIdList:MutableList<MutableList<Int>> = mutableListOf()

        for ((condKey, condValue) in cond) {
            //検索条件がフリーワードかつ文字が入力されているとき
           // val key = Regex(condKey)
            val fld = Regex("fld")
            if(condKey =="freeWord" && condValue!=""){
                Log.d("検索処理","分岐完了")
                for (idx in 0 until result.size) {
                    var hitFlg = false
                    //ここエラー発生した。
                    // 予想だけどfor文回しているときに配列操作するとsizeが違うってエラーあり。
                    for ((resKey, resValue) in result[idx]) {
                        //item_idは検索に含まない
                        if(resKey != "item_id"){
                            val search = Regex(condValue.toString())
                            //検索ワードと値が部分一致した場合、flgをtrueにする。
                            if(search.containsMatchIn(resValue.toString())){
                                hitFlg = true
                            }
                        }
                    }
                    if(!hitFlg){
                        //検索結果、一致しなかったから、このレコードを除外たレコードを返す
                        chkList.add(idx)
                    }
                }
                removeIdList.add(chkList)
            }

            if(fld.containsMatchIn(condKey) && condValue != ""){

                //keyがfld○○の場合の関数を作る => データ管理画面から検索をした時の処理
                Log.d("データ管理の検索","取得完了。項目名${condKey}:検索ワード${condValue}")
                val dataRemoveIdList = itemSearch(condKey,condValue,result)
                removeIdList.add(dataRemoveIdList)
            }

            if(!fld.containsMatchIn(condKey) && condKey !="freeWord" && condValue != ""){
                //keyがfld○○でない場合の関数を作る => 機器管理画面から検索した時の処理
                val fixtureRemoveIdList = fixSearch(condKey,condValue,result)
                removeIdList.add(fixtureRemoveIdList)
            }

        }

        /**
         * リストが1~10の時
         * 削除リスト1[3,4,5]
         * 削除リスト2[3,4,5,6,7,8]
         * 削除リスト3[4,5,6,7,10]
         * とする。表示するレコードは1,2のみ
         * 全ての配列で一つも出てこないということは全条件に合致する
         *
         */
        //降順に並び替え。これしないとエラー。
        val countRemoveIdList : MutableMap<Int,Int> = mutableMapOf()
        val size = result.size -1
        for(i in size downTo  0  ){
            countRemoveIdList.set(i,0)
        }

        removeIdList.forEach{
            //削除処理
            it.forEach{
                var value = countRemoveIdList[it]!!.toInt()
                value += 1
                countRemoveIdList.set(it,value)
            }
        }

        countRemoveIdList.forEach{
            Log.d("答えの配列","${it}")
            if(it.value > 0){
                result.removeAt(it.key)
            }
        }



        return result
    }



    /**
     * データ管理の検索を行う関数
     * @param condKey 検索対象の項目
     * @param condValue 検索ワード
     * @param search 検索する対象
     * @return result 検索結果
     *
     * やりたいこととしては、fld○○の検索を行う。
     * 検索条件と不一致IDを返す。
     * 検索条件が空白の時は上の検索条件ではじく[if value != ""]で実行
     * */
    public fun itemSearch(condKey:String,condValue:String?, search:MutableList<MutableMap<String, String?>>): MutableList<Int> {
        val result: MutableList<Int> = mutableListOf()
        val value =  Regex(condValue.toString())

        for (idx in 0 until search.size){
            if(!value.containsMatchIn(search[idx][condKey].toString())){
                result.add(idx)
                Log.d("検索結果","君削除ね")
            }
        }

        return result
    }




    /**
     * 機器管理の検索を行う関数
     * @param cond 検索条件のmap(key:項目名 value:検索ワード)
     * @param search 検索する対象
     * @return result 検索結果
     *
     * やりたいこととしては、機器管理の検索を行う。
     * 検索条件と不一致IDを返す。
     * ○○日～○〇日までという処理も行うため、別で検索する。
     */
    public fun fixSearch(condKey:String,condValue:String?, search:MutableList<MutableMap<String, String?>>): MutableList<Int> {

        var result: MutableList<Int> = mutableListOf()
        val value =  Regex(condValue.toString())
        val key = Regex(condKey.toString())
        val dayMin = Regex("DayMin")
        val dayMax = Regex("DayMax")
        search.forEach{
            Log.d("機器管理の検索","${it}")
        }

        if(condKey == "status") {
            //ステータス検索
            when (condValue) {
                //ステータス:すべて
                // 特に処理を行わない
                FixtureType.statusTakeOut -> {
                    //ステータス:持ち出し中
                    result = statusSearch(search,"takeOut")
                }

                FixtureType.statusCanTakeOut -> {
                    //ステータス：持ち出し可
                    result = statusSearch(search,"canTakeOut")
                }

                FixtureType.statusNotTakeOut -> {
                    //ステータス：持ち出し不可
                    result = statusSearch(search,"notTakeOut")
                }

                FixtureType.statusFinishInstall -> {
                    //ステータス：設置済の時
                    result = statusSearch(search,"finishInstall")
                }
            }
        }else if(dayMin.containsMatchIn(condKey)){
            //日付検索
            when(condKey){
                "kenpinDayMin"->{
                    //検品日の最小を求める
                    for (idx in 0 until search.size) {
                        val chk = chkDateMin(condValue,search[idx]["fix_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }
                }
                "takeOutDayMin"->{
                    for (idx in 0 until search.size) {
                        val chk = chkDateMin(condValue,search[idx]["takeout_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }

                }
                "returnDayMin"->{
                    for (idx in 0 until search.size) {
                        val chk = chkDateMin(condValue,search[idx]["rtn_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }
                }
                "itemDayMin"->{
                    for (idx in 0 until search.size) {
                        val chk = chkDateMin(condValue,search[idx]["item_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }

                }
            }


        }else if(dayMax.containsMatchIn(condKey)){
            //日付検索
            when(condKey){
                "kenpinDayMax"->{
                    //検品日のMaxを求める
                    for (idx in 0 until search.size) {
                        val chk = chkDateMax(condValue,search[idx]["fix_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }
                }
                "takeOutDayMax"->{
                    for (idx in 0 until search.size) {
                        val chk = chkDateMax(condValue,search[idx]["takeout_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }

                }
                "returnDayMax"->{
                    for (idx in 0 until search.size) {
                        val chk = chkDateMax(condValue,search[idx]["rtn_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }
                }
                "itemDayMax"->{
                    for (idx in 0 until search.size) {
                        val chk = chkDateMax(condValue,search[idx]["item_date"])
                        if(chk == true){
                            result.add(idx)
                        }
                    }

                }
            }


        }else {
            //ID検索など。
            /**
             * (検索側) = (ベース側)
             * serialNumber = serial_number
             * dataId = project_id
             * kenpinOrg = FixOrg
             * kenpinUser = FixUser
             * takeOutOrg = TakeOutOrg
             * takeOutUser = TakeOutUser
             * returnOrg = RtnOrg
             * returnUser = RtnUser
             * itemOrg = ItemOrg
             * itemUser = ItemUser
             */
           //when(condKey){

            //}


        }

        return result
    }

    public fun statusSearch(search:MutableList<MutableMap<String, String?>>,type:String): MutableList<Int> {
        val result: MutableList<Int> = mutableListOf()
        when(type) {
            "takeOut"-> {
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.takeOut) {
                        result.add(idx)
                    }
                }
            }
            "canTakeOut"-> {
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.canTakeOut) {
                        result.add(idx)
                    }
                }
            }
            "notTakeOut"-> {
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.notTakeOut) {
                        result.add(idx)
                    }
                }
            }
            "finishInstall"-> {
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.finishInstall) {
                        result.add(idx)
                    }
                }
            }

        }
        return result
    }


    public fun searchWordCreate(string:String?): LocalDate? {
        if(string != null) {
            when (string) {
                "null"->{
                    //値がnullの時
                    return null
                }
                ""->{
                    //値が空白の時
                    return null
                }
                else->{
                    var time = string.replace("/", "-")
                    val result = LocalDate.parse(time)
                    return result
                }
            }
        }else{
            return null
        }
    }

    public fun baseWordCreate(string:String?): LocalDate? {
        if(string != null) {
            when(string){
                "null"->{
                    //値が入力されていないとき
                    return null
                }
                ""->{
                    //値が空白の時
                    return null
                }
                else->{
                    //値が入力されてるとき
                    var number = string.indexOf(" ")
                    var result = LocalDate.parse(string.substring(0, number))
                    return result
                }
            }
        }else{
            //値がnullの時
            return null
        }
    }

    //日付検索(最小値)
    public fun chkDateMin(condValue: String?,baseValue:String?): Boolean {
        val searchValue = searchWordCreate(condValue)
        val baseValue = baseWordCreate(baseValue)
        if(searchValue !=null && baseValue!=null) {
            val resultValue = ChronoUnit.DAYS.between(searchValue, baseValue)
            //これでtrueかfalseを返却できるみたい
            return resultValue.toInt() < 0
        }else{
            //日付が空白またはnullの場合は条件に合わないので、removeIdに追加するflgをtrueにする
            return true
        }
    }


    //日付検索（最大値）
    public fun chkDateMax(condValue: String?,baseValue:String?): Boolean {
        val searchValue = searchWordCreate(condValue)
        val baseValue = baseWordCreate(baseValue)
        if(searchValue != null && baseValue != null) {
            val resultValue = ChronoUnit.DAYS.between(searchValue, baseValue)
            //これでtrueかfalseを返却できるみたい
            return resultValue.toInt() > 0
        }else{
            //日付が空白またはnullの場合は条件に合わないので、removeIdに追加するflgをtrueにする
            return true
        }
    }



