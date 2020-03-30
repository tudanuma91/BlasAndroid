package com.v3.basis.blas.blasclass.formaction

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.v3.basis.blas.blasclass.config.FieldType

class FormActionDataCreate(setToken: String, setActivity: FragmentActivity):FormActionDataBasic(setToken, setActivity) {
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

    override fun createFormSectionTitle(
        params: LinearLayout.LayoutParams?,
        formInfo: formType
    ): TextView {
        var title =  super.createFormSectionTitle(params, formInfo)
        if(formInfo.require == FieldType.TURE){
            //ラジオボタンかチェックボックスの場合、タイトルに入力必須を表示。
            when(formInfo.type){
                FieldType.SINGLE_SELECTION ->{title.setError("入力必須です")}
                FieldType.MULTIPLE_SELECTION ->{title.setError("入力必須です")}
            }
        }
        return title
    }



    override fun createTextField(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        var edit = super.createTextField(params, cnt, formInfo)
        edit = setMessate(edit,formInfo)
        return edit
    }


    override fun createTextAlea(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        var edit = super.createTextAlea(params, cnt, formInfo)
        edit = setMessate(edit,formInfo)
        return edit
    }


    override fun createDateTime(params: LinearLayout.LayoutParams?, cnt: Int, formInfo: formType): EditText {
        var edit = super.createDateTime(params, cnt, formInfo)
        edit = setMessate(edit,formInfo)
        return edit
    }


    fun setMessate(editText: EditText,formInfo: formType): EditText {
        if(formInfo.require == FieldType.TURE){
            editText.setError("入力必須の項目です")
        }
        return editText
    }



}