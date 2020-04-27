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
            title.setError("入力必須です")
        }
        return title
    }









}