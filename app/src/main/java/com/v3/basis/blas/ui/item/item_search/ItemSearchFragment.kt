package com.v3.basis.blas.ui.item.item_search

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.text.MessagePattern
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.activity.ItemSearchResultActivity
import com.v3.basis.blas.activity.TerminalActivity
import com.v3.basis.blas.blasclass.app.BlasCom
import com.v3.basis.blas.blasclass.app.BlasDef.Companion.BTN_FIND
import com.v3.basis.blas.blasclass.app.BlasMsg
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.formaction.FormActionDataSearch
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import com.v3.basis.blas.ui.ext.addTitle
import com.v3.basis.blas.ui.ext.hideKeyboardWhenTouch
import kotlinx.android.synthetic.main.fragment_item_search.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.concurrent.timerTask

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemSearchFragment : Fragment() {
    private var receiveData : Boolean = true
    private var msg = BlasMsg()
    private val toastErrorLen = Toast.LENGTH_LONG
    private var toastSuccessLen = Toast.LENGTH_SHORT

    //文字型またはview型
    private lateinit var token: String
    private lateinit var projectId: String
    private lateinit var rootView: LinearLayout
    //コレクション各種
    private var formInfoMap:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap:MutableMap<String, EditText?> = mutableMapOf()
    private var dateTime:MutableMap<String,EditText> = mutableMapOf()
    private var checkMap:MutableMap<String,MutableMap<String?, CheckBox?>> = mutableMapOf()
    private var searchValue:MutableMap<String,String?> = mutableMapOf()
    private var errorList:MutableList<String> = mutableListOf()
    private var titleMap:MutableMap<String,TextView> = mutableMapOf()
    //パラメータ
    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,50)
    //データピッカー・タイムピッカー用
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)
    //インスタンス作成
    private lateinit var formAction:FormActionDataSearch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addTitle("projectName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras.getString("token").toString()
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras.getString("project_id").toString()
        }
        try {
            if(token != null && projectId != null && activity != null) {
                formAction = FormActionDataSearch(token, activity!!)
            }else{
                throw java.lang.Exception("Failed to receive internal data ")
            }
        }catch (e:Exception){
            receiveData = false
            val errorMessage = msg.createErrorMessage("getFail")
            Toast.makeText(activity, errorMessage, toastErrorLen).show()
        }
        //ここでもエスケープ処理追加
        val root= inflater.inflate(R.layout.fragment_item_search, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view.findViewById<LinearLayout>(R.id.item_search_liner)

        if(receiveData){
            //レイアウトの設置位置の設定
            val payload = mapOf("token" to token, "project_id" to projectId)
            BlasRestField(payload, ::getSuccess, ::getFail).execute()

        }

        search_scroller.hideKeyboardWhenTouch(this)
    }

    private fun getSuccess(result:JSONObject?) {
        //カラム順に並べ替える
        var cnt = 1
        var checkCount = 1
        if (result != null) {

            //一番上。フリーワード検索を表示する処理
            val space = formAction.createSpace(layoutParamsSpace)
            val title = formAction.createFreeWordSearchTitle(layoutParams)
            val freeWordSearch = formAction.createFreeWordSearch(layoutParams)
            editMap.set(key = "col_${0}", value = freeWordSearch)
            rootView.addView(space)
            rootView.addView(title)
            rootView.addView(freeWordSearch)
            titleMap.set(key = "freeWord", value = title)

            //colによる並び替えが発生しているため、ソートを行う
            val sortFormFieldList = RestHelper().createFormField(result)
            val test = sortFormFieldList.values.sortedBy { it["field_col"] !!.toInt()}
            test.forEach  {
                /**
                 * formInfoには以下の情報が入っている。
                 * ・title => 項目のタイトル
                 * ・type => 項目のタイプ(日付入力など)
                 * ・choiceValue => 項目が持つ選択肢
                 * ・nullable => 項目がnullが許容するかの定義
                 * ・unique => 項目が重複を許可するかの定義
                 */
                val formInfo= formAction.typeCheck(it)
                //先に項目のタイトルをセットする
                val formSectionTitle = formAction.createFormSectionTitle(layoutParams,formInfo)
                rootView.addView(formSectionTitle)
                titleMap.set(key = cnt.toString(),value = formSectionTitle)

                //フォームの項目の情報をメンバ変数に格納
                val typeMap = formAction.createFormInfoMap(formInfo)
                formInfoMap.set(key = "${cnt}",value =typeMap )


                when(formInfo.type){
                    FieldType.TEXT_FIELD,
                    FieldType.TEXT_AREA,
                    FieldType.QR_CODE,
                    FieldType.TEKKILYO_RENDOU_QR,
                    FieldType.KENPIN_RENDOU_QR,
                    FieldType.SIG_FOX,
                    FieldType.ACOUNT_NAME->{
                        //自由入力(1行)または自由入力(複数行)
                        val formPart =formAction.createTextField(layoutParams,cnt,formInfo)
                        rootView.addView(formPart)

                        //配列にeditTextの情報を格納。
                        editMap.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.DATE_TIME->{
                        //日付入力
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_day, null)
                        val minDay = layout.findViewById<EditText>(R.id.MinDay)
                        val maxDay = layout.findViewById<EditText>(R.id.MaxDay)
                        setClickDateTime(minDay)
                        setClickDateTime(maxDay)
                        rootView.addView(layout)
                        dateTime.set(key = "col_${cnt}_minDay",value =minDay )
                        dateTime.set(key = "col_${cnt}_maxDay",value =maxDay )
                    }

                    FieldType.TIME->{
                        //時間入力
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_datetime, null)
                        val minTime = layout.findViewById<EditText>(R.id.MinTime)
                        val maxTime = layout.findViewById<EditText>(R.id.MaxTime)
                        setClickTime(minTime)
                        setClickTime(maxTime)
                        rootView.addView(layout)
                        dateTime.set(key = "col_${cnt}_minTime",value = minTime )
                        dateTime.set(key = "col_${cnt}_maxTime",value = maxTime )

                    }

                    FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                        //チェックボックスの時
                        var colCheckMap : MutableMap<String?,CheckBox?> = mutableMapOf()
                        val colCheckBoxValues = formInfo.choiceValue
                        if(formInfo.parentFieldId =="0") {
                            if (colCheckBoxValues != null) {
                                colCheckBoxValues.forEach {
                                    val formPart =
                                        formAction.createMutipleSelection(
                                            layoutParams,
                                            it,
                                            checkCount
                                        )
                                    rootView.addView(formPart)
                                    colCheckMap.set(
                                        key = "col_${cnt}_${checkCount}",
                                        value = formPart
                                    )
                                    checkCount += 1

                                }
                                checkMap.set(key = "col_${cnt}", value = colCheckMap)
                            }
                        }else{
                            if (colCheckBoxValues != null) {
                                val testL = formAction.getSelectValue(colCheckBoxValues)
                                var vCnt = 0
                                colCheckBoxValues.forEach {
                                    val formPart =
                                        formAction.createMutipleSelection(
                                            layoutParams,
                                            testL[vCnt],
                                            checkCount
                                        )
                                    rootView.addView(formPart)
                                    colCheckMap.set(
                                        key = "col_${cnt}_${checkCount}",
                                        value = formPart
                                    )
                                    checkCount += 1
                                    vCnt += 1

                                }
                                checkMap.set(key = "col_${cnt}", value = colCheckMap)
                            }
                        }
                    }
                    FieldType.CHECK_VALUE->{
                        val layout = requireActivity().layoutInflater.inflate(R.layout.cell_search_checkvalue, null)
                        val value = layout.findViewById<EditText>(R.id.searchValue)
                        val memo = layout.findViewById<EditText>(R.id.searchMemo)

                        rootView.addView(layout)
                        editMap.set(key = "col_${cnt}_value",value = value )
                        editMap.set(key = "col_${cnt}_memo",value = memo )
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
            button.text = BTN_FIND
            button.setLayoutParams(layoutParams)
            rootView.addView(button)

            //ボタン押下時の処理
            button.setOnClickListener{

                if(errorList.size>0) {
                    errorList.forEach {
                        val text = formAction.test2(it,titleMap)
                        titleMap[it]?.setText(text)
                        titleMap[it]?.setTextColor(Color.DKGRAY)
                    }
                }
                errorList.clear()


                val freeWordEdit = editMap.get("col_0")!!
                val freeWordValue ="${freeWordEdit.text}"
                Log.d("検索結果(フリーワード)","${freeWordValue}")
                if(freeWordValue != "") {
                    val chk = formAction.valueChk(freeWordValue)
                    if (chk) {
                        errorList.add("freeWord")
                    }
                }
                searchValue.set("freeWord",freeWordValue)
                var cnt = 1
                var dateTimeCol = ""
                var checkValueCol = ""
                formInfoMap.forEach{
                    var value = ""
                    when(it.value["type"]){
                        FieldType.TEXT_FIELD,
                        FieldType.TEXT_AREA,
                        FieldType.QR_CODE,
                        FieldType.TEKKILYO_RENDOU_QR,
                        FieldType.KENPIN_RENDOU_QR,
                        FieldType.SIG_FOX ,
                        FieldType.ACOUNT_NAME->{
                            //自由入力(1行)・自由入力(複数行)
                            value = formAction.pickUpValue(editMap,cnt)
                            if(value != "") {
                                val chk = formAction.valueChk(value)
                                if (chk) {
                                    errorList.add(cnt.toString())
                                }
                            }
                            searchValue.set("fld${cnt}",value)
                        }

                        FieldType.DATE_TIME->{
                            //日付入力
                            value = formAction.pickUpDateTime(dateTime,cnt,"Day")
                            searchValue.set("fld${cnt}",value)
                            dateTimeCol += "${cnt},"
                        }
                        FieldType.TIME->{
                            //時間入力
                            value = formAction.pickUpDateTime(dateTime,cnt,"Time")
                            searchValue.set("fld${cnt}",value)
                            dateTimeCol += "${cnt},"
                        }

                        FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                            //チェックボックスとラジオボタン
                            val colCheckMap = checkMap.get("col_${cnt}")
                            value = formAction.getCheckedValues(colCheckMap)
                            searchValue.set("fld${cnt}",value)
                        }

                        FieldType.CHECK_VALUE->{
                            //値チェック時
                            value = formAction.pickupCheckValue(editMap,cnt)
                            searchValue.set("fld${cnt}",value)
                            checkValueCol += "${cnt},"

                        }
                    }
                    cnt += 1
                }


                if(errorList.size>0) {
                    //ここから検索処理入れる
                    //バリデーション入れるならここっす
                    errorList.forEach{
                        titleMap[it] = formAction.test(it,titleMap)
                    }

                }else {
                    //検索結果画面を開くための処理
                    val intent = Intent(activity, ItemSearchResultActivity::class.java)
                    val fldSize = searchValue.size - 1
                    intent.putExtra("token", token)
                    intent.putExtra("project_id", projectId)
                    intent.putExtra("freeWord", searchValue["freeWord"])
                    intent.putExtra("fldSize", fldSize.toString())

                    //dateTimeColに値が入っていた場合のみ処理を行う
                    if (dateTimeCol != "") {
                        val newDateTimeCol = dateTimeCol.dropLast(1)
                        intent.putExtra("dateTimeCol", newDateTimeCol)
                    }

                    if (checkValueCol != "") {
                        val newCheckValueCol = checkValueCol.dropLast(1)
                        intent.putExtra("checkValueCol", newCheckValueCol)
                    }

                    //検索する値を検索画面に送信する
                    for (idx in 1..searchValue.size - 1) {
                        intent.putExtra("fld${idx}", searchValue["fld${idx}"])
                    }
                    startActivity(intent)
                }

            }
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun getFail(errorCode: Int, aplCode:Int) {

        var message:String? = null

        message = BlasMsg().getMessage(errorCode,aplCode)

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show()

        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

    /**
     * 日付フィールドタップ時の処理
     */
    private fun setClickDateTime(formPart: EditText): EditText {
        formPart.setOnClickListener{
            val dtp = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                //フォーマットを作成
                formPart.setText(String.format("%d/%02d/%02d",y,m+1,d))
            }, year,month,day)
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
                    formPart.setText(String.format("%02d:%02d",hour,minute))
                },hour,minute,true)
            tp.show()
        }
        return formPart
    }

}
