package com.v3.basis.blas.ui.item.item_create


import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.app.BlasDef
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.formaction.FormActionDataCreate
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.blasclass.rest.BlasRestUser
import com.v3.basis.blas.ui.ext.addTitle
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import android.widget.EditText
import android.widget.TextView
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.NavUtils
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.blasclass.app.BlasMsg


/**
 * A simple [Fragment] subclass.
 */
class ItemCreateFragment : Fragment() {
    private var receiveData : Boolean = true
    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var rootView: LinearLayout
    private var parentChk :Boolean = true
    private val toastErrorLen = Toast.LENGTH_LONG

    private var formInfoMap: MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap: MutableMap<String, EditText?> = mutableMapOf()
    private var radioGroupMap: MutableMap<String, RadioGroup?> = mutableMapOf()
    private var radioValue: MutableMap<String, RadioButton?> = mutableMapOf()
    private var checkMap: MutableMap<String, MutableMap<String?, CheckBox?>> = mutableMapOf()
    private var textViewMap: MutableMap<String, TextView> = mutableMapOf()
    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50)
    private var memoMap:MutableMap<String,EditText> = mutableMapOf()
    private var userMap :MutableMap<String,String?> = mutableMapOf()
    private val nullChk: MutableMap<Int, MutableMap<String, String>> = mutableMapOf()

    private val idMap:MutableMap<String,String?> = mutableMapOf()
    private val parentMap:MutableMap<String,MutableMap<String,String>> = mutableMapOf()
    private val selectValueMap:MutableMap<String,MutableList<String>> = mutableMapOf()
    private val valueIdColMap : MutableMap<String,String> = mutableMapOf()

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)

    private lateinit var formAction: FormActionDataCreate
    private lateinit var qrCodeView: EditText
    private var handler = Handler()
    private var msg = BlasMsg()
    private val helper:RestHelper = RestHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        //初期値の取得
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }

        try {
            if (token != null && activity != null&& projectId != null) {
                formAction = FormActionDataCreate(token, activity!!)
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            receiveData = false
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
        //この時もう処理しないこと！！
        return inflater.inflate(R.layout.fragment_item_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //コンテンツを配置するLinearLayoutを取得
        rootView = view.findViewById<LinearLayout>(R.id.item_create_liner)
        //フォームセクションごとにスペース入れる処理。試しに入れてみた。
        val space = Space(activity)
        space.setLayoutParams(layoutParamsSpace)
        rootView.addView(space)
        //レイアウトの設置位置の設定

        if(receiveData) {
            val payload = mapOf("token" to token, "project_id" to projectId)
            val payload2 = mapOf("token" to token, "my_self" to "1")
            BlasRestField(payload, ::getSuccess, ::getFail).execute()
            BlasRestUser(payload2, ::userGetSuccess, ::userGetFail).execute()
        }
    }

    private fun getSuccess(result: JSONObject?) {
        //カラム順に並べ替える
        var cnt = 1
        var radioCount = 1
        var checkCount = 1
        if (result != null) {
            //colによる並び替えが発生しているため、ソートを行う
            val sortFormFieldList = helper.createFormField(result)
            val formFields = sortFormFieldList.values.sortedBy { it["field_col"]!!.toInt() }
            formFields.forEach {
                /**
                 * formInfoには以下の情報が入っている。
                 * ・title => 項目のタイトル
                 * ・type => 項目のタイプ(日付入力など)
                 * ・choiceValue => 項目が持つ選択肢
                 * ・require => 項目がnullが許容するかの定義
                 * ・unique => 項目が重複を許可するかの定義
                 */
                val formInfo = formAction.typeCheck(it)
                idMap.set(cnt.toString(),formInfo.fieldId.toString() )
                //先に項目のタイトルをセットする
                val formSectionTitle = formAction.createFormSectionTitle(layoutParams, formInfo)
                //formSectionTitle.setError("入力必須です")
                textViewMap.set(cnt.toString(), formSectionTitle)
                rootView.addView(formSectionTitle)

                //フォームの項目の情報をメンバ変数に格納
                val typeMap = formAction.createFormInfoMap(formInfo)

                formInfoMap.set(key = "${cnt}", value = typeMap)

                when (formInfo.type) {
                    FieldType.TEXT_FIELD -> {
                        //自由入力(複数行)
                        //editTextを作成
                        val formPart = formAction.createTextField(layoutParams, cnt, formInfo)
                        rootView.addView(formPart)
                        //配列にeditTextの情報を格納。
                        editMap.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.TEXT_AREA -> {
                        //自由入力(複数行)
                        //editTextを作成
                        val formPart = formAction.createTextAlea(layoutParams, cnt, formInfo)
                        rootView.addView(formPart)
                        //配列にeditTextの情報を格納。
                        editMap.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.DATE_TIME -> {
                        //日付入力
                        //editTextを作成
                        var formPart = formAction.createDateTime(layoutParams, cnt, formInfo)
                        formPart = setClickDateTime(formPart)
                        rootView.addView(formPart)


                        //配列にeditTextを格納
                        editMap.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.TIME -> {
                        //時間入力
                        //editText作成
                        var formPart = formAction.createDateTime(layoutParams, cnt, formInfo)
                        formPart = setClickTime(formPart)
                        rootView.addView(formPart)

                        //配列にeditTextを格納
                        editMap.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.SINGLE_SELECTION -> {
                        //ラジオボタンの時
                        val formGroup = formAction.createRadioGrop(layoutParams, cnt)
                        if(formInfo.parentFieldId == "0") {
                            val chkValueMap = formInfo.choiceValue
                            val colTargetPart:MutableList<String> = mutableListOf()
                            if (chkValueMap != null) {
                                chkValueMap.forEach {
                                    val formPart =
                                        formAction.createSingleSelection(
                                            layoutParams,
                                            it,
                                            radioCount
                                        )
                                    formPart.setOnClickListener{
                                        Log.d("デバック用のログ","${formPart.text}")

                                        var colNum:String? = null
                                        valueIdColMap.forEach{
                                            val protoNum = it.key
                                            colNum = formAction.getColNum(protoNum)
                                            val colId = idMap[colNum.toString()]
                                            var flg = false
                                            parentMap.forEach{
                                                //親IDが同じかつkeyWordが一致した時の処理
                                                Log.d("値","${it}")
                                                if(it.value["parentId"] == colId && it.value["keyWord"] == formPart.text) {
                                                    val list = selectValueMap[it.key]!!
                                                    list.forEach {
                                                        //ラジオボタンを編集可能にする
                                                        radioValue[it]!!.isEnabled = true
                                                        Log.d("デバック用ログ", "${it}")
                                                    }
                                                    flg = true
                                                }
                                            }
                                            if(!flg){
                                                //親IDまたはkeyWordが不一致の処理
                                                parentMap.forEach{
                                                    if(it.value["parentId"] == colId.toString()){
                                                        val list = selectValueMap[it.key]!!
                                                        list.forEach {
                                                            //チェックをはずす。編集不可状態にする
                                                            radioValue[it]!!.isChecked = false
                                                            radioValue[it]!!.isEnabled = false
                                                            Log.d("デバック用ログ", "${it}")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    radioValue.set(key = "${radioCount}", value = formPart)
                                    valueIdColMap.set(key = "${cnt}_${radioCount}",value = "${formPart.text}")
                                    colTargetPart.add(radioCount.toString())
                                    radioCount += 1
                                    formGroup.addView(formPart)
                                }
                                selectValueMap.set(cnt.toString(),colTargetPart)
                                rootView.addView(formGroup)
                                radioGroupMap.set(key = "col_${cnt}", value = formGroup)
                            }
                        }else{
                            val information :MutableMap<String,String> = mutableMapOf()
                            val chkValueMap = formAction.getSelectValue(formInfo.choiceValue)
                            val parentSelect = formAction.getParentSelect(formInfo.choiceValue)
                            val colTargetPart:MutableList<String> = mutableListOf()
                            information.set(key = "keyWord" ,value = parentSelect.toString())
                            information.set(key = "parentId",value = formInfo.parentFieldId.toString())
                            parentMap.set(cnt.toString(),information)
                            chkValueMap.forEach {
                                val formPart =
                                    formAction.createSingleSelection(
                                        layoutParams,
                                        it,
                                        radioCount
                                    )
                                radioValue.set(key = "${radioCount}", value = formPart)
                                colTargetPart.add(radioCount.toString())
                                radioCount += 1
                                formGroup.addView(formPart)
                                formPart.isEnabled = false
                            }
                            selectValueMap.set(cnt.toString(),colTargetPart)
                            rootView.addView(formGroup)
                            radioGroupMap.set(key = "col_${cnt}", value = formGroup)
                        }
                    }

                    FieldType.MULTIPLE_SELECTION -> {
                        //チェックボックスの時
                        val colCheckMap: MutableMap<String?, CheckBox?> = mutableMapOf()
                        val choicevalues = formInfo.choiceValue
                        if (choicevalues != null)
                            choicevalues.forEach {
                                val formPart =
                                    formAction.createMutipleSelection(layoutParams, it, checkCount)
                                rootView.addView(formPart)
                                colCheckMap.set(key = "col_${cnt}_${checkCount}", value = formPart)
                                checkCount += 1

                            }
                        checkMap.set(key = "col_${cnt}", value = colCheckMap)
                    }
                    FieldType.QR_CODE,
                    FieldType.KENPIN_RENDOU_QR,
                    FieldType.TEKKILYO_RENDOU_QR->{
                        //QRコードの処理
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_qr_item, null)
                        rootView.addView(layout)
                        val ed = layout.findViewById<EditText>(R.id.editText)
                        layout.findViewById<Button>(R.id.button)?.setOnClickListener{

                            qrCodeView = layout.findViewById(R.id.editText)
                            val intent = Intent(activity, QRActivity::class.java)
                            intent.putExtra("colNumber","${cnt}")
                            startActivityForResult(intent, QRActivity.QR_CODE)
                        }
                        //配列に値を格納//
                        editMap.set(key = "col_${cnt}", value = ed)
                    }

                    FieldType.CHECK_VALUE->{
                        //入力チェック
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_checkvalue, null)
                        rootView.addView(layout)
                        val value = layout.findViewById<EditText>(R.id.editValue)
                        val memo = layout.findViewById<EditText>(R.id.editMemo)

                        editMap.set(key = "col_${cnt}", value = value)
                        memoMap.set(key = "col_${cnt}",value = memo)

                        val information :MutableMap<String,String> = mutableMapOf()
                        information.set(key = "parentId",value = formInfo.parentFieldId.toString())
                        parentMap.set(cnt.toString(),information)

                    }

                    FieldType.ACOUNT_NAME->{
                        //アカウント名。
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_account, null)
                        rootView.addView(layout)
                        val editAccount = layout.findViewById<EditText>(R.id.editAccount)
                        val btn = layout.findViewById<Button>(R.id.buttonAccount)
                        btn.setOnClickListener{
                            editAccount.setText(userMap["name"])
                        }
                        editMap.set(key = "col_${cnt}", value = editAccount)
                    }

                    FieldType.SIG_FOX->{
                        //シグフォックス。
                        val view = TextView(activity)
                        view.setText("シグフォックスは使用できません")
                        //文字の色変更したい。
                        view.setTextColor(Color.BLACK)
                        view.setLayoutParams(layoutParams)
                        rootView.addView(view)

                    }

                }

                //フォームセクションごとにスペース入れる処理。試しに入れてみた。
                val space = Space(activity)
                space.setLayoutParams(layoutParamsSpace)
                rootView.addView(space)
                cnt += 1
            }

            //ボタンの作成処理
            val button = Button(activity)
            button.text = BlasDef.BTN_SAVE
            button.setLayoutParams(layoutParams)
            rootView.addView(button)

            //ボタン押下時の処理
            button.setOnClickListener {
                formAction.setDefaultTitle(textViewMap,formInfoMap)
                nullChk.clear()

                parentChk = true
                val parentErrorMap:MutableMap<String,MutableMap<String,String?>> = mutableMapOf()

                val payload: MutableMap<String, String?> =
                    mutableMapOf("token" to token, "project_id" to projectId)
                var cnt = 1
                var errorCnt = 0
                formInfoMap.forEach {
                    var cnt = 1

                    formInfoMap.forEach {
                        var value = ""
                        when (it.value["type"]) {
                            FieldType.TEXT_FIELD,
                            FieldType.TEXT_AREA,
                            FieldType.DATE_TIME,
                            FieldType.TIME -> {
                                //自由入力(1行)・自由入力(複数行)・日付入力・時間入力
                                value = formAction.pickUpValue(editMap, cnt)
                                payload.set("fld${cnt}", value)
                            }

                            FieldType.SIG_FOX->{
                                payload.set("fld${cnt}", value)
                            }

                            FieldType.SINGLE_SELECTION -> {
                                //ラジオボタン
                                val checkedRadioId = formAction.getCheckedRadioId(radioGroupMap, cnt)
                                val radioGrp = radioValue.get(checkedRadioId)
                                if(radioGrp != null) {
                                    if (radioGrp.isEnabled) {
                                        value = formAction.getCheckedValue(radioValue, checkedRadioId)
                                    }
                                }
                                payload.set("fld${cnt}", "${value}")
                            }

                            FieldType.MULTIPLE_SELECTION -> {
                                //チェックボックス
                                val colCheckMap = checkMap.get("col_${cnt}")
                                value = formAction.getCheckedValues(colCheckMap)
                                payload.set("fld${cnt}", "${value}")

                            }
                            FieldType.KENPIN_RENDOU_QR,
                            FieldType.QR_CODE,
                            FieldType.TEKKILYO_RENDOU_QR -> {
                                //配列に格納した値を取得

                                val colCheckMap = editMap.get("col_${cnt}")
                                if(colCheckMap != null) {
                                    value = colCheckMap.text.toString()
                                    payload.set("fld${cnt}", "${value}")
                                }
                            }
                            FieldType.ACOUNT_NAME->{
                                val colCheckMap = editMap.get("col_${cnt}")
                                if(colCheckMap != null) {
                                    value = colCheckMap.text.toString()
                                    payload.set("fld${cnt}", "${value}")
                                }
                            }
                            FieldType.CHECK_VALUE-> {
                                value = formAction.pickUpValue(editMap, cnt)
                                val memoValue = memoMap["col_${cnt}"]?.text.toString()
                                val protoMap = parentMap[cnt.toString()]
                                val parentId = protoMap?.get("parentId")
                                idMap.forEach{
                                    if(it.value == parentId){
                                        var parentValue = ""
                                        val parentCol = it.key
                                        when(formInfoMap[parentCol]?.get("type")){
                                            FieldType.TEXT_FIELD,
                                            FieldType.TEXT_AREA,
                                            FieldType.DATE_TIME,
                                            FieldType.TIME -> {
                                                //自由入力(1行)・自由入力(複数行)・日付入力・時間入力
                                                parentValue = formAction.pickUpValue(editMap, parentCol.toInt())
                                            }
                                            FieldType.SINGLE_SELECTION -> {
                                                //ラジオボタン
                                                val checkedRadioId = formAction.getCheckedRadioId(radioGroupMap, parentCol.toInt())
                                                parentValue = formAction.getCheckedValue(radioValue, checkedRadioId)
                                                Log.d("値チェック(ラジオボタン)","parentValue = ${parentValue}")
                                            }
                                            FieldType.MULTIPLE_SELECTION -> {
                                                //チェックボックス
                                                val colCheckMap = checkMap.get("col_${parentCol}")
                                                parentValue = formAction.getCheckedValues(colCheckMap)
                                                Log.d("値チェック(チェックボックス)","parentValue = ${parentValue}")
                                            }
                                            FieldType.KENPIN_RENDOU_QR,
                                            FieldType.QR_CODE,
                                            FieldType.TEKKILYO_RENDOU_QR -> {
                                                //配列に格納した値を取得
                                                val colCheckMap = editMap.get("col_${parentCol}")
                                                if (colCheckMap != null) {
                                                    parentValue = colCheckMap.text.toString()
                                                }
                                            }
                                            FieldType.ACOUNT_NAME->{
                                                val colCheckMap = editMap.get("col_${parentCol}")
                                                if (colCheckMap != null) {
                                                    parentValue = colCheckMap.text.toString()
                                                }
                                            }
                                        }
                                        if(parentValue != value){
                                            val status :MutableMap<String,String?> = mutableMapOf()
                                            status.set(key = "parentId",value = parentId)
                                            status.set(key = "parentCol",value = parentCol)
                                            parentErrorMap.set(cnt.toString(),status)
                                            if(memoValue == ""){
                                                parentChk = false
                                                Toast.makeText(activity, getText(R.string.check_error), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    payload.set("fld${cnt}", "{\"value\": \"${value}\", \"memo\": \"${memoValue}\"}")
                                }
                            }
                        }

                        val nullChkMap: MutableMap<String, String> =
                            formAction.chkNull(it.value["require"], value)
                        nullChk.set(cnt, nullChkMap)
                        cnt += 1
                    }

                }
                errorCnt = formAction.countNullError(nullChk, textViewMap,formInfoMap)
                Log.d("デバックログの取得","nullChk => ${nullChk}")
                if (errorCnt == 0 && parentChk) {
                    BlasRestItem("create", payload, ::createSuccess, ::createError).execute()
                }
            }
        }
    }

    /**
     * フィールド取得失敗時
     */
    fun getFail(errorCode: Int ,aplCode :Int) {
    
        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

    /**
     * データの作成失敗時の処理
     */
    fun createError(errorCode: Int, aplCode:Int) {
        Log.d("sippai ", "失敗")
        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        handler.post {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * データの作成成功時
     */
    fun createSuccess(result: JSONObject) {
        Log.d("seikou ", "成功")
        Toast.makeText(activity, getText(R.string.success_data_create), Toast.LENGTH_SHORT)
            .show()
        (requireActivity() as ItemActivity).transitionItemListScreen()
    }

    /**
     * 日付フィールドタップ時の処理
     */
    fun setClickDateTime(formPart: EditText): EditText {
        //editTextタップ時の処理
        formPart.setOnClickListener {
            val dtp = DatePickerDialog(
                getContext()!!,
                DatePickerDialog.OnDateSetListener { view, y, m, d ->
                    //フォーマットを作成
                    formPart.setText(String.format("%d/%02d/%02d", y, m + 1, d))
                },
                year,
                month,
                day
            )
            dtp.show()
        }
        return formPart
    }

    /**
     * 時間フィールドタップ時の処理
     */
    private fun setClickTime(formPart: EditText): EditText {
        //editTextタップ時の処理
        formPart.setOnClickListener {
            val tp = TimePickerDialog(
                context,
                TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                    formPart.setText(String.format("%02d:%02d", hour, minute))
                }, hour, minute, true
            )
            tp.show()
        }
        return formPart
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && requestCode == QRActivity.QR_CODE) {

            val qr = data?.getStringExtra("qr_code")
            qrCodeView.setText(qr)
        }
    }

    private fun userGetSuccess(result: JSONObject){
        val test = helper.createUserList(result)
        test.forEach{
            val map = it.value
            map.forEach{
                userMap.set(key = it.key,value = it.value)
            }
        }
        Log.d("デバックログ","ユーザの中身=>${userMap}")
    }


    private fun userGetFail(errorCode: Int, aplCode:Int){

    }
}


