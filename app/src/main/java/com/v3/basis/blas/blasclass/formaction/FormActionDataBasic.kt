package com.v3.basis.blas.blasclass.formaction

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.blasclass.config.FieldType
import java.security.AccessController.getContext
import java.util.*

open class FormActionDataBasic(setToken:String,setActivity: FragmentActivity) {
    public var token :String = setToken
    get() = field
    set(token:String){
        field = token
    }

    public var baseActivity : FragmentActivity = setActivity
    get() = field
    set(baseActivity:FragmentActivity){
        field = baseActivity
    }

    var baseContext : Context? = null
    get() = field
    set(baseContext){
        field = baseContext
    }
    var payload:MutableMap<String,String?> = mutableMapOf()

    data class formType(var type: String?,
                        var title: String?,
                        var choiceValue: List<String?>?,
                        var require:String?,
                        var unique:String?,
                        var fieldId:String?,
                        var parentFieldId:String?)


    /**
     * 取得したアイテムフィールドから項目のタイプ・名前・選択肢を返す
     * [引数]
     * list => 配列。項目の情報が入っている
     *
     * [返り値]
     * rtnType => 項目のタイプを返す。
     *            それ以外 => 項目名を返す。
     *            listがnull => nullを返す。
     * rtnTitle => 項目の名称を返す。
     *             それ以外 => 項目名を返す
     *             listがnull => nullを返す。
     * choiceValue => 項目の選択肢を返す。
     *                typeがSINGLE_SELECTION(単一選択) => 値を返す
     *                typeがMULTIPLE_SELECTION(複数選択) => 値を返す
     *                それ以外 => nullを返す
     */
    open fun typeCheck(list:MutableMap<String, String?>?): formType {
        val choiceValue:List<String>? = null
        val rtnType:String? =null
        val rtnTitle :String?= null
        val nullable:String? = null
        val unique:String? = null
        val fieldId:String? = null
        val parentFieldId :String? = null
        val formInfo =  formType(rtnType,rtnTitle, choiceValue, nullable,unique,fieldId,parentFieldId)

        if(list!=null){
            Log.d("デバック用ログ","配列中身=>${list}")
            formInfo.title = list.get("name")
            formInfo.require = list.get("essential").toString()
            formInfo.unique = list.get("unique_chk").toString()
            formInfo.fieldId = list.get("field_id").toString()
            formInfo.parentFieldId = list.get("parent_field_id").toString()

            val chkType = list.get("type")
            when(chkType){
                FieldType.TEXT_FIELD->{
                    //自由入力(1行)
                    formInfo.type =  FieldType.TEXT_FIELD
                }
                FieldType.TEXT_AREA->{
                    //自由入力(複数行)
                    formInfo.type = FieldType.TEXT_AREA
                }
                FieldType.DATE_TIME->{
                    //日付入力
                    formInfo.type = FieldType.DATE_TIME
                }
                FieldType.TIME->{
                    //時間入力
                    formInfo.type = FieldType.TIME
                }
                FieldType.SINGLE_SELECTION->{
                    //単一選択
                    formInfo.type = FieldType.SINGLE_SELECTION
                    val singleCheckValue = list["choice"]
                    if(singleCheckValue != null){
                        formInfo.choiceValue =singleCheckValue.split(",")
                    }
                }
                FieldType.MULTIPLE_SELECTION->{
                    //チェックボックス選択
                    formInfo.type = FieldType.MULTIPLE_SELECTION
                    val multipCheckValue = list["choice"]
                    if(multipCheckValue != null){
                        formInfo.choiceValue = multipCheckValue.split(",")
                    }
                }
                FieldType.KENPIN_RENDOU_QR->{
                    //チェックボックス選択
                    formInfo.type = FieldType.KENPIN_RENDOU_QR
                }
                FieldType.QR_CODE->{
                    formInfo.type = FieldType.QR_CODE

                }
                FieldType.TEKKILYO_RENDOU_QR->{
                    formInfo.type = FieldType.TEKKILYO_RENDOU_QR

                }
                FieldType.CHECK_VALUE->{
                    formInfo.type = FieldType.CHECK_VALUE

                }
                FieldType.ACOUNT_NAME->{
                    formInfo.type = FieldType.ACOUNT_NAME
                }
                FieldType.SIG_FOX->{
                    formInfo.type = FieldType.SIG_FOX
                }
            }
        }
        return formInfo
    }

