package com.v3.basis.blas.ui.item.item_create


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
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.formaction.FormActionDataCreate
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import org.json.JSONObject
import java.nio.channels.NonReadableChannelException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ItemCreateFragment : Fragment() {
    private var token: String? = null
    private var projectId: String? = null
    private var rootView: LinearLayout? = null

    private var formInfoMap: MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap: MutableMap<String, EditText?>? = mutableMapOf()
    private var radioGroupMap: MutableMap<String, RadioGroup?>? = mutableMapOf()
    private var radioValue: MutableMap<String, RadioButton?>? = mutableMapOf()
    private var checkMap: MutableMap<String, MutableMap<String?, CheckBox?>>? = mutableMapOf()
    private var textViewMap: MutableMap<String, TextView> = mutableMapOf()
    private var layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
    private var layoutParamsSpace =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50)

    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)

    private var Item = ItemActivity()
    private var formAction: FormActionDataCreate? = null

    private lateinit var qrCodeView: EditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        //初期値の取得
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras?.getString("token")
            Log.d("token_item", "${token}")
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras?.getString("project_id")
        }

        formAction = FormActionDataCreate(token!!, activity!!)
        return inflater.inflate(R.layout.fragment_item_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //コンテンツを配置するLinearLayoutを取得
        rootView = view.findViewById<LinearLayout>(R.id.item_create_liner)
        //フォームセクションごとにスペース入れる処理。試しに入れてみた。
        val space = Space(activity)
        space.setLayoutParams(layoutParamsSpace)
        rootView!!.addView(space)
        //レイアウトの設置位置の設定
        val payload = mapOf("token" to token, "project_id" to projectId)
        BlasRestField(payload, ::getSuccess, ::getFail).execute()

    }

    private fun getSuccess(result: JSONObject?) {
        //カラム順に並べ替える
        var cnt = 1
        var radioCount = 1
        var checkCount = 1
        if (result != null) {
            //colによる並び替えが発生しているため、ソートを行う
            // val resultSort = result!!.sortedBy { it["col"]!!.toInt() }
            val sortFormFieldList = RestHelper().createFormField(result)
            val test = sortFormFieldList.values.sortedBy { it["field_col"]!!.toInt() }
            test.forEach {
                Log.d("aaaaaaa", "${it}")

                /**
                 * formInfoには以下の情報が入っている。
                 * ・title => 項目のタイトル
                 * ・type => 項目のタイプ(日付入力など)
                 * ・choiceValue => 項目が持つ選択肢
                 * ・require => 項目がnullが許容するかの定義
                 * ・unique => 項目が重複を許可するかの定義
                 */
                val formInfo = formAction!!.typeCheck(it)
                //先に項目のタイトルをセットする
                val formSectionTitle = formAction!!.createFormSectionTitle(layoutParams, formInfo)
                //formSectionTitle.setError("入力必須です")
                textViewMap.set(cnt.toString(), formSectionTitle)
                rootView!!.addView(formSectionTitle)

                //フォームの項目の情報をメンバ変数に格納
                val typeMap = formAction!!.createFormInfoMap(formInfo)

                formInfoMap.set(key = "${cnt}", value = typeMap)

                when (formInfo.type) {
                    FieldType.TEXT_FIELD, FieldType.TEXT_AREA -> {
                        //自由入力(1行)と自由入力(複数行)
                        //editTextを作成
                        val formPart = formAction!!.createTextField(layoutParams, cnt, formInfo)
                        rootView!!.addView(formPart)
                        //配列にeditTextの情報を格納。
                        editMap!!.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.DATE_TIME -> {
                        //日付入力
                        //editTextを作成
                        var formPart = formAction!!.createDateTime(layoutParams, cnt, formInfo)
                        formPart = setClickDateTime(formPart)
                        rootView!!.addView(formPart)


                        //配列にeditTextを格納
                        editMap!!.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.TIME -> {
                        //時間入力
                        //editText作成
                        var formPart = formAction!!.createDateTime(layoutParams, cnt, formInfo)
                        formPart = setClickTime(formPart)
                        rootView!!.addView(formPart)

                        //配列にeditTextを格納
                        editMap!!.set(key = "col_${cnt}", value = formPart)
                    }

                    FieldType.SINGLE_SELECTION -> {
                        //ラジオボタンの時
                        val formGroup = formAction!!.createRadioGrop(layoutParams, cnt)
                        formInfo.choiceValue!!.forEach {
                            val formPart =
                                formAction!!.createSingleSelection(layoutParams, it, radioCount)
                            radioValue!!.set(key = "${radioCount}", value = formPart)
                            radioCount += 1
                            formGroup.addView(formPart)
                        }
                        rootView!!.addView(formGroup)
                        radioGroupMap!!.set(key = "col_${cnt}", value = formGroup)
                    }

                    FieldType.MULTIPLE_SELECTION -> {
                        //チェックボックスの時
                        val colCheckMap: MutableMap<String?, CheckBox?> = mutableMapOf()
                        formInfo.choiceValue!!.forEach {
                            val formPart =
                                formAction!!.createMutipleSelection(layoutParams, it, checkCount)
                            rootView!!.addView(formPart)
                            colCheckMap!!.set(key = "col_${cnt}_${checkCount}", value = formPart)
                            checkCount += 1

                        }
                        checkMap!!.set(key = "col_${cnt}", value = colCheckMap)
                    }
                    FieldType.QR_CODE,
                    FieldType.KENPIN_RENDOU_QR,
                    FieldType.TEKKILYO_RENDOU_QR->{

                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_qr_item, null)
                        rootView!!.addView(layout)
                        layout.findViewById<Button>(R.id.button)?.setOnClickListener{

                            qrCodeView = layout.findViewById(R.id.editText)
                            val intent = Intent(activity, QRActivity::class.java)
                            intent.putExtra("colNumber","${cnt}")
                            startActivityForResult(intent, QRActivity.QR_CODE)
                        }
                    }
                }
                Log.d("atait","タイプは=>${formInfo.type}")

                //フォームセクションごとにスペース入れる処理。試しに入れてみた。
                val space = Space(activity)
                space.setLayoutParams(layoutParamsSpace)
                rootView!!.addView(space)
                cnt += 1
            }
            //ボタンの作成処理
            val button = Button(activity)
            button.text = "send"
            button.setLayoutParams(layoutParams)
            rootView!!.addView(button)

            //ボタン押下時の処理
            button.setOnClickListener {

                var payload: MutableMap<String, String?> =
                    mutableMapOf("token" to token, "project_id" to projectId)
                var cnt = 1
                var errorCnt = 0
                var nullChk: MutableMap<Int, MutableMap<String, String>> = mutableMapOf()
                formInfoMap.forEach {
                    var value = ""
                    Log.d("ItemCreateFragment", "「SEND」ボタンが押されました")

                    var cnt = 1
                    var params = mutableMapOf<String, String?>()

                    formInfoMap.forEach {

                        Log.d("send button", "it.value === " + it.value)

                        //val field_col = Integer.parseInt(editMap!!.get("field_col").toString())
                        val field_col = it.value["field_col"].toString()
                        //val field_col = "0"

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
                                val checkedRadioId =
                                    formAction!!.getCheckedRadioId(radioGroupMap, cnt)
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
                            FieldType.TEKKILYO_RENDOU_QR -> {
                                // TODO:まだ
                            }

                        }
                        val nullChkMap: MutableMap<String, String> =
                            formAction!!.chkNull(it.value["require"], value)
                        nullChk.set(cnt, nullChkMap)
                        cnt += 1
                    }

                    errorCnt = formAction!!.countNullError(nullChk, textViewMap)
                    if (errorCnt == 0) {
                        BlasRestItem("create", payload, ::createSuccess, ::createError).execute()
                    }
                }
            }
        }
    }
    fun getButtonSuccess(json: JSONObject) {
        Log.d("getButtonSuccess", "start")

        val intent = Intent(activity, ItemActivity::class.java)
        intent.putExtra("token", token)
        intent.putExtra("project_id", projectId)
        startActivity(intent)

    }

    /**
     * フィールド取得失敗時
     */
    fun getFail(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

    /**
     * データの作成失敗時の処理
     */
    fun createError(errorCode: Int) {
        Log.d("sippai ", "失敗")
        Toast.makeText(activity, getText(R.string.error_data_create), Toast.LENGTH_LONG).show()
    }

    /**
     * データの作成成功時
     */
    fun createSuccess(result: JSONObject) {
        Log.d("seikou ", "成功")
        Toast.makeText(activity, getText(R.string.success_data_create), Toast.LENGTH_LONG)
            .show()
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
        formPart.setOnClickListener {
            val tp = TimePickerDialog(
                getContext()!!,
                TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                    Toast.makeText(activity, "時間を選択しました${hour}:${minute}", Toast.LENGTH_LONG)
                        .show()
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
}


