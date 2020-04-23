package com.v3.basis.blas.ui.item.item_edit

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.QRActivity
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.BTN_SAVE
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.formaction.FormActionDataEdit
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.item.item_create.ItemCreateFragment
import org.json.JSONObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemEditFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemEditFragment : Fragment() {
    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var itemId: String
    private lateinit var rootView: LinearLayout
    private var parentChk :Boolean = true

    private var formInfoMap:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private val editMap:MutableMap<String, EditText?> = mutableMapOf()
    private val radioGroupMap:MutableMap<String, RadioGroup?> = mutableMapOf()
    private val radioValue :MutableMap<String, RadioButton?> = mutableMapOf()
    private val checkMap:MutableMap<String,MutableMap<String?, CheckBox?>> = mutableMapOf()
    private var formFieldList:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private val jsonItem:MutableMap<String,JSONObject> = mutableMapOf()
    private val formDefaultValueList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private val textViewMap:MutableMap<String,TextView> = mutableMapOf()
    private var memoMap:MutableMap<String,EditText> = mutableMapOf()

    private val idMap:MutableMap<String,String?> = mutableMapOf()
    private val parentMap:MutableMap<String,MutableMap<String,String>> = mutableMapOf()
    private val selectValueMap:MutableMap<String,MutableList<String>> = mutableMapOf()
    private val valueIdColMap : MutableMap<String,String> = mutableMapOf()

    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,50)

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)

    private lateinit var formAction: FormActionDataEdit
    private val helper:RestHelper = RestHelper()
    private lateinit var qrCodeView: EditText

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

        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
            Log.d("token_item", "${token}")
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }
        if (extras?.getString("item_id") != null) {
            itemId = extras.getString("item_id").toString()
        }

        if(token != null && activity != null){
            formAction = FormActionDataEdit(token,activity!!)
        }



        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //コンテンツを配置するLinearLayoutを取得
        rootView = view.findViewById<LinearLayout>(R.id.item_edit_liner)
        //フォームセクションごとにスペース入れる処理。試しに入れてみた。
        val space = Space(activity)
        space.setLayoutParams(layoutParamsSpace)
        rootView.addView(space)

        //レイアウトの設置位置の設定
        val payload = mapOf("token" to token, "project_id" to projectId)
        val payloadItem = mapOf("token" to token, "project_id" to projectId )
        //item_idでの取得ができない!!
        BlasRestField(payload, ::getSuccess, ::getFail).execute()
        BlasRestItem("search", payloadItem, ::itemRecv, ::itemRecvError).execute()
    }

    /**
     * フィールドを取得
     */
    private fun getSuccess(result: JSONObject) {
        formFieldList.clear()
        formFieldList = RestHelper().createFormField(result)
        if (jsonItem.isNotEmpty() && formFieldList.isNotEmpty()) {
            createForm()
        }

    }

    /**
     * フィールド取得失敗時
     */
    private fun getFail(errorCode: Int, aplCode:Int) {
    
        var message:String? = null
        
        when(errorCode) {
            BlasRestErrCode.DB_NOT_FOUND_RECORD -> {
                message = getString(R.string.record_not_found)
            }
            BlasRestErrCode.NETWORK_ERROR -> {
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            else-> {
                //サーバでエラーが発生しました(要因コード)
                message = getString(R.string.server_error, errorCode)
            }
        }

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()
        
        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

    /**
     * アイテムを取得
     */
    private fun itemRecv(result: JSONObject){
        formDefaultValueList.clear()
        jsonItem.clear()
        jsonItem.set(key = "1",value = result)
        if (jsonItem.isNotEmpty() && formFieldList.isNotEmpty()) {
            createForm()
        }


    }

    private fun itemRecvError(errorCode: Int, aplCode:Int) {

        var message:String? = null

        when(errorCode) {
            BlasRestErrCode.DB_NOT_FOUND_RECORD -> {
                message = getString(R.string.record_not_found)
            }
            BlasRestErrCode.NETWORK_ERROR -> {
                //サーバと通信できません
                message = getString(R.string.network_error)
            }
            else-> {
                //サーバでエラーが発生しました(要因コード)
                message = getString(R.string.server_error, errorCode)
            }
        }

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()

        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

    /**
     * フォームの作成処理
     */
    private fun createForm() {
        var cnt = 1
        var radioCount = 1
        var checkCount = 1
        //項目の並び替え
        val sortForm = formFieldList.values.sortedBy { it["field_col"]!!.toInt() }
        //指定したitemIDに入力されている値を取得(複数件の取得)
        formDefaultValueList.addAll(
            RestHelper().createDefaultValueList(
                jsonItem,
                sortForm.size,
                itemId
            )
        )
        sortForm.forEach {

            /**
             * formInfoには以下の情報が入っている。
             * ・title => 項目のタイトル
             * ・type => 項目のタイプ(日付入力など)
             * ・choiceValue => 項目が持つ選択肢
             * ・nullable => 項目がnullが許容するかの定義
             * ・unique => 項目が重複を許可するかの定義
             * ・parentFieldId =>親フィールドID
             * ・fieldId => フィールドID
             */
            val formInfo = formAction.typeCheck(it)
            idMap.set(cnt.toString(),formInfo.fieldId.toString() )
            //先に項目のタイトルをセットする。入力必須を表示
            val formSectionTitle = formAction.createFormSectionTitle(layoutParams, formInfo)
            textViewMap.set(cnt.toString(), formSectionTitle)
            rootView.addView(formSectionTitle)

            //フォームの項目の情報をメンバ変数に格納
            val typeMap = formAction.createFormInfoMap(formInfo)
            formInfoMap.set(key = "${cnt}", value = typeMap)

            //フォームの作成処理。項目の数だけ行う。
            when (formInfo.type) {
                FieldType.TEXT_FIELD-> {
                    //自由入力(1行)と自由入力(複数行)
                    //editTextを作成
                    var formPart = formAction.createTextField(layoutParams, cnt, formInfo)
                    formPart = formAction.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    rootView.addView(formPart)
                    //配列にeditTextの情報を格納。
                    editMap.set(key = "col_${cnt}", value = formPart)
                }
                FieldType.TEXT_AREA ->{
                    var formPart = formAction.createTextAlea(layoutParams, cnt, formInfo)
                    formPart = formAction.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    rootView.addView(formPart)
                    //配列にeditTextの情報を格納。
                    editMap.set(key = "col_${cnt}", value = formPart)
                }

                FieldType.DATE_TIME -> {
                    //日付入力
                    var formPart = formAction.createDateTime(layoutParams, cnt, formInfo)
                    formPart = formAction.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    formPart = setClickDateTime(formPart)
                    rootView.addView(formPart)

                    //配列にeditTextを格納
                    editMap.set(key = "col_${cnt}", value = formPart)
                }

                FieldType.TIME -> {
                    //時間入力
                    var formPart = formAction.createDateTime(layoutParams, cnt, formInfo)
                    //タップ時処理追加
                    formPart = formAction.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    formPart = setClickTime(formPart)
                    rootView.addView(formPart)

                    //配列にeditTextを格納
                    editMap.set(key = "col_${cnt}", value = formPart)
                }

                FieldType.SINGLE_SELECTION -> {
                    //ラジオボタンの時
                    var selectedValueId: Int = -1
                    //ラジオグループの作成
                    val formGroup = formAction.createRadioGrop(layoutParams, cnt)
                    if(formInfo.parentFieldId == "0") {
                        val radioButtonValues = formInfo.choiceValue
                        val colTargetPart:MutableList<String> = mutableListOf()
                        if (radioButtonValues != null) {
                            radioButtonValues.forEach {
                                //ラジオボタン作成
                                val formPart =
                                    formAction.createSingleSelection(layoutParams, it, radioCount)
                                //初期値の検索
                                selectedValueId = formAction.setDefaultValueRadio(
                                    formDefaultValueList[0].get("fld${cnt}"),
                                    formPart,
                                    radioCount,
                                    selectedValueId
                                )
                                formPart.setOnClickListener {
                                    Log.d("デバック用ログ", "選択された値=>${formPart.text}")
                                    var colNum:String? = null
                                    valueIdColMap.forEach{
                                        val protoNum = it.key
                                        colNum = formAction.getColNum(protoNum)
                                        val colId = idMap[colNum.toString()]
                                        var flg = false
                                        parentMap.forEach{
                                            //親IDが同じかつkeyWordが一致した時の処理
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
                                //値のセット
                                radioValue.set(key = "${radioCount}", value = formPart)
                                valueIdColMap.set(key = "${cnt}_${radioCount}",value = "${formPart.text}")
                                formGroup.addView(formPart)
                                colTargetPart.add(radioCount.toString())
                                radioCount += 1


                            }
                        }
                        //初期値が登録されている場合、初期値をセットする
                        selectValueMap.set(cnt.toString(),colTargetPart)
                        formAction.setDefaultValueRadioGroup(formGroup,selectedValueId)
                        rootView.addView(formGroup)
                        radioGroupMap.set(key = "col_${cnt}", value = formGroup)
                    }else{
                        val information :MutableMap<String,String> = mutableMapOf()
                        val parentSelect = formAction.getParentSelect(formInfo.choiceValue)
                        val colTargetPart:MutableList<String> = mutableListOf()
                        Log.d("デバック用ログ","親の値が[${parentSelect}]の時、処理を走らせる")
                        information.set(key = "keyWord" ,value = parentSelect.toString())
                        information.set(key = "parentId",value = formInfo.parentFieldId.toString())
                        parentMap.set(cnt.toString(),information)
                        val selectValues = formAction.getSelectValue(formInfo.choiceValue)
                        selectValues.forEach{
                            val formPart =
                                formAction.createSingleSelection(layoutParams, it, radioCount)
                            selectedValueId = formAction.setDefaultValueRadio(
                                formDefaultValueList[0].get("fld${cnt}"),
                                formPart,
                                radioCount,
                                selectedValueId
                            )
                            formPart.isEnabled = false

                            //値のセット
                            radioValue.set(key = "${radioCount}", value = formPart)
                            colTargetPart.add(radioCount.toString())
                            formGroup.addView(formPart)
                            radioCount += 1
                        }
                        //初期値が登録されている場合、初期値をセットする
                        selectValueMap.set(cnt.toString(),colTargetPart)
                        formAction.setDefaultValueRadioGroup(formGroup,selectedValueId)
                        rootView.addView(formGroup)
                        radioGroupMap.set(key = "col_${cnt}", value = formGroup)
                    }
                }

                FieldType.MULTIPLE_SELECTION -> {
                    //チェックボックスの時
                    val colCheckMap: MutableMap<String?, CheckBox?> = mutableMapOf()
                    //登録してあるチェックボックスの数だけ行う
                    val checkBoxValues = formInfo.choiceValue
                    if(checkBoxValues != null) {
                        checkBoxValues.forEach {
                            val formPart =
                                formAction.createMutipleSelection(layoutParams, it, checkCount)
                            //初期値の設定
                            formAction.setDefaultValueCheck(
                                formDefaultValueList[0].get("fld${cnt}"),
                                formPart
                            )
                            rootView.addView(formPart)
                            colCheckMap.set(key = "col_${cnt}_${checkCount}", value = formPart)
                            checkCount += 1

                        }
                    }
                    checkMap.set(key = "col_${cnt}", value = colCheckMap)
                }
                FieldType.KENPIN_RENDOU_QR,
                FieldType.QR_CODE,
                FieldType.TEKKILYO_RENDOU_QR -> {
                    //  QR code 読み取り
                    val layout = requireActivity().layoutInflater.inflate(R.layout.cell_qr_item, null)
                    rootView.addView(layout)
                    val ed = layout.findViewById<EditText>(R.id.editText)
                    layout.findViewById<Button>(R.id.button)?.setOnClickListener {
                        //190エラー
                        qrCodeView = layout.findViewById(R.id.editText)
                        val intent = Intent(activity, QRActivity::class.java)
                        intent.putExtra("colNumber","${cnt}")
                        startActivityForResult(intent, QRActivity.QR_CODE)
                    }
                    //初期値を設定。配列に格納
                    ed.setText(formDefaultValueList[0].get("fld${cnt}").toString())
                    editMap.set(key = "col_${cnt}", value = ed)
                }
                FieldType.CHECK_VALUE->{
                    //値チェック
                    val layout = requireActivity().layoutInflater.inflate(R.layout.cell_checkvalue, null)
                    rootView.addView(layout)
                    val value = layout.findViewById<EditText>(R.id.editValue)
                    val memo = layout.findViewById<EditText>(R.id.editMemo)
                    val valueText = helper.createCheckValueText(formDefaultValueList[0].get("fld${cnt}").toString(),"value")
                    val memoText = helper.createCheckValueText(formDefaultValueList[0].get("fld${cnt}").toString(),"memo")

                    value.setText(valueText)
                    memo.setText(memoText)

                    editMap.set(key = "col_${cnt}", value = value)
                    memoMap.set(key = "col_${cnt}",value = memo)

                    val information :MutableMap<String,String> = mutableMapOf()
                    information.set(key = "parentId",value = formInfo.parentFieldId.toString())
                    parentMap.set(cnt.toString(),information)

                }
                FieldType.ACOUNT_NAME->{
                    //アカウント名。現在実装不可
                    val view = TextView(activity)
                    view.setText("アカウント名は実装中です。完成までお待ちください")
                    //文字の色変更したい。
                    view.setTextColor(Color.BLACK)
                    view.setLayoutParams(layoutParams)
                    rootView.addView(view)

                }
                FieldType.SIG_FOX->{
                    //シグフォックス。現在実装不可
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



        //親の値があった時の処理
        parentMap.forEach{
            Log.d("デバック用ログ","parentMap=>${it}")
            val parent = it
            idMap.forEach{
                if(it.value == parent.value["parentId"] && formDefaultValueList[0]["fld${it.key}"] == parent.value["keyWord"]){
                    Log.d("デバック用ログ","分岐完了")
                    val list = selectValueMap[parent.key]!!
                    list.forEach {
                        //ラジオボタンを編集可能にする
                        radioValue[it]!!.isEnabled = true
                        Log.d("デバック用ログ", "${it}")
                    }
                }
            }
        }


        //ボタンの作成処理
        val button = Button(activity)
        button.text = BTN_SAVE
        button.setLayoutParams(layoutParams)
        rootView.addView(button)

        //ボタン押下時の処理
        button.setOnClickListener {
            parentChk = true
            val parentErrorMap:MutableMap<String,MutableMap<String,String?>> = mutableMapOf()

            val payload: MutableMap<String, String?> =
                mutableMapOf("token" to token, "project_id" to projectId, "item_id" to itemId)
            val nullChk: MutableMap<Int, MutableMap<String, String>> = mutableMapOf()
            var cnt = 1
            var errorCnt: Int = 0
            Log.d("aafqgrea", "${payload}")

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

                    FieldType.SINGLE_SELECTION -> {
                        //ラジオボタン
                        val checkedRadioId = formAction.getCheckedRadioId(radioGroupMap, cnt)
                        value = formAction.getCheckedValue(radioValue, checkedRadioId)
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
                    FieldType.TEKKILYO_RENDOU_QR->{
                        //格納した値から取得
                        val colCheckMap = editMap.get("col_${cnt}")
                        if(colCheckMap != null) {
                            value = colCheckMap.text.toString()
                            payload.set("fld${cnt}", "${value}")
                        }
                    }
                    FieldType.CHECK_VALUE-> {
                        parentChk = true
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
                                        val checkedRadioId = formAction.getCheckedRadioId(radioGroupMap, cnt)
                                        parentValue = formAction.getCheckedValue(radioValue, checkedRadioId)
                                    }
                                    FieldType.MULTIPLE_SELECTION -> {
                                        //チェックボックス
                                        val colCheckMap = checkMap.get("col_${cnt}")
                                        parentValue = formAction.getCheckedValues(colCheckMap)
                                    }
                                    FieldType.KENPIN_RENDOU_QR,
                                    FieldType.QR_CODE,
                                    FieldType.TEKKILYO_RENDOU_QR -> {
                                        //配列に格納した値を取得
                                        val colCheckMap = editMap.get("col_${cnt}")
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
                //入力必須項目のエラーがないかを記録。
                val nullChkMap: MutableMap<String, String> =
                    formAction.chkNull(it.value["require"], value)
                nullChk.set(cnt, nullChkMap)
                cnt += 1
            }

            //エラーチェック
            errorCnt = formAction.countNullError(nullChk, textViewMap)
            if (errorCnt == 0 && parentChk) {
                BlasRestItem("update", payload, ::updateSuccess, ::updateError).execute()
            }
        }
    }


    /**
     * 更新成功時の処理
     */
    fun updateSuccess(result: JSONObject){
        //更新成功を通知
        Toast.makeText(activity, getText(R.string.success_data_update), Toast.LENGTH_LONG).show()
    }

    /**
     * 更新失敗時の処理
     */
    fun updateError(errorCode: Int, aplCode:Int){
        //更新失敗を通知
        Toast.makeText(activity, getText(R.string.error_data_update), Toast.LENGTH_LONG).show()
    }

    /**
     * 日付フィールドタップ時の処理
     */
    private fun setClickDateTime(formPart:EditText): EditText {
        //editTextタップ時の処理
        formPart.setOnClickListener {
            val dtp = DatePickerDialog(
                getContext()!!,
                DatePickerDialog.OnDateSetListener { view, y, m, d ->
                    Toast.makeText(activity, "日付を選択しました${y}/${m + 1}/${d}", Toast.LENGTH_LONG)
                        .show()
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
        formPart.setOnClickListener{
            val tp = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener{ view, hour, minute->
                    Toast.makeText(activity, "時間を選択しました${hour}:${minute}", Toast.LENGTH_LONG).show()
                    formPart.setText(String.format("%02d:%02d",hour,minute))
                },hour,minute,true)
            tp.show()
        }
        return formPart
    }

    /**
     * この処理がアクティビティから値を受け取って閉じる処理かな
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //返り値を持つactivityを作成。resultがOKかつcodeがQRコードの時
        if (resultCode == Activity.RESULT_OK && requestCode == QRActivity.QR_CODE) {

            val qr = data?.getStringExtra("qr_code")
            qrCodeView.setText(qr)
        }
    }
}