    /**
     * 取得した項目名をフォームの項目のタイトルにセットする。
     * [引数]
     * title => string。取得したタイトルを指定
     * params => LinearLayout.params。表示するviewの設定を指定
     * act => FragmentActivity。この操作を行うactivity指定
     *
     * [返り値]
     * view => paramsの設定を反映したにtextViewを作成。titleをタイトルに反映して返す。
     *         titleがnullの時 => タイトルnullのまま作成
     *         それ以外 => titleの値をタイトルに反映する
     */
    open fun createFormSectionTitle(params: LinearLayout.LayoutParams?, formInfo:FormActionDataBasic.formType): TextView {
        val view = TextView(baseActivity)
        var formTitle = if(formInfo.title != null){"${formInfo.title}"}else{" "}
        view.setText("${formTitle}")
        //文字の色変更したい。
        view.setTextColor(Color.BLACK)
        view.setLayoutParams(params)
        return view
    }

    /**
     * 配列から必要な情報を抜き出す
     * type=>一行入力や複数選択など
     * require=>必須入力か否
     * unique => 重複不可制約
     */
    fun createFormInfoMap(formInfo:formType): MutableMap<String, String?> {
        val typeMap :MutableMap<String,String?> = mutableMapOf()
        typeMap.set(key = "type",value = "${formInfo.type}")
        typeMap.set(key = "require",value = "${formInfo.require}")
        typeMap.set(key = "unique",value = "${formInfo.unique}")
        return typeMap
    }


    /**
     * テキストフィールドを作成する関数。
     * [引数]
     * params => LinearLayoutに設定したパラメータ
     * act => 操作をするFragmentActivity
     * cnt => 作成しているフォームは何行目かを示す
     *
     * []
     */
    open fun createTextField(params:LinearLayout.LayoutParams?, cnt:Int,formInfo: formType): EditText {
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.inputType =1
        edit.setLayoutParams(params)
        edit.id = cnt
        return edit
    }


    /**
     * テキスエリアを作成する関数。
     * [引数]
     * params => LinearLayoutに設定したパラメータ
     * act => 操作をするFragmentActivity
     * cnt => 作成しているフォームは何行目かを示す
     *
     * []
     */
    open fun createTextAlea(params:LinearLayout.LayoutParams?, cnt:Int,formInfo: formType): EditText {
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.height = 350
        edit.setLayoutParams(params)
        edit.id = cnt
        return edit
    }


    /**
     * 日付・時間フィールドを作成する関数。
     * [引数]
     * params => LinearLayoutに設定したパラメータ
     * act => 操作をするFragmentActivity
     * cnt => 作成しているフォームは何行目かを示す
     *
     * []
     */
    open fun createDateTime(params:LinearLayout.LayoutParams?, cnt:Int,formInfo: formType): EditText {
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.inputType = 1
        edit.setLayoutParams(params)
        edit.id = cnt
        //これがミソ！！！これなしだとタップ2回での起動になる
        edit.isFocusableInTouchMode = false

        return edit
    }

    /**
     * 日付・時間フィールドを作成する関数。
     * [引数]
     * params => LinearLayoutに設定したパラメータ
     * act => 操作をするFragmentActivity
     * cnt => 作成しているフォームは何行目かを示す
     *
     * []
     */
    open fun createRadioGrop(params:LinearLayout.LayoutParams?, cnt:Int): RadioGroup {
        val r_group = RadioGroup(baseActivity)
        r_group.setLayoutParams(params)
        r_group.id = cnt
        return r_group
    }

    /**
     * 日付・時間フィールドを作成する関数。
     * [引数]
     * params => LinearLayoutに設定したパラメータ
     * act => 操作をするFragmentActivity
     * cnt => 作成しているフォームは何行目かを示す
     *
     * []
     */
    open fun createSingleSelection(params:LinearLayout.LayoutParams?,value:String?,radiocnt : Int): RadioButton {
        val r_button = RadioButton(baseActivity)
        r_button.setText(value)
        r_button.id = radiocnt
        return r_button

    }

    open fun createMutipleSelection(params:LinearLayout.LayoutParams?,it:String?,cnt: Int): CheckBox {
        val checkbox = CheckBox(baseActivity)
        checkbox.setText(it)
        checkbox.setLayoutParams(params)
        checkbox.id = cnt
        checkbox.isChecked = false
        return checkbox
    }


    open fun pickUpValue(editMap:MutableMap<String,EditText?>,cnt:Int): String {
        val editText = editMap.get("col_${cnt}")
        var value = ""
        if(editText != null){
            value = "${editText.text}"
        }
        return value
    }

