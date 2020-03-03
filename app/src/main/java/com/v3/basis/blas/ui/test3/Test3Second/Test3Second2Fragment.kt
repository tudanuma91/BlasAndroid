package com.v3.basis.blas.ui.test3.Test3Second


import android.app.ActionBar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.v3.basis.blas.activity.Test3Activity
import android.util.Log
import android.widget.*
import com.v3.basis.blas.R


/**
 * A simple [Fragment] subclass.
 */
class Test3Second2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val root = inflater.inflate(com.v3.basis.blas.R.layout.fragment_test3_second2, container, false)
        return root
    }

    fun replaceFragment(fragment: Fragment) {
        /*val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()*/
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //レイアウトの設定
        val test = view.findViewById<LinearLayout>(com.v3.basis.blas.R.id.framer)
        /*
            fragment_test3_second2にあるIDが"framer"のLinearLayoutを取得。
            取得したLinearLayoutにコンテンツを追加していく
         */

        //レイアウトの設置位置の設定
        val LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        //ボタンの作成処理
        val button = Button(activity)
        button.text = "send"
        button.setLayoutParams(LayoutParams)
        test.addView(button)

        //テキストの作成処理
        val text = TextView(activity)
        text.setText("aiueo")
        text.setLayoutParams(LayoutParams)
        test.addView(text)

        //editテキストの追加処理
        val edit  = EditText(activity)
        edit.setText("ddd")
        edit.setLayoutParams(LayoutParams)
        test.addView(edit)

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
        test.addView(r_group)
        //初期チェックしている処理
        r_button2.isChecked = true

        //チェックボックスの追加処理
        val checkbox = CheckBox(activity)
        checkbox.setText("aiueo")
        checkbox.setLayoutParams(LayoutParams)
        test.addView(checkbox)
        val checkbox2 = CheckBox(activity)
        checkbox2.setText("kakikukeko")
        checkbox2.setLayoutParams(LayoutParams)
        test.addView(checkbox2)

        //ボタン押下時の処理
        button.setOnClickListener{
            //editTextの値取得時
            Log.d("test!!!","${edit.text}")
            //radiobuttonにてチェックされている値の取得
            Log.d("test!!!","${r_group.getCheckedRadioButtonId()}")
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

}
