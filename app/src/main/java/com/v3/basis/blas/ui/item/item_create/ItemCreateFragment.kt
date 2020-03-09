package com.v3.basis.blas.ui.item.item_create


import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity

import com.v3.basis.blas.R
import com.v3.basis.blas.activity.ItemActivity
import com.v3.basis.blas.blasclass.config.FieldType
import com.v3.basis.blas.blasclass.rest.BlasRestField
import com.v3.basis.blas.blasclass.rest.BlasRestImageField
import com.v3.basis.blas.ui.item.item_view.ItemViewFragment

/**
 * A simple [Fragment] subclass.
 */
class ItemCreateFragment : Fragment() {
    private var token: String? = null
    private var projectId: String? = null
    private var rootView: LinearLayout? = null
    private var act:FragmentActivity? = null
    private var editMap:MutableMap<String, EditText?>? = mutableMapOf<String, EditText?>()
    private var fieldMap: MutableMap<Int, MutableMap<String, String?>> =
        mutableMapOf<Int, MutableMap<String, String?>>()
    private var layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )

    //data class formEditText(var id: Int, var editText: EditText)


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
        return inflater.inflate(R.layout.fragment_item_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //コンテンツを配置するLinearLayoutを取得
        rootView = view.findViewById<LinearLayout>(com.v3.basis.blas.R.id.item_create_liner)
        act = activity
        //レイアウトの設置位置の設定
        val payload = mapOf("token" to token, "project_id" to projectId)
        BlasRestField(payload, ::getSuccess, ::getFail).execute()

        //ボタンの作成処理
        val button = Button(activity)
        button.text = "send"
        button.setLayoutParams(layoutParams)
        rootView!!.addView(button)

        //テキストの作成処理
        val text = createTextBox(activity)
        rootView!!.addView(text)

        //editテキストの追加処理
        val edit = EditText(activity)
        edit.setText("ddd")
        edit.setLayoutParams(layoutParams)
        rootView!!.addView(edit)
        //array!!.set(key="col_aaa",value = edit)


        //ラジオボタンの追加処理
        //ラジオグループを追加（ラジオグループを実装した時のみ、複数選択肢から一つを選ぶという処理が可能）
        val r_group = RadioGroup(activity)
        //選択肢1
        val r_button = RadioButton(activity)
        r_button.setText("1")
        r_group.addView(r_button)
        //選択肢2
        val r_button2 = RadioButton(activity)
        r_button2.setText("2")
        r_group.addView(r_button2)
        rootView!!.addView(r_group)
        //初期チェックしている処理
        r_button2.isChecked = true

        //チェックボックスの追加処理
        val checkbox = CheckBox(activity)
        checkbox.setText("aiueo")
        checkbox.setLayoutParams(layoutParams)
        rootView!!.addView(checkbox)
        val checkbox2 = CheckBox(activity)
        checkbox2.setText("kakikukeko")
        checkbox2.setLayoutParams(layoutParams)
        rootView!!.addView(checkbox2)

        //ボタン押下時の処理
        button.setOnClickListener{
            //editTextの値取得時
            Log.d("test!!!","${edit.text}")
            //radiobuttonにてチェックされている値の取得
            Log.d("test!!!","${r_group.getCheckedRadioButtonId()}")
            //Log.d("test!!!","${}")
            editMap!!.forEach {
                Log.d("aaaa","${it.value!!.id}")
                Log.d("aaaa","${it.value!!.text}")
            }
        }


        /*
        [処理内容メモ]
        <<フォーム作成の処理(formCreate(col_type,value,action))>>
        onViewCreatedにover rideすること。
        処理内容は新規作成・または更新時にフォームを作成・表示する

        col_type : マップにて入力項目の属性を取得。
        value : マップにてタップされたitem_idのvalueを取得
        action : フォームが行うaction。現在はcreateかupdateを想定。
        ===================================================
        foreach(登録している項目の数)
        when
            TEXT_FIELD=> EditTextの文字入力
            TEXT_AREA=>  EditTextの文字入力( android:inputType="textMultiLine")
            ⒶDATE_TIME => DateTime入力する必要あり
            ⒶTIME =>　    Time入力する必要あり
            SINGLE_SELECTION => radiobuttonにて選択し作成。checkedを使う。
            MULTIPLE_SELECTION => checkbocにて選択し作成。命名規則で判別するので規則を考えること
            　=>fld_〇_選択肢を変数として使用。

            <--下4つは今のところノープラン-->
            KENPIN_RENDOU_QR
            SIG_FOX
            QR_CODE
            TEKKILYO_RENDOU_QR
            <------------------------------>
        ※Ⓐについては入力形式をDBのほうに合わせる必要があるため追加で調査する必要あり。入力のさせ方に工夫必要！
        ※update時については、初期値を入力すること。


       <<データ新規登録処理(dataCreate(value,data_rule,target))>>
        set.onClickListner(ボタン押下時の処理)に設定すること。
        処理内容は新規作成・または更新時にフォームを作成・表示する

        value : マップにて入力された項目とその値をマップで取得
        data_rule : 項目ごとの制約をまとめたマップを取得
        target : itemなのかkikiなのかを判別
        ===================================================
        data_ruleにて以下の判定を行う。
            ①data-typeにてunique_chkがtrue=>重複を認めない
        　   data-typeにてunique_chkがfalse=>重複を許可
            ②data-typeにて、nullableがfalse =>nullを認めない
        　   data-typeにて、nullableがtrue =>nullを許可
            ①②の判定がうまくいかなかった場合、トーストにてエラーを周知。項目とエラー内容を表示する
        入力されたデータを取得
        restfulを用いてデータの新規作成を行う
        できなかった場合、errorを表示する。（サーバーエラー？）=>ここちょっといらないかもしれない
        return データ一覧画面に戻る。


        <<データの更新処理(dataUpdate(value,data_rule,target))>>
        set.onClickListner(ボタン押下時の処理)に設定すること。
        処理内容は新規作成・または更新時にフォームを作成・表示する

        value : マップにて入力された項目とその値をマップで取得
        data_rule : 項目ごとの制約をまとめたマップを取得
        target : itemなのかkikiなのかを判別
        ===================================================
        data_ruleにて以下の判定を行う。
            ①data-typeにてunique_chkがtrue=>重複を認めない
        　   data-typeにてunique_chkがfalse=>重複を許可
            ②data-typeにて、nullableがfalse =>nullを認めない
        　   data-typeにて、nullableがtrue =>nullを許可
            ①②の判定がうまくいかなかった場合、トーストにてエラーを周知。項目とエラー内容を表示する
        入力されたデータを取得
        restfulを用いてデータの更新処理を行う
        できなかった場合、errorを表示する。（サーバーエラー？）=>ここちょっといらないかもしれない
        return データ一覧画面に戻る。

         */
    }

    private fun createTextBox(activity: FragmentActivity?): TextView {
        val text = TextView(activity)
        text.setText("aiueo")
        text.setLayoutParams(layoutParams)
        return text
    }

    private fun getSuccess(result: MutableList<MutableMap<String, String?>>?) {
        //カラム順に並べ替える
        var cnt = 1
        if (result != null) {
            result.forEach {
                Log.d("aaaaaaa","${it}")
                val (type,title,choiceValue) = ItemActivity().typeCheck(it)
                when(type){
                    FieldType.TEXT_FIELD->{
                        val formSectionTitle = ItemActivity().createFormSectionTitle(title,layoutParams,act)
                        val info = ItemActivity().createTextField(layoutParams,act,cnt)
                        rootView!!.addView(formSectionTitle)
                        rootView!!.addView(info)
                        editMap!!.set(key="col_${cnt}",value = info)
                    }

                }
                cnt += 1
               /* if(type !=null && title != null) {
                    val formTitle = ItemActivity().createFormTitle(title,layoutParams,act)
                    rootView!!.addView(formTitle)
                    //val formPart = ItemActivity().createFormPart(type, title, choiceValue, null,layoutParams,act)
                    //rootView!!.addView(formPart)
                }*/
            }
        }
    }

    /**
     * フィールド取得失敗時
     */
    private fun getFail(errorCode: Int) {
        Toast.makeText(getActivity(), errorCode.toString(), Toast.LENGTH_LONG).show()
        //エラーのため、データを初期化する
        fieldMap = mutableMapOf<Int, MutableMap<String, String?>>()


    }
}
