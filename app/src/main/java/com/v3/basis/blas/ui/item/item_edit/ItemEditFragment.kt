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
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
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
    private var token: String? = null
    private var projectId: String? = null
    private var itemId: String? = null
    private var rootView: LinearLayout? = null

    private var formInfoMap:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap:MutableMap<String, EditText?>? = mutableMapOf()
    private var radioGroupMap:MutableMap<String, RadioGroup?>? = mutableMapOf()
    private var radioValue :MutableMap<String, RadioButton?>? = mutableMapOf()
    private var checkMap:MutableMap<String,MutableMap<String?, CheckBox?>>? = mutableMapOf()
    private var formFieldList:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var jsonItem:MutableMap<String,JSONObject> = mutableMapOf()
    private val formDefaultValueList: MutableList<MutableMap<String, String?>> = mutableListOf()
    private var textViewMap:MutableMap<String,TextView> = mutableMapOf()

    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,50)

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)

    private var formAction: FormActionDataEdit? = null

    private lateinit var qrCodeView: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras?.getString("token")
            Log.d("token_item", "${token}")
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras?.getString("project_id")
        }
        if (extras?.getString("item_id") != null) {
            itemId = extras?.getString("item_id")
        }

        formAction = FormActionDataEdit(token!!,activity!!)


        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //コンテンツを配置するLinearLayoutを取得
        rootView = view.findViewById<LinearLayout>(R.id.item_edit_liner)
        //フォームセクションごとにスペース入れる処理。試しに入れてみた。
        val space = Space(activity)
        space.setLayoutParams(layoutParamsSpace)
        rootView!!.addView(space)

        //レイアウトの設置位置の設定
        val payload = mapOf("token" to token, "project_id" to projectId)
        //item_idでの取得ができない？
        BlasRestField(payload, ::getSuccess, ::getFail).execute()
        BlasRestItem("search", payload, ::itemRecv, ::itemRecvError).execute()
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
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
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
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
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
             */
            val formInfo = formAction!!.typeCheck(it)
            //先に項目のタイトルをセットする。入力必須を表示
            val formSectionTitle = formAction!!.createFormSectionTitle(layoutParams, formInfo)
            textViewMap.set(cnt.toString(), formSectionTitle)
            rootView!!.addView(formSectionTitle)

            //フォームの項目の情報をメンバ変数に格納
            val typeMap = formAction!!.createFormInfoMap(formInfo)
            formInfoMap.set(key = "${cnt}", value = typeMap)

            //フォームの作成処理。項目の数だけ行う。
            when (formInfo.type) {
                FieldType.TEXT_FIELD-> {
                    //自由入力(1行)と自由入力(複数行)
                    //editTextを作成
                    var formPart = formAction!!.createTextField(layoutParams, cnt, formInfo)
                    formPart = formAction!!.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    rootView!!.addView(formPart)
                    //配列にeditTextの情報を格納。
                    editMap!!.set(key = "col_${cnt}", value = formPart)
                }
                FieldType.TEXT_AREA ->{
                    var formPart = formAction!!.createTextAlea(layoutParams, cnt, formInfo)
                    formPart = formAction!!.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    rootView!!.addView(formPart)
                    //配列にeditTextの情報を格納。
                    editMap!!.set(key = "col_${cnt}", value = formPart)
                }

                FieldType.DATE_TIME -> {
                    //日付入力
                    var formPart = formAction!!.createDateTime(layoutParams, cnt, formInfo)
                    formPart = formAction!!.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    formPart = setClickDateTime(formPart)
                    rootView!!.addView(formPart)

                    //配列にeditTextを格納
                    editMap!!.set(key = "col_${cnt}", value = formPart)
                }

                FieldType.TIME -> {
                    //時間入力
                    var formPart = formAction!!.createDateTime(layoutParams, cnt, formInfo)
                    //タップ時処理追加
                    formPart = formAction!!.setDefaultValueEdit(
                        formDefaultValueList[0].get("fld${cnt}"),
                        formPart
                    )
                    formPart = setClickTime(formPart)
                    rootView!!.addView(formPart)

                    //配列にeditTextを格納
                    editMap!!.set(key = "col_${cnt}", value = formPart)
                }

                FieldType.SINGLE_SELECTION -> {
                    //ラジオボタンの時
                    var selectedValueId: Int = -1
                    //ラジオグループの作成
                    val formGroup = formAction!!.createRadioGrop(layoutParams, cnt)
                    formInfo.choiceValue!!.forEach {
                        //ラジオボタン作成
                        val formPart = formAction!!.createSingleSelection(layoutParams, it, radioCount)
                        //初期値の検索
                        selectedValueId = formAction!!.setDefaultValueRadio(
                            formDefaultValueList[0].get("fld${cnt}"),
                            formPart,
                            radioCount,
                            selectedValueId
                        )
                        //値のセット
                        radioValue!!.set(key = "${radioCount}", value = formPart)
                        formGroup.addView(formPart)
                        radioCount += 1

                    }
                    //初期値が登録されている場合、初期値をセットする
                    formAction!!.setDefaultValueRadioGroup(formGroup,selectedValueId)
                    rootView!!.addView(formGroup)
                    radioGroupMap!!.set(key = "col_${cnt}", value = formGroup)
                }

                FieldType.MULTIPLE_SELECTION -> {
                    //チェックボックスの時
                    var colCheckMap: MutableMap<String?, CheckBox?> = mutableMapOf()
                    //登録してあるチェックボックスの数だけ行う
                    formInfo.choiceValue!!.forEach {
                        val formPart =
                            formAction!!.createMutipleSelection(layoutParams, it, checkCount)
                        //初期値の設定
                        formAction!!.setDefaultValueCheck(formDefaultValueList[0].get("fld${cnt}"), formPart)
                        rootView!!.addView(formPart)
                        colCheckMap!!.set(key = "col_${cnt}_${checkCount}", value = formPart)
                        checkCount += 1

                    }
                    checkMap!!.set(key = "col_${cnt}", value = colCheckMap)
                }
                FieldType.KENPIN_RENDOU_QR,
                FieldType.QR_CODE,
                FieldType.TEKKILYO_RENDOU_QR -> {

                    //  QR code 読み取り
                    val layout = requireActivity().layoutInflater.inflate(R.layout.cell_qr_item, null)
                    rootView!!.addView(layout)
                    val value = "aaaa"
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
                    editMap!!.set(key = "col_${cnt}", value = ed)
                }
            }
            //フォームセクションごとにスペース入れる処理。試しに入れてみた。
            val space = Space(activity)
            space.setLayoutParams(layoutParamsSpace)
            rootView!!.addView(space)
            cnt += 1
        }


        //ボタンの作成処理
        val button = Button(activity)
        button.text = BTN_SAVE
        button.setLayoutParams(layoutParams)
        rootView!!.addView(button)

        //ボタン押下時の処理
        button.setOnClickListener {

            var payload: MutableMap<String, String?> =
                mutableMapOf("token" to token, "project_id" to projectId, "item_id" to itemId)
            var nullChk: MutableMap<Int, MutableMap<String, String>> = mutableMapOf()
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
                        value = formAction!!.pickUpValue(editMap, cnt)
                        payload.set("fld${cnt}", value)
                    }

                    FieldType.SINGLE_SELECTION -> {
                        //ラジオボタン
                        val checkedRadioId = formAction!!.getCheckedRadioId(radioGroupMap, cnt)
                        value = formAction!!.getCheckedValue(radioValue, checkedRadioId)
                        payload.set("fld${cnt}", "${value}")
                    }

                    FieldType.MULTIPLE_SELECTION -> {
                        //チェックボックス
                        val colCheckMap = checkMap!!.get("col_${cnt}")
                        value = formAction!!.getCheckedValues(colCheckMap)
                        payload.set("fld${cnt}", "${value}")
                    }
                    FieldType.KENPIN_RENDOU_QR,
                    FieldType.QR_CODE,
                    FieldType.TEKKILYO_RENDOU_QR->{
                        //格納した値から取得

                        val colCheckMap = editMap!!.get("col_${cnt}")
                        Log.d("testytesttt","${colCheckMap!!.text}")
                        value = colCheckMap!!.text.toString()
                        payload.set("fld${cnt}", "${value}")

                    }

                }
                //入力必須項目のエラーがないかを記録。
                val nullChkMap: MutableMap<String, String> =
                    formAction!!.chkNull(it.value["require"], value)
                nullChk.set(cnt, nullChkMap)
                cnt += 1
            }

            //エラーチェック
            errorCnt = formAction!!.countNullError(nullChk, textViewMap)
            if (errorCnt == 0) {
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
            val tp = TimePickerDialog(getContext()!!,
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
