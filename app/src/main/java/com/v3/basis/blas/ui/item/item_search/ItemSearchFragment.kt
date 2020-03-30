package com.v3.basis.blas.ui.item.item_search

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
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
import com.v3.basis.blas.blasclass.app.searchAndroid
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.formaction.FormActionDataSearch
import com.v3.basis.blas.blasclass.helper.RestHelper
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestItem
import org.json.JSONObject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ItemSearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ItemSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ItemSearchFragment : Fragment() {
    //文字型またはview型
    private var token: String? = null
    private var projectId: String? = null
    private var rootView: LinearLayout? = null
    //コレクション各種
    private var formInfoMap:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap:MutableMap<String, EditText?>? = mutableMapOf()
    private var checkMap:MutableMap<String,MutableMap<String?, CheckBox?>>? = mutableMapOf()
    private var searchValue:MutableMap<String,String?> = mutableMapOf()
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
    private var formAction:FormActionDataSearch? = null
    //この下は最終的に消す
    private val jsonItemList:MutableMap<String,JSONObject> = mutableMapOf()
    private val itemList: MutableList<MutableMap<String, String?>> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val extras = activity?.intent?.extras
        if (extras?.getString("token") != null) {
            token = extras?.getString("token")
            Log.d("token_item", "${token}")
        }
        if (extras?.getString("project_id") != null) {
            projectId = extras?.getString("project_id")
        }
        formAction = FormActionDataSearch(token!!,activity!!)
        val root= inflater.inflate(R.layout.fragment_item_search, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view.findViewById<LinearLayout>(R.id.item_search_liner)

        val space = formAction!!.createSpace(layoutParamsSpace)
        val title = formAction!!.createFreeWordSearchTitle(layoutParams)
        val freeWordSearch = formAction!!.createFreeWordSearch(layoutParams)
        editMap!!.set(key="col_${0}",value = freeWordSearch)
        rootView!!.addView(space)
        rootView!!.addView(title)
        rootView!!.addView(freeWordSearch)

        //レイアウトの設置位置の設定
        val payload = mapOf("token" to token, "project_id" to projectId)
        BlasRestField(payload, ::getSuccess, ::getFail).execute()
    }

    private fun getSuccess(result:JSONObject?) {
        //カラム順に並べ替える
        var cnt = 1
        var checkCount = 1
        if (result != null) {
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
                val formInfo= formAction!!.typeCheck(it)
                //先に項目のタイトルをセットする
                val formSectionTitle = formAction!!.createFormSectionTitle(layoutParams,formInfo)
                rootView!!.addView(formSectionTitle)

                //フォームの項目の情報をメンバ変数に格納
                val typeMap = formAction!!.createFormInfoMap(formInfo)
                formInfoMap.set(key = "${cnt}",value =typeMap )


                when(formInfo.type){
                    FieldType.TEXT_FIELD, FieldType.TEXT_AREA->{
                        //自由入力(1行)または自由入力(複数行)
                        val formPart =formAction!!.createTextField(layoutParams,cnt,formInfo)
                        rootView!!.addView(formPart)

                        //配列にeditTextの情報を格納。
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.DATE_TIME->{
                        //日付入力
                        var formPart = formAction!!.createDateTime(layoutParams,cnt,formInfo)
                        formPart =  setClickDateTime(formPart)
                        rootView!!.addView(formPart)

                        //配列にeditTextを格納
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.TIME->{
                        //時間入力
                        var formPart = formAction!!.createDateTime(layoutParams,cnt,formInfo)
                        formPart = setClickTime(formPart)
                        rootView!!.addView(formPart)

                        //配列にeditTextを格納
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                        //チェックボックスの時
                        var colCheckMap : MutableMap<String?,CheckBox?> = mutableMapOf()
                        formInfo.choiceValue!!.forEach {
                            val formPart = formAction!!.createMutipleSelection(layoutParams,it,checkCount)
                            rootView!!.addView(formPart)
                            colCheckMap!!.set(key = "col_${cnt}_${checkCount}",value = formPart)
                            checkCount += 1

                        }
                        checkMap!!.set(key = "col_${cnt}",value = colCheckMap)
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
            button.text = "find"
            button.setLayoutParams(layoutParams)
            rootView!!.addView(button)

            //ボタン押下時の処理
            button.setOnClickListener{
                val freeWordEdit = editMap!!.get("col_0")!!
                val freeWordValue ="${freeWordEdit.text}"
                Log.d("検索結果(フリーワード)","${freeWordValue}")
                searchValue.set("freeWord",freeWordValue)
                var cnt = 1
                formInfoMap.forEach{
                    var value = ""
                    when(it.value["type"]){
                        FieldType.TEXT_FIELD,
                        FieldType.TEXT_AREA,
                        FieldType.DATE_TIME,
                        FieldType.TIME->{
                            //自由入力(1行)・自由入力(複数行)・日付入力・時間入力
                            value = formAction!!.pickUpValue(editMap,cnt)
                        }

                        FieldType.MULTIPLE_SELECTION , FieldType.SINGLE_SELECTION->{
                            //チェックボックス
                            val colCheckMap = checkMap!!.get("col_${cnt}")
                            value = formAction!!.getCheckedValues(colCheckMap)
                        }
                    }
                    Log.d("testtest","${value}")
                    searchValue.set("fld${cnt}",value)
                    cnt += 1
                }
                //ここから検索処理入れる
                //ここで新しいアクティビティ？フラグメントを起動
                //新しくitemを取得。
                searchValue.forEach{
                    Log.d("testesttest","${it}")
                }

                val payload2 = mapOf("token" to token, "project_id" to projectId)
                BlasRestItem("search", payload2, ::itemRecv, ::itemRecvError).execute()

            }
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun getFail(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        //fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()
    }

    /**
     * 日付フィールドタップ時の処理
     */
    private fun setClickDateTime(formPart: EditText): EditText {
        formPart.setOnClickListener{
            val dtp = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                Toast.makeText(activity, "日付を選択しました${y}/${m+1}/${d}", Toast.LENGTH_LONG).show()
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
            val tp = TimePickerDialog(getContext()!!,
                TimePickerDialog.OnTimeSetListener{ view, hour, minute->
                    Toast.makeText(activity, "時間を選択しました${hour}:${minute}", Toast.LENGTH_LONG).show()
                    formPart.setText(String.format("%02d:%02d",hour,minute))
                },hour,minute,true)
            tp.show()
        }
        return formPart
    }

    private fun itemRecv(result: JSONObject){
        jsonItemList.set("1",result)
        var colMax = formInfoMap.size
        Log.d("gafeaqwaf","${colMax}")
        val itemInfo = RestHelper().createItemList(jsonItemList, colMax )
        itemInfo.forEach{
            Log.d("testtest","${it}")
        }
        searchValue.forEach{
            Log.d("htsreafgrsdjf","${it}")
        }
        val test = searchAndroid(searchValue,itemInfo)
        Log.d("ログ取得中","==================================================================")

        val intent = Intent(activity, ItemSearchResultActivity::class.java)
        intent.putExtra("token",token)
        intent.putExtra("project_id",projectId)
        intent.putExtra("freeWord",searchValue["freeWord"])
        startActivity(intent)
    }

    private fun itemRecvError(errorCode: Int){

    }


}
