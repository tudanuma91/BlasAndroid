package com.v3.basis.blas.blasclass.app

import android.util.Log
import com.v3.basis.blas.blasclass.config.FixtureType
import com.v3.basis.blas.blasclass.rest.RestfulRtn
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.icu.text.SimpleDateFormat
import java.lang.Exception
import java.security.MessageDigest
import java.util.*


class BlasCom {

}
private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

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
     * @param dateTimeCol 日付・時間検索のカラム
     * @return result 検索結果
     */
    public fun searchAndroid(cond:MutableMap<String, String?>, search:MutableList<MutableMap<String, String?>>,dateTimeCol:String): MutableList<MutableMap<String, String?>> {
        //TODO:検索窓に記号が入力されたときの処理を実装する
        //java.util.regex.PatternSyntaxException: Incorrectly nested parentheses in regexp pattern near index 1
        //多分だけど、"("は判定できないのかな？ちょっとテストする必要あり
        //エラー記号リスト
        //[(,),?,\,[,{,},*,+,.,]
        //別件だけど$,^,|,:,でなぜか全件ヒットする...

        val dateTimeColList :MutableList<String> = mutableListOf()
        val result = search.toMutableList()
        val chkList :MutableList<Int> = mutableListOf()
        val removeIdList:MutableList<MutableList<Int>> = mutableListOf()
        if(dateTimeCol != "") {
            dateTimeColList.addAll(dateTimeCol.split(","))
        }


        for ((condKey, condValue) in cond) {
            //検索条件がフリーワードかつ文字が入力されているとき
            val fld = Regex("fld")
            if(condKey =="freeWord" && condValue!=""){
                for (idx in 0 until result.size) {
                    var hitFlg = false
                    // 予想だけどfor文回しているときに配列操作するとsizeが違うってエラーあり。なので対策した
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
                if(dateTimeColList.size != 0) {
                    //検索項目の中に日付・時間検索が含まれているとき
                    val target = condKey.drop(3)
                    var dateFlg = false
                    dateTimeCol.forEach {
                        if (target == it.toString()) {
                            dateFlg = true
                        }
                    }
                    if (dateFlg) {
                        val dataRemoveIdList = itemSearchDateTimeManager(condKey,condValue,result)
                        removeIdList.add(dataRemoveIdList)

                    } else {
                        val dataRemoveIdList = itemSearch(condKey, condValue, result)
                        removeIdList.add(dataRemoveIdList)
                    }

                }else{
                    val dataRemoveIdList = itemSearch(condKey, condValue, result)
                    removeIdList.add(dataRemoveIdList)
                }
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
         * とする。表示するレコードは1,2,9のみ
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
            //削除処理を実行するために、配列に入れる。
            it.forEach{
                var value = countRemoveIdList[it]!!.toInt()
                value += 1
                countRemoveIdList.set(it,value)
            }
        }

        countRemoveIdList.forEach{
            //削除処理
            if(it.value > 0){
                //削除条件に1件以上合致する時削除処理を実施
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
    fun itemSearchDateTimeManager(condKey:String,condValue:String?, search:MutableList<MutableMap<String, String?>>): MutableList<Int> {
        val result : MutableList<Int> = mutableListOf()
        val valueList = condValue.toString().split("_from_")
        val min = valueList[0]
        val max = valueList[1]
        val minLength = min.length
        val maxLength = max.length
        if(minLength == 5 || maxLength == 5){
            //Time
            var searchValueMin:Date? =  null
            var searchValueMax:Date? =  null
            if(min != "Null"){
                searchValueMin = searchWordCreateTime(min)
                result.addAll(itemSearchTime(searchValueMin,condKey,condValue,search,"Min"))
            }
            if(max != "Null"){
                searchValueMax = searchWordCreateTime(max)
                Log.d("デバック用のログ","${searchValueMax}")
                result.addAll(itemSearchTime(searchValueMax,condKey,condValue,search,"Max"))
            }
        }else{
            //Day
            var searchValueMin:LocalDate? =  null
            var searchValueMax:LocalDate? =  null
            if(min != "Null"){
                searchValueMin = searchWordCreate(min)
                result.addAll(itemSearchDate(searchValueMin,condKey,search,"Min"))
                //検索処理
                //result.addAll(検索処理の値)
            }
            if(max != "Null"){
                searchValueMax = searchWordCreate(max)
                result.addAll(itemSearchDate(searchValueMax,condKey,search,"Max"))
                //検索処理
                //result.addAll()
            }
        }
        return result
    }

    /**
    *  @param string 検索ワード
    *  @return result フォーマットを整えた検索ワード or null
    *
    * ※ここちょっと簡略化できるかもしれない
    *  この関数は検索ワードのフォーマットを整える関数です
    */
    public fun searchWordCreateTime(string:String): Date {
        val stringSplit = string.split(":")
        val result =timeFormat.parse("2000-01-01 ${stringSplit[0]}:${stringSplit[1]}:30")
        return result
    }


    /**
    *  @param string 検索ワード
    *  @return result フォーマットを整えた検索ワード or null
    *
    * ※ここちょっと簡略化できるかもしれない
    *  この関数は検索ワードのフォーマットを整える関数です
    */
    fun itemSearchTime(searchValue:Date,
                       condKey: String,
                       condValue: String?,
                       search: MutableList<MutableMap<String, String?>>,
                       type: String): MutableList<Int>{
        val result : MutableList<Int> = mutableListOf()
        when(type){
            "Min"->{
                for (idx in 0 until search.size) {
                    try {
                        val date = search[idx][condKey]
                        if(date != null){
                            val baseDate = searchWordCreateTime(date)
                            val resultValue = searchValue.after(baseDate)
                            if(resultValue){
                                result.add(idx)
                            }

                        }else{
                            result.add(idx)
                        }
                    }catch (e:Exception){
                        result.add(idx)
                    }
                }

            }
            "Max"->{
                for (idx in 0 until search.size) {
                    try {
                        val date = search[idx][condKey]
                        if(date != null){
                            val baseDate = searchWordCreateTime(date)
                            val resultValue = baseDate.after(searchValue)
                            if(resultValue){
                                result.add(idx)
                            }

                        }else{
                            result.add(idx)
                        }
                    }catch (e:Exception){
                        result.add(idx)
                    }
                }

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
    fun itemSearchDate(searchValue:LocalDate?,
                       condKey: String,
                       search: MutableList<MutableMap<String, String?>>,
                       type: String): MutableList<Int> {
        val result : MutableList<Int> = mutableListOf()
        when(type){
            "Min"->{
                for (idx in 0 until search.size) {
                    try {
                        //検索用の値を取得
                        val date = searchWordCreate(search[idx][condKey]).toString()
                        if (date != "" && date != "null") {
                            val baseValue = searchWordCreate(date)
                            val resultValue = ChronoUnit.DAYS.between(searchValue, baseValue)
                            if(resultValue<0){
                                result.add(idx)
                            }
                        } else {
                            result.add(idx)
                        }
                    } catch (e: Exception) {
                        result.add(idx)
                    }
                }

            }
            "Max" ->{
                for (idx in 0 until search.size) {
                    try {
                        //検索用の値を取得
                        val date = searchWordCreate(search[idx][condKey]).toString()
                        if (date != "" && date != "null") {
                            val baseValue = searchWordCreate(date)
                            val resultValue = ChronoUnit.DAYS.between(searchValue, baseValue)
                            if(resultValue>0){
                                result.add(idx)
                            }
                        } else {
                            result.add(idx)
                        }
                    } catch (e: Exception) {
                        result.add(idx)
                    }
                }
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

        val result: MutableList<Int> = mutableListOf()
        val dayMin = Regex("DayMin")
        val dayMax = Regex("DayMax")


        if(condKey == "status") {
            //ステータス検索
            result.addAll(statusSearch(search,condValue))

        }else if(dayMin.containsMatchIn(condKey)){
            //日付検索（最低値）
            result.addAll(dayMinSearch(search,condValue,condKey))

        }else if(dayMax.containsMatchIn(condKey)){
            //日付検索（最大値）
            result.addAll(dayMaxSearch(search,condValue,condKey))

        }else {
            //ID検索など。
            result.addAll(otherSearch(search,condValue,condKey))
        }

        return result
    }

    /**
     * @param search => 検索のベースになる配列。
     * @param condValue => 検索ワードのこと。
     * @return result 検索ワードと合致しないインデックスを格納して返却する
     *
     * この関数はステータス検索を行う関数です。
     *
     */
    public fun statusSearch(search:MutableList<MutableMap<String, String?>>,condValue: String?): MutableList<Int> {
        val result: MutableList<Int> = mutableListOf()
        //ステータス:すべての時は何も処理を行わず、配列の未返却する

        when(condValue){

            FixtureType.statusTakeOut -> {
                //ステータス:持ち出し中
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.takeOut) {
                        result.add(idx)
                    }
                }
            }

            FixtureType.statusCanTakeOut -> {
                //ステータス：持ち出し可
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.canTakeOut) {
                        result.add(idx)
                    }
                }
            }

            FixtureType.statusNotTakeOut -> {
                //ステータス：持ち出し不可
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.notTakeOut) {
                        result.add(idx)
                    }
                }
            }

            FixtureType.statusFinishInstall -> {
                //ステータス：設置済の時
                for (idx in 0 until search.size) {
                    if (search[idx]["status"] != FixtureType.finishInstall) {
                        result.add(idx)
                    }
                }
            }
        }
        return result
    }

    /**
     * @param search 検索されるベースの配列。
     * @param condKey 検索対象の項目
     * @param condValue 検索ワード
     * @return result 検索条件と不一致の配列を返却する
     *
     * この関数は日付検索の最低値を求める関数です。
     * ○○/××～　の正誤を求める
     *
     */
    public fun dayMinSearch(search:MutableList<MutableMap<String, String?>>,condValue: String?,condKey:String): MutableList<Int> {
        val result:MutableList<Int> = mutableListOf()

        when(condKey) {
            "kenpinDayMin" -> {
                //検品日の最小を求める
                for (idx in 0 until search.size) {
                    val chk = chkDateMin(condValue, search[idx]["fix_date"])
                    if (chk == true) {
                        result.add(idx)
                    }
                }
            }

            "takeOutDayMin" -> {
                //持ち出し日の最小を求める
                for (idx in 0 until search.size) {
                    val chk = chkDateMin(condValue, search[idx]["takeout_date"])
                    if (chk == true) {
                        result.add(idx)
                    }
                }

            }

            "returnDayMin" -> {
                //返却日の最小を求める
                for (idx in 0 until search.size) {
                    val chk = chkDateMin(condValue, search[idx]["rtn_date"])
                    if (chk == true) {
                        result.add(idx)
                    }
                }
            }

            "itemDayMin" -> {
                //設置日の最小を求める
                for (idx in 0 until search.size) {
                    val chk = chkDateMin(condValue, search[idx]["item_date"])
                    if (chk == true) {
                        result.add(idx)
                    }
                }

            }

        }
        return result
    }

    /**
     * @param search 検索される側の値の配列
     * @param condValue 検索ワード
     * @param condKey 検索対象の項目
     *
     */
    public fun dayMaxSearch(search:MutableList<MutableMap<String, String?>>,condValue: String?,condKey:String): MutableList<Int> {
        val result:MutableList<Int> = mutableListOf()
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
        return result
    }


    /**
     *  @param string 検索ワード
     *  @return result フォーマットを整えた検索ワード or null
     *
     * ※ここちょっと簡略化できるかもしれない
     *  この関数は検索ワードのフォーマットを整える関数です
     */
    public fun searchWordCreate(string:String?): LocalDate? {
        val time = string!!.replace("/", "-")
        val result = LocalDate.parse(time)
        return result
    }


    /**
     * @param string  検索されるベースの値
     * @return result or null 検索ベースの値のフォーマットを整えて返却するかnullで返す
     *
     * この関数は検索ベースのフォーマットを整える関数です
     *
     * */
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
                    val number = string.indexOf(" ")
                    val result = LocalDate.parse(string.substring(0, number))
                    return result
                }
            }
        }else{
            //値がnullの時
            return null
        }
    }


    /**
     * @param condValue 検索ワード
     * @param baseValue 検索される方のワード
     * @return Bool
     *
     * この関数は日付検索の結果をtrueかfalseで返します
     * Minなので値が+1以上だとfalseを返す。
     * ※trueが削除対象
     *
     */
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


    /**
     * @param condValue 検索ワード
     * @param baseValue 検索される方のワード
     * @return Bool
     *
     * この関数は日付検索の結果をtrueかfalseで返します
     * Maxなので値が-1以下だとfalseを返す。
     * ※trueが削除対象
     *
     */
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


    public fun otherSearch(search: MutableList<MutableMap<String, String?>>, condValue: String?, condKey: String): MutableList<Int> {
        val result:MutableList<Int> = mutableListOf()

        val value = Regex(condValue.toString())

        for (idx in 0 until search.size){
            if(!value.containsMatchIn(search[idx][condKey].toString())){
                result.add(idx)
                Log.d("検索結果","君削除ね")
            }
        }

        return result
    }

    fun encrypt(data:String, encKey:String) :String{

        val cip = Cipher.getInstance("AES")
        cip.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encKey.toByteArray(charset("UTF-8")), "AES"))
        val enc_byte = cip.doFinal(data.toByteArray(charset("UTF-8")))
        val enc_data64 = Base64.getEncoder().encodeToString(enc_byte);

        return enc_data64

    }

    fun decrypt(data:String, decKey:String) :String{

        val cip = Cipher.getInstance("AES")
        cip.init(Cipher.DECRYPT_MODE, SecretKeySpec(decKey.toByteArray(charset("UTF-8")), "AES"))
        val newData = Base64.getDecoder().decode(data.toByteArray(charset("UTF-8")))
        val dec_data = cip.doFinal(newData)
        return dec_data.toString(charset("UTF-8"))

    }

    /**
     * 引数からhash値を返す関数
     * @param source hash変換する文字列
     * @return hash値を返却する
     */
    fun getHash(source:String) : String{

        source.toByteArray()
        val hashStr = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString(separator = "") {
                "%02x".format(it)
            }

        return hashStr

    }
