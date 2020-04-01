package com.v3.basis.blas.blasclass.formaction

import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.blasclass.config.FieldType

class FormActionDataSearch(setToken: String, setActivity: FragmentActivity) : FormActionDataBasic(setToken, setActivity){
    var setToken = super.token
    var setActivity = super.baseActivity
    var payloard = super.payload
    var mode :String? = null
        get() = field
        set(mode:String?){
            if(mode != null){
                field = mode
            }
        }

    override fun typeCheck(list: MutableMap<String, String?>?): formType {
        return super.typeCheck(list)
    }

    override fun createTextField(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        return super.createTextField(params, cnt, formInfo)
    }

    override fun createTextAlea(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        return super.createTextAlea(params, cnt, formInfo)
    }

    override fun createDateTime(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        return super.createDateTime(params, cnt, formInfo)
    }

    fun createFreeWordSearch(params: LinearLayout.LayoutParams?): EditText {
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.inputType =1
        edit.setLayoutParams(params)
        edit.id = 0
        return edit
    }

    fun createFreeWordSearchTitle(params: LinearLayout.LayoutParams?): TextView {
        val view = TextView(baseActivity)
        val title = "フリーワード検索"
        var formTitle =
        view.setText("${title}")
        //文字の色変更したい。
        view.setTextColor(Color.BLACK)
        view.setLayoutParams(params)
        return view
    }

    fun createNewDateTime(params: LinearLayout.LayoutParams?, cnt: Int): EditText{
        val edit = EditText(baseActivity)
        edit.setText("")
        edit.inputType = 1
        edit.setLayoutParams(params)
        edit.maxEms = 10
        edit.id = cnt
        //これがミソ！！！これなしだとタップ2回での起動になる
        edit.isFocusableInTouchMode = false


        return edit
    }


}