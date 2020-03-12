package com.v3.basis.blas.ui.item.item_edit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.rest.BlasRestField
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
    private var rootView: LinearLayout? = null
    private var formInfoMap:MutableMap<String, MutableMap<String, String?>> = mutableMapOf()
    private var editMap:MutableMap<String, EditText?>? = mutableMapOf()
    private var radioGroupMap:MutableMap<String, RadioGroup?>? = mutableMapOf()
    private var radioValue :MutableMap<String, RadioButton?>? = mutableMapOf()
    private var checkMap:MutableMap<String,MutableMap<String?, CheckBox?>>? = mutableMapOf()
    private var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    private var layoutParamsSpace = LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,50)
    private val calender = Calendar.getInstance()
    private val year = calender.get(Calendar.YEAR)
    private val month = calender.get(Calendar.MONTH)
    private val day = calender.get(Calendar.DAY_OF_MONTH)
    private val hour = calender.get(Calendar.YEAR)
    private val minute = calender.get(Calendar.MONTH)

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

        Log.d("token","${token}")
        Log.d("token","${projectId}")

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
        BlasRestField(payload, ::getSuccess, ::getFail).execute()
    }

    private fun getSuccess(result: MutableList<MutableMap<String, String?>>?) {
        //カラム順に並べ替える
        Log.d("呼ばれた","呼ばれた")
        var cnt = 1
        var radioCount = 1
        var checkCount = 1
        if (result != null) {
            //colによる並び替えが発生しているため、ソートを行う
            val resultSort = result!!.sortedBy { it["col"]!!.toInt() }
            resultSort.forEach {
                Log.d("aaaaaaa","${it}")

                /**
                 * formInfoには以下の情報が入っている。
                 * ・title => 項目のタイトル
                 * ・type => 項目のタイプ(日付入力など)
                 * ・choiceValue => 項目が持つ選択肢
                 * ・nullable => 項目がnullが許容するかの定義
                 * ・unique => 項目が重複を許可するかの定義
                 */
                val formInfo= ItemActivity().typeCheck(it)
                //先に項目のタイトルをセットする
                val formSectionTitle = ItemActivity().createFormSectionTitle(layoutParams,activity,formInfo)
                //formSectionTitle.setError("入力必須です")
                rootView!!.addView(formSectionTitle)

                //フォームの項目の情報をメンバ変数に格納
                val typeMap :MutableMap<String,String?> = mutableMapOf()
                typeMap.set(key = "type",value = "${formInfo.type}")
                typeMap.set(key = "require",value = "${formInfo.require}")
                typeMap.set(key = "unique",value = "${formInfo.unique}")
                formInfoMap.set(key = "${cnt}",value =typeMap )

                when(formInfo.type){
                    FieldType.TEXT_FIELD->{
                        //自由入力(1行)
                        //editTextを作成
                        val formPart = ItemActivity().createTextField(layoutParams,activity,cnt)
                        if(formInfo.require == FieldType.TURE){
                            formPart.setError("必須入力の項目です")
                        }
                        rootView!!.addView(formPart)
                        //配列にeditTextの情報を格納。
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.TEXT_AREA->{
                        //自由入力(複数行)
                        //editTextを作成
                        val formPart = ItemActivity().createTextAlea(layoutParams,activity,cnt)
                        if(formInfo.require == FieldType.TURE){
                            formPart.setError("必須入力の項目です")
                        }
                        rootView!!.addView(formPart)
                        //配列にeditTextの情報を格納
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.DATE_TIME->{
                        //日付入力
                        //editTextを作成
                        val formPart = ItemActivity().createDateTime(layoutParams,activity,cnt)
                        if(formInfo.require == FieldType.TURE){
                            formPart.setError("必須入力の項目です")
                        }
                        rootView!!.addView(formPart)

                        //editTextタップ時の処理
                        formPart.setOnClickListener{
                            val dtp = DatePickerDialog(getContext()!!, DatePickerDialog.OnDateSetListener{ view, y, m, d ->
                                Toast.makeText(activity, "日付を選択しました${y}/${m+1}/${d}", Toast.LENGTH_LONG).show()
                                //フォーマットを作成
                                formPart.setText(String.format("%d/%02d/%02d",y,m+1,d))
                            }, year,month,day)
                            dtp.show()
                        }
                        //配列にeditTextを格納
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.TIME->{
                        //時間入力
                        //editText作成
                        val formPart = ItemActivity().createDateTime(layoutParams,activity,cnt)
                        if(formInfo.require == FieldType.TURE){
                            formPart.setError("必須入力の項目です")
                        }
                        rootView!!.addView(formPart)

                        //editTextタップ時の処理
                        formPart.setOnClickListener{
                            val tp = TimePickerDialog(getContext()!!,
                                TimePickerDialog.OnTimeSetListener{ view, hour, minute->
                                    Toast.makeText(activity, "時間を選択しました${hour}:${minute}", Toast.LENGTH_LONG).show()
                                    formPart.setText(String.format("%02d:%02d",hour,minute))
                                },hour,minute,true)
                            tp.show()
                        }

                        //配列にeditTextを格納
                        editMap!!.set(key="col_${cnt}",value = formPart)
                    }

                    FieldType.SINGLE_SELECTION->{
                        //ラジオボタンの時
                        val formGroup = ItemActivity().createSelectionGroup(layoutParams,activity,cnt)
                        formInfo.choiceValue!!.forEach {
                            val formPart = ItemActivity().createSingleSelection(layoutParams,activity,it)
                            radioValue!!.set(key ="${radioCount}",value = formPart )
                            radioCount += 1
                            formGroup.addView(formPart)

                        }
                        rootView!!.addView(formGroup)
                        radioGroupMap!!.set(key="col_${cnt}",value = formGroup)
                    }

                    FieldType.MULTIPLE_SELECTION->{
                        //チェックボックスの時
                        var colCheckMap : MutableMap<String?,CheckBox?> = mutableMapOf()
                        formInfo.choiceValue!!.forEach {
                            val formPart = ItemActivity().createMutipleSelection(layoutParams,activity,it,checkCount)
                            Log.d("testtestest","${formPart.id}")
                            rootView!!.addView(formPart)
                            colCheckMap!!.set(key = "col_${cnt}_${checkCount}",value = formPart)
                            // checkMap!!.set(key = "col_${cnt}_${checkCount}",value = formPart)
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
            button.text = "send"
            button.setLayoutParams(layoutParams)
            rootView!!.addView(button)

            //ボタン押下時の処理
            button.setOnClickListener{
                var cnt = 1
                formInfoMap.forEach{
                    when(it.value["type"]){
                        FieldType.TEXT_FIELD,
                        FieldType.TEXT_AREA,
                        FieldType.DATE_TIME,
                        FieldType.TIME->{
                            //自由入力(1行)・自由入力(複数行)・日付入力・時間入力
                            val editText = editMap!!.get("col_${cnt}")
                            Log.d("aaa","${editText!!.text}")
                        }
                        FieldType.SINGLE_SELECTION->{
                            //ラジオボタン
                            val radioGroup = radioGroupMap!!.get("col_${cnt}")
                            val checkedRadioId = radioGroup!!.checkedRadioButtonId.toString()
                            Log.d("bbb","${checkedRadioId}")
                            val aaa = radioValue!!.get(checkedRadioId)
                            if(aaa != null) {
                                Log.d("tttt", "${aaa.text}")
                            }
                        }
                        FieldType.MULTIPLE_SELECTION->{
                            //チェックボックス
                            val colCheckMap = checkMap!!.get("col_${cnt}")
                            colCheckMap!!.forEach {
                                if(it.value!!.isChecked){
                                    Log.d("cccc","${it.value!!.text}")
                                    val aaa = it.value!!.text.toString()
                                }
                            }
                        }
                    }
                    cnt += 1
                }

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


}