    open fun getCheckedRadioId(radioGroupMap:MutableMap<String,RadioGroup?>,cnt: Int): String {
        val radioGroup = radioGroupMap.get("col_${cnt}")
        var checkedRadioId = ""
        if (radioGroup != null) {
            checkedRadioId = radioGroup.checkedRadioButtonId.toString()
            Log.d("radio", "${checkedRadioId}")
        }
        return checkedRadioId
    }

    open fun getCheckedValue(radioValue:MutableMap<String,RadioButton?>,checkedRadioId:String): String {
        val checkValue = radioValue.get(checkedRadioId)
        var value = ""
        if(checkValue != null) {
            Log.d("radio", "${checkValue.text}")
            value = "${checkValue.text}"
        }
        return value
    }

    open fun getCheckedValues(colCheckMap:MutableMap<String?,CheckBox?>?): String {
        var value = ""
        var v_cnt = 1
        colCheckMap?.forEach {
            val msg = it.value
            if(msg != null) {
                if (msg.isChecked) {
                    Log.d("check", "${msg.text}")
                    if (v_cnt == 1) {
                        value = "${msg.text}"
                        v_cnt += 1
                    } else {
                        value += ",${msg.text}"
                    }
                }
            }
        }
        return value
    }

    open fun chkNull(it:String?,value: String?): MutableMap<String, String> {
        var nullChkMap:MutableMap<String,String> = mutableMapOf()
        when(it) {
            FieldType.TURE -> {
                //必須入力設定の時
                if(value == ""){
                    //値が入力されていない場合の処理
                    nullChkMap.set("nullChk","error")
                }else{
                    //値が入力されている処理
                    nullChkMap.set("nullChk","success")
                }

            }
            FieldType.FALSE -> {
                //自由入力設定の時
                nullChkMap.set("nullChk","success")
            }
        }
        return nullChkMap
    }

    open fun countNullError(nullChk:MutableMap<Int,MutableMap<String,String>>,textViewMap :MutableMap<String,TextView>): Int {
        var errorCnt = 0
        for(i in 1 until nullChk.size+1){
            val value =  nullChk[i]
            val values = textViewMap["${i}"]
            if(value != null && values!=null) {
                if (value["nullChk"] == "error") {
                    values.setTextColor(Color.RED)
                    val title = values.text
                    //複数回、入力必須項目ですを出力しない
                    if (!title.contains(FieldType.REQUIRED)) {
                        values.text = "${title}${FieldType.REQUIRED}"
                    }
                    errorCnt += 1
                }
            }
        }
        return errorCnt
    }

    open fun createSpace(layoutParamsSpace:LinearLayout.LayoutParams): Space {
        val space = Space(baseActivity)
        space.setLayoutParams(layoutParamsSpace)
        return space
    }

    fun getSelectValue(list:List<String?>?): MutableList<String> {
        val valueList : MutableList<String> = mutableListOf()
        if(list != null) {
            val listSize = list.size
            for (i in 0 until listSize){
                var selectedValue = list[i]
                if(selectedValue != null) {
                    when (i) {
                        0 -> {
                            selectedValue = selectedValue.drop(2)
                            val delNum = selectedValue.indexOf("\":\"")
                            selectedValue = selectedValue.removeRange(0,delNum+3)
                        }
                        listSize-1 -> {
                            selectedValue = selectedValue.dropLast(2)
                        }
                    }
                    valueList.add(i,selectedValue)
                }
            }
        }
        return valueList
    }

    fun getParentSelect(list:List<String?>?): String? {
        Log.d("デバック用ログ","値の取得=>${list}")
        var master :String?= null
        if(list != null) {
            val parentValue = list[0]
            if(parentValue != null){
                master = parentValue.drop(2)
                val delNum = master.indexOf("\":\"")
                master = master.removeRange(delNum,master.length)
            }
        }
        return master

    }

    fun getColNum(num:String): String {
        var protoNum = num
        val delNum = num.indexOf("_")
        protoNum = num.dropLast(delNum+1)
        return protoNum
    }

    fun  setDefaultTitle(textViewMap :MutableMap<String,TextView>){
        textViewMap.forEach{
            if(it.value.text.toString().contains((FieldType.REQUIRED)) ){
                val newValue = it.value.text.toString().dropLast( FieldType.REQUIRED.length)
                it.value.setText(newValue)
                it.value.setTextColor(Color.DKGRAY)
                it.value.setError("入力必須の項目です")
            }
        }

    }
}